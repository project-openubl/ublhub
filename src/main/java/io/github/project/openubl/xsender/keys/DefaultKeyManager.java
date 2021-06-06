/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.keys;

import io.github.project.openubl.xsender.keys.component.ComponentModel;
import io.github.project.openubl.xsender.keys.component.utils.ComponentProviderLiteral;
import io.github.project.openubl.xsender.keys.component.utils.RsaKeyProviderLiteral;
import io.github.project.openubl.xsender.keys.qualifiers.RsaKeyType;
import org.jboss.logging.Logger;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.*;

@ApplicationScoped
public class DefaultKeyManager implements KeyManager {

    private static final Logger logger = Logger.getLogger(DefaultKeyManager.class);

    private final Map<String, List<KeyProvider>> providersMap = new HashMap<>();

    @Inject
    ComponentProvider componentProvider;

    @Inject
    @Any
    Instance<KeyProviderFactory> keyProviderFactories;

    @Override
    public KeyWrapper getActiveKey(String entityId, KeyUse use, String algorithm) {
        KeyWrapper activeKey = getActiveKey(getProviders(entityId), entityId, use, algorithm);
        if (activeKey != null) {
            return activeKey;
        }

        logger.debugv("Failed to find active key, trying fallback: entityId={0} algorithm={1} use={2}", entityId, algorithm, use.name());

        for (KeyProviderFactory kf : keyProviderFactories) {
            if (kf.createFallbackKeys(entityId, use, algorithm)) {
                providersMap.remove(entityId);
                List<KeyProvider> providers = getProviders(entityId);
                activeKey = getActiveKey(providers, entityId, use, algorithm);
                if (activeKey != null) {
                    logger.warnv("Fallback key created: entityId={0} algorithm={1} use={2}", entityId, algorithm, use.name());
                    return activeKey;
                } else {
                    break;
                }
            }
        }

        logger.errorv("Failed to create fallback key for organization: entityId={0} algorithm={1} use={2", entityId, algorithm, use.name());
        throw new RuntimeException("Failed to find key: entityId=" + entityId + " algorithm=" + algorithm + " use=" + use.name());
    }

    private KeyWrapper getActiveKey(List<KeyProvider> providers, String entityId, KeyUse use, String algorithm) {
        for (KeyProvider p : providers) {
            for (KeyWrapper key : p.getKeys()) {
                if (key.getStatus().isActive() && matches(key, use, algorithm)) {
                    if (logger.isTraceEnabled()) {
                        logger.tracev("Active key found: entityId={0} kid={1} algorithm={2} use={3}", entityId, key.getKid(), algorithm, use.name());
                    }

                    return key;
                }
            }
        }
        return null;
    }

    @Override
    public KeyWrapper getKey(String entityId, String kid, KeyUse use, String algorithm) {
        if (kid == null) {
            logger.warnv("kid is null, can't find public key", entityId, kid);
            return null;
        }

        for (KeyProvider p : getProviders(entityId)) {
            for (KeyWrapper key : p.getKeys()) {
                if (key.getKid().equals(kid) && key.getStatus().isEnabled() && matches(key, use, algorithm)) {
                    if (logger.isTraceEnabled()) {
                        logger.tracev("Found key: entityId={0} kid={1} algorithm={2} use={3}", entityId, key.getKid(), algorithm, use.name());
                    }

                    return key;
                }
            }
        }

        if (logger.isTraceEnabled()) {
            logger.tracev("Failed to find public key: entityId={0} kid={1} algorithm={2} use={3}", entityId, kid, algorithm, use.name());
        }

        return null;
    }

    @Override
    public List<KeyWrapper> getKeys(String entityId, KeyUse use, String algorithm) {
        List<KeyWrapper> keys = new LinkedList<>();
        for (KeyProvider p : getProviders(entityId)) {
            for (KeyWrapper key : p.getKeys()) {
                if (key.getStatus().isEnabled() && matches(key, use, algorithm)) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    @Override
    public List<KeyWrapper> getKeys(String entityId) {
        List<KeyWrapper> keys = new LinkedList<>();
        for (KeyProvider p : getProviders(entityId)) {
            keys.addAll(p.getKeys());
        }
        return keys;
    }

    private boolean matches(KeyWrapper key, KeyUse use, String algorithm) {
        return use.equals(key.getUse()) && key.getAlgorithm().equals(algorithm);
    }

    private List<KeyProvider> getProviders(String entityId) {
        List<KeyProvider> providers;
//        List<KeyProvider> providers = providersMap.get(entityId);
//        if (providers == null) {
        providers = new LinkedList<>();

        List<ComponentModel> components1 = componentProvider.getComponents(entityId, entityId, KeyProvider.class.getName());
        List<ComponentModel> components = new LinkedList<>(components1);
        components.sort(new ProviderComparator());

        for (ComponentModel c : components) {
            try {
                RsaKeyType rsaKeyType = RsaKeyType.findByProviderId(c.getProviderId()).orElseThrow(() -> new IllegalArgumentException("Invalid provider:" + c.getProviderId()));
                Annotation componentProviderLiteral = new ComponentProviderLiteral(KeyProvider.class);
                Annotation rsaKeyProviderLiteral = new RsaKeyProviderLiteral(rsaKeyType);
                KeyProviderFactory factory = keyProviderFactories.select(componentProviderLiteral, rsaKeyProviderLiteral).get();

                KeyProvider provider = factory.create(entityId, c);
                providers.add(provider);
            } catch (Throwable t) {
                logger.errorv(t, "Failed to load provider {0}", c.getId());
            }
        }

        providersMap.put(entityId, providers);
//        }
        return providers;
    }

    private static class ProviderComparator implements Comparator<ComponentModel> {

        @Override
        public int compare(ComponentModel o1, ComponentModel o2) {
            int i = Long.compare(o2.get("priority", 0l), o1.get("priority", 0l));
            return i != 0 ? i : o1.getId().compareTo(o2.getId());
        }

    }
}

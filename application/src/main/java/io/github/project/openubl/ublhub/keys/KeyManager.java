/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.keys;

import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.keys.qualifiers.ComponentProviderLiteral;
import io.github.project.openubl.ublhub.keys.qualifiers.RsaKeyProviderLiteral;
import io.github.project.openubl.ublhub.keys.qualifiers.RsaKeyType;
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class KeyManager {

    private static final Logger logger = Logger.getLogger(KeyManager.class);

    @Inject
    ComponentRepository componentRepository;

    @Inject
    @Any
    Instance<KeyProviderFactory<?>> keyProviderFactories;

    public KeyWrapper getActiveKeyWithoutFallback(ComponentOwner owner, KeyUse use, String algorithm) {
        List<KeyProvider> keyProviders = getProviders(owner);

        return getActiveKey(keyProviders, owner, use, algorithm);
    }

    public KeyWrapper getActiveKey(ComponentOwner owner, KeyUse use, String algorithm) {
        List<KeyProvider> keyProviders = getProviders(owner);

        KeyWrapper activeKey = getActiveKey(keyProviders, owner, use, algorithm);
        if (activeKey != null) {
            return activeKey;
        }

        logger.debugv("Failed to find active key, trying fallback: owner={0} algorithm={1} use={2}", owner, algorithm, use.name());

        boolean isPresent = keyProviderFactories.stream()
                .anyMatch(keyProviderFactory -> keyProviderFactory.createFallbackKeys(owner, use, algorithm));

        if (isPresent) {
            List<KeyProvider> providers = getProviders(owner);
            return getActiveKey(providers, owner, use, algorithm);
        } else {
            logger.errorv("Failed to create fallback key for realm: owner={0} algorithm={1} use={2}", owner, algorithm, use.name());
            throw  new RuntimeException("Failed to find key: owner=" + owner + " algorithm=" + algorithm + " use=" + use.name());
        }
    }

    private KeyWrapper getActiveKey(List<KeyProvider> providers, ComponentOwner owner, KeyUse use, String algorithm) {
        for (KeyProvider p : providers) {
            for (KeyWrapper key : p.getKeys()) {
                if (key.getStatus().isActive() && matches(key, use, algorithm)) {
                    if (logger.isTraceEnabled()) {
                        logger.tracev("Found key: owner={0} kid={1} algorithm={2} use={3}", owner, key.getKid(), algorithm, use.name());
                    }

                    return key;
                }
            }
        }
        return null;
    }

    public KeyWrapper getKey(ComponentOwner owner, String kid, KeyUse use, String algorithm) {
        if (kid == null) {
            logger.warnv("kid is null, can't find public key: owner={0}", owner);
            return null;
        }

        List<KeyProvider> keyProviders = getProviders(owner);

        for (KeyProvider p : keyProviders) {
            Optional<KeyWrapper> keyWrapper = p.getKeys().stream()
                    .filter(key -> Objects.equals(key.getKid(), kid) && key.getStatus().isEnabled() && matches(key, use, algorithm))
                    .peek(key -> {
                        if (logger.isTraceEnabled()) {
                            logger.tracev("Found key: owner={0} kid={1} algorithm={2} use={3}", owner, key.getKid(), algorithm, use.name());
                        }
                    })
                    .findFirst();
            if (keyWrapper.isPresent()) {
                return keyWrapper.get();
            }
        }

        if (logger.isTraceEnabled()) {
            logger.tracev("Failed to find public key: owner={0} kid={1} algorithm={2} use={3}", owner, kid, algorithm, use.name());
        }

        return null;
    }

    public List<KeyWrapper> getKeys(ComponentOwner owner, KeyUse use, String algorithm) {
        return getProviders(owner).stream()
                .flatMap(keyProvider -> keyProvider.getKeys().stream().filter(key -> key.getStatus().isEnabled() && matches(key, use, algorithm)))
                .collect(Collectors.toList());
    }

    public List<KeyWrapper> getKeys(ComponentOwner owner) {
        return getProviders(owner).stream()
                .flatMap(keyProvider -> keyProvider.getKeys().stream())
                .collect(Collectors.toList());
    }

    private boolean matches(KeyWrapper key, KeyUse use, String algorithm) {
        return use.equals(key.getUse()) && key.getAlgorithm().equals(algorithm);
    }

    private List<KeyProvider> getProviders(ComponentOwner owner) {
        return componentRepository.getComponents(owner, owner.getProject(), KeyProvider.class.getName())
                .stream()
                .sorted(new ProviderComparator())
                .map(c -> {
                    RsaKeyType rsaKeyType = RsaKeyType.findByProviderId(c.getProviderId()).orElseThrow(() -> new IllegalArgumentException("Invalid provider:" + c.getProviderId()));
                    Annotation componentProviderLiteral = new ComponentProviderLiteral(KeyProvider.class);
                    Annotation rsaKeyProviderLiteral = new RsaKeyProviderLiteral(rsaKeyType);
                    KeyProviderFactory<?> factory = keyProviderFactories.select(componentProviderLiteral, rsaKeyProviderLiteral).get();

                    return factory.create(owner, c);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static class ProviderComparator implements Comparator<ComponentModel> {
        @Override
        public int compare(ComponentModel o1, ComponentModel o2) {
            int i = Long.compare(o2.get("priority", 0L), o1.get("priority", 0L));
            return i != 0 ? i : o1.getId().compareTo(o2.getId());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class ActiveRsaKey {
        private String kid;
        private PrivateKey privateKey;
        private PublicKey publicKey;
        private X509Certificate certificate;
    }

}

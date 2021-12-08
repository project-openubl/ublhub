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
package io.github.project.openubl.ublhub.keys;

import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.component.utils.ComponentProviderLiteral;
import io.github.project.openubl.ublhub.keys.component.utils.RsaKeyProviderLiteral;
import io.github.project.openubl.ublhub.keys.qualifiers.RsaKeyType;
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class DefaultKeyManager implements KeyManager {

    private static final Logger logger = Logger.getLogger(DefaultKeyManager.class);

    @Inject
    ComponentRepository componentRepository;

    @Inject
    @Any
    Instance<KeyProviderFactory> keyProviderFactories;

    @Override
    public Uni<KeyWrapper> getActiveKey(NamespaceEntity namespace, KeyUse use, String algorithm) {
        return getProviders(namespace).chain(keyProviders -> {
            KeyWrapper activeKey = getActiveKey(keyProviders, namespace, use, algorithm);
            if (activeKey != null) {
                return Uni.createFrom().item(activeKey);
            }

            logger.debugv("Failed to find active key, trying fallback: namespace={0} algorithm={1} use={2}", namespace.id, algorithm, use.name());

            List<Uni<Boolean>> collect = keyProviderFactories.stream()
                    .map(keyProviderFactory -> (Uni<Boolean>) keyProviderFactory.createFallbackKeys(namespace, use, algorithm))
                    .collect(Collectors.toList());

            return Uni.combine().all().unis(collect)
                    .combinedWith(listOfResults -> {
                        List<Boolean> results = (List<Boolean>) listOfResults;
                        return results.stream().anyMatch(aBoolean -> Objects.equals(aBoolean, true));
                    })
                    .chain(isPresent -> isPresent
                            ? getProviders(namespace).map(providers -> getActiveKey(providers, namespace, use, algorithm))
                            : Uni.createFrom().nullItem()
                    )
                    .onItem().ifNull().failWith(() -> {
                        logger.errorv("Failed to create fallback key for realm: namespace={0} algorithm={1} use={2}", namespace.id, algorithm, use.name());
                        return new RuntimeException("Failed to find key: namespace=" + namespace.id + " algorithm=" + algorithm + " use=" + use.name());
                    });
        });
    }

    private KeyWrapper getActiveKey(List<KeyProvider> providers, NamespaceEntity namespace, KeyUse use, String algorithm) {
        for (KeyProvider p : providers) {
            for (KeyWrapper key : p.getKeys()) {
                if (key.getStatus().isActive() && matches(key, use, algorithm)) {
                    if (logger.isTraceEnabled()) {
                        logger.tracev("Found key: namespace={0} kid={1} algorithm={2} use={3}", namespace.id, key.getKid(), algorithm, use.name());
                    }

                    return key;
                }
            }
        }
        return null;
    }

    @Override
    public Uni<KeyWrapper> getKey(NamespaceEntity namespace, String kid, KeyUse use, String algorithm) {
        if (kid == null) {
            logger.warnv("kid is null, can't find public key: namespace={0}", namespace.id);
            return Uni.createFrom().nullItem();
        }

        return getProviders(namespace).map(keyProviders -> {
            for (KeyProvider p : keyProviders) {
                Optional<KeyWrapper> keyWrapper = p.getKeys().stream()
                        .filter(key -> Objects.equals(key.getKid(), kid) && key.getStatus().isEnabled() && matches(key, use, algorithm))
                        .peek(key -> {
                            if (logger.isTraceEnabled()) {
                                logger.tracev("Found key: namespace={0} kid={1} algorithm={2} use={3}", namespace.id, key.getKid(), algorithm, use.name());
                            }
                        })
                        .findFirst();
                if (keyWrapper.isPresent()) {
                    return keyWrapper.get();
                }
            }

            if (logger.isTraceEnabled()) {
                logger.tracev("Failed to find public key: namespace={0} kid={1} algorithm={2} use={3}", namespace.id, kid, algorithm, use.name());
            }

            return null;
        });
    }

    @Override
    public Uni<List<KeyWrapper>> getKeys(NamespaceEntity namespace, KeyUse use, String algorithm) {
        return getProviders(namespace).map(keyProviders -> keyProviders.stream()
                .flatMap(keyProvider -> keyProvider.getKeys().stream().filter(key -> key.getStatus().isEnabled() && matches(key, use, algorithm)))
                .collect(Collectors.toList())
        );
    }

    @Override
    public Uni<List<KeyWrapper>> getKeys(NamespaceEntity namespace) {
        return getProviders(namespace).map(keyProviders -> keyProviders.stream()
                .flatMap(keyProvider -> keyProvider.getKeys().stream())
                .collect(Collectors.toList())
        );
    }

    private boolean matches(KeyWrapper key, KeyUse use, String algorithm) {
        return use.equals(key.getUse()) && key.getAlgorithm().equals(algorithm);
    }

    private Uni<List<KeyProvider>> getProviders(NamespaceEntity namespace) {
        return componentRepository.getComponents(namespace.id, KeyProvider.class.getName())
                .map(componentModels -> componentModels.stream()
                        .sorted(new ProviderComparator())
                        .map(c -> {
                            RsaKeyType rsaKeyType = RsaKeyType.findByProviderId(c.getProviderId()).orElseThrow(() -> new IllegalArgumentException("Invalid provider:" + c.getProviderId()));
                            Annotation componentProviderLiteral = new ComponentProviderLiteral(KeyProvider.class);
                            Annotation rsaKeyProviderLiteral = new RsaKeyProviderLiteral(rsaKeyType);
                            KeyProviderFactory<?> factory = keyProviderFactories.select(componentProviderLiteral, rsaKeyProviderLiteral).get();

                            return factory.create(namespace, c);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                );
    }

    private static class ProviderComparator implements Comparator<ComponentModel> {
        @Override
        public int compare(ComponentModel o1, ComponentModel o2) {
            int i = Long.compare(o2.get("priority", 0L), o1.get("priority", 0L));
            return i != 0 ? i : o1.getId().compareTo(o2.getId());
        }
    }
}

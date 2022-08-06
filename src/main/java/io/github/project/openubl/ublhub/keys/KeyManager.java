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
import io.smallrye.mutiny.Uni;
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
    Instance<KeyProviderFactory> keyProviderFactories;

    Uni<KeyWrapper> getActiveKey(ComponentOwner owner, KeyUse use, String algorithm) {
        return getProviders(owner).chain(keyProviders -> {
            KeyWrapper activeKey = getActiveKey(keyProviders, owner, use, algorithm);
            if (activeKey != null) {
                return Uni.createFrom().item(activeKey);
            }

            logger.debugv("Failed to find active key, trying fallback: owner={0} algorithm={1} use={2}", owner, algorithm, use.name());

            List<Uni<Boolean>> collect = keyProviderFactories.stream()
                    .map(keyProviderFactory -> (Uni<Boolean>) keyProviderFactory.createFallbackKeys(owner, use, algorithm))
                    .collect(Collectors.toList());

            return Uni.combine().all().unis(collect)
                    .combinedWith(listOfResults -> {
                        List<Boolean> results = (List<Boolean>) listOfResults;
                        return results.stream().anyMatch(aBoolean -> Objects.equals(aBoolean, true));
                    })
                    .chain(isPresent -> isPresent
                            ? getProviders(owner).map(providers -> getActiveKey(providers, owner, use, algorithm))
                            : Uni.createFrom().nullItem()
                    )
                    .onItem().ifNull().failWith(() -> {
                        logger.errorv("Failed to create fallback key for realm: owner={0} algorithm={1} use={2}", owner, algorithm, use.name());
                        return new RuntimeException("Failed to find key: owner=" + owner + " algorithm=" + algorithm + " use=" + use.name());
                    });
        });
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

    public Uni<KeyWrapper> getKey(ComponentOwner owner, String kid, KeyUse use, String algorithm) {
        if (kid == null) {
            logger.warnv("kid is null, can't find public key: owner={0}", owner);
            return Uni.createFrom().nullItem();
        }

        return getProviders(owner).map(keyProviders -> {
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
        });
    }

    public Uni<List<KeyWrapper>> getKeys(ComponentOwner owner, KeyUse use, String algorithm) {
        return getProviders(owner).map(keyProviders -> keyProviders.stream()
                .flatMap(keyProvider -> keyProvider.getKeys().stream().filter(key -> key.getStatus().isEnabled() && matches(key, use, algorithm)))
                .collect(Collectors.toList())
        );
    }

    public Uni<List<KeyWrapper>> getKeys(ComponentOwner owner) {
        return getProviders(owner).map(keyProviders -> keyProviders.stream()
                .flatMap(keyProvider -> keyProvider.getKeys().stream())
                .collect(Collectors.toList())
        );
    }

    private boolean matches(KeyWrapper key, KeyUse use, String algorithm) {
        return use.equals(key.getUse()) && key.getAlgorithm().equals(algorithm);
    }

    private Uni<List<KeyProvider>> getProviders(ComponentOwner owner) {
        return componentRepository.getComponents(owner, owner.getId(), KeyProvider.class.getName())
                .map(componentModels -> componentModels.stream()
                        .sorted(new ProviderComparator())
                        .map(c -> {
                            RsaKeyType rsaKeyType = RsaKeyType.findByProviderId(c.getProviderId()).orElseThrow(() -> new IllegalArgumentException("Invalid provider:" + c.getProviderId()));
                            Annotation componentProviderLiteral = new ComponentProviderLiteral(KeyProvider.class);
                            Annotation rsaKeyProviderLiteral = new RsaKeyProviderLiteral(rsaKeyType);
                            KeyProviderFactory<?> factory = keyProviderFactories.select(componentProviderLiteral, rsaKeyProviderLiteral).get();

                            return factory.create(owner, c);
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

    class ActiveRsaKey {
        private final String kid;
        private final PrivateKey privateKey;
        private final PublicKey publicKey;
        private final X509Certificate certificate;

        public ActiveRsaKey(String kid, PrivateKey privateKey, PublicKey publicKey, X509Certificate certificate) {
            this.kid = kid;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.certificate = certificate;
        }

        public String getKid() {
            return kid;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }
    }

}

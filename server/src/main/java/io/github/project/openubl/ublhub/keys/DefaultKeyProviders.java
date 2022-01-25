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
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.smallrye.mutiny.Uni;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.crypto.KeyUse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class DefaultKeyProviders {

    @Inject
    ComponentRepository componentRepository;

    public Uni<Void> createProviders(NamespaceEntity namespace) {
        return hasProvider(namespace, "rsa-generated").chain(hasProvider -> {
            if (!hasProvider) {
                return createRsaKeyProvider("rsa-generated", KeyUse.SIG, namespace)
                        .chain(c -> Uni.createFrom().voidItem());
            } else {
                return Uni.createFrom().voidItem();
            }
        });
    }

    private Uni<ComponentModel> createRsaKeyProvider(String name, KeyUse keyUse, NamespaceEntity namespace) {
        ComponentModel generated = new ComponentModel();
        generated.setName(name);
        generated.setParentId(namespace.id);
        generated.setProviderId("rsa-generated");
        generated.setProviderType(KeyProvider.class.getName());

        MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
        config.putSingle("priority", "100");
        config.putSingle("keyUse", keyUse.getSpecName());
        generated.setConfig(config);

        return componentRepository.addComponentModel(namespace, generated);
    }

    protected Uni<Boolean> hasProvider(NamespaceEntity namespace, String providerId) {
        return componentRepository.getComponents(namespace.id, KeyProvider.class.getName())
                .map(componentModels -> componentModels
                        .stream()
                        .anyMatch(component -> Objects.equals(component.getProviderId(), providerId))
                );
    }
}

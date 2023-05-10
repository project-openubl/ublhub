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
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.crypto.KeyUse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class DefaultKeyProviders {

    @Inject
    ComponentRepository componentRepository;

    public void createProviders(ComponentOwner owner) {
        boolean hasProvider = hasProvider(owner, "rsa-generated");
        if (!hasProvider) {
            createRsaKeyProvider("rsa-generated", KeyUse.SIG, owner);
        }
    }

    private ComponentModel createRsaKeyProvider(String name, KeyUse keyUse, ComponentOwner owner) {
        ComponentModel generated = new ComponentModel();
        generated.setName(name);
        generated.setParentId(owner.getProject());
        generated.setProviderId("rsa-generated");
        generated.setProviderType(KeyProvider.class.getName());

        MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
        config.putSingle("priority", "100");
        config.putSingle("keyUse", keyUse.getSpecName());
        generated.setConfig(config);

        return componentRepository.addComponentModel(owner, generated);
    }

    protected boolean hasProvider(ComponentOwner owner, String providerId) {
        return componentRepository.getComponents(owner, owner.getProject(), KeyProvider.class.getName()).stream()
                .anyMatch(component -> Objects.equals(component.getProviderId(), providerId));
    }
}

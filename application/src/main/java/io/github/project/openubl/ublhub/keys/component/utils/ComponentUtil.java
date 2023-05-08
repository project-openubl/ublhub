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
package io.github.project.openubl.ublhub.keys.component.utils;

import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.keys.ImportedRsaKeyProviderFactory;
import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.github.project.openubl.ublhub.keys.component.ComponentFactory;
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.provider.ProviderConfigProperty;
import io.github.project.openubl.ublhub.keys.qualifiers.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ComponentUtil {

    @Inject
    @Any
    Instance<ComponentFactory<?, ?>> componentFactories;

    @Inject
    @ComponentProviderType(providerType = KeyProvider.class)
    @RsaKeyProviderType(type = RsaKeyType.IMPORTED)
    ImportedRsaKeyProviderFactory importedRsaKeyProviderFactory;


    public Map<String, ProviderConfigProperty> getComponentConfigProperties(ComponentDto component) {
        return getComponentConfigProperties(component.getProviderType(), component.getProviderId());
    }

    public Map<String, ProviderConfigProperty> getComponentConfigProperties(ComponentModel component) {
        return getComponentConfigProperties(component.getProviderType(), component.getProviderId());
    }

    public ComponentFactory getComponentFactory(ComponentDto component) {
        return getComponentFactory(component.getProviderType(), component.getProviderId());
    }

    public ComponentFactory getComponentFactory(ComponentModel component) {
        return getComponentFactory(component.getProviderType(), component.getProviderId());
    }

    public Map<String, ProviderConfigProperty> getComponentConfigProperties(String providerType, String providerId) {
        try {
            ComponentFactory componentFactory = getComponentFactory(providerType, providerId);
            List<ProviderConfigProperty> l = componentFactory.getConfigProperties();
            Map<String, ProviderConfigProperty> properties = new HashMap<>();
            for (ProviderConfigProperty p : l) {
                properties.put(p.getName(), p);
            }
            List<ProviderConfigProperty> common = componentFactory.getCommonProviderConfigProperties();
            for (ProviderConfigProperty p : common) {
                properties.put(p.getName(), p);
            }

            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ComponentFactory getComponentFactory(String providerType, String providerId) {
        try {
            Class<?> aClass = Class.forName(providerType);

            Optional<RsaKeyType> op = RsaKeyType.findByProviderId(providerId);
            if (op.isEmpty()) {
                return null;
            }

            Annotation componentProviderLiteral = new ComponentProviderLiteral(aClass);
            Annotation rsaKeyProviderLiteral = new RsaKeyProviderLiteral(op.get());

            return componentFactories.select(componentProviderLiteral, rsaKeyProviderLiteral).get();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Invalid factory", e);
        }
    }
}

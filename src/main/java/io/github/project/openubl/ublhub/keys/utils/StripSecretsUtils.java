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
package io.github.project.openubl.ublhub.keys.utils;

import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.keys.component.utils.ComponentUtil;
import io.github.project.openubl.ublhub.keys.provider.ProviderConfigProperty;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentExportRepresentation;
import org.keycloak.representations.idm.ComponentRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class StripSecretsUtils {

    private static final Pattern VAULT_VALUE = Pattern.compile("^\\$\\{vault\\.(.+?)}$");

    @Inject
    ComponentUtil componentUtil;

    private static String maskNonVaultValue(String value) {
        return value == null
                ? null
                : (VAULT_VALUE.matcher(value).matches()
                ? value
                : ComponentRepresentation.SECRET_VALUE
        );
    }

    public ComponentDto strip(ComponentDto componentDto) {
        Map<String, ProviderConfigProperty> configProperties = componentUtil.getComponentConfigProperties(componentDto);
        if (componentDto.getConfig() == null) {
            return componentDto;
        }

        Iterator<Map.Entry<String, List<String>>> itr = componentDto.getConfig().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, List<String>> next = itr.next();
            ProviderConfigProperty configProperty = configProperties.get(next.getKey());
            if (configProperty != null) {
                if (configProperty.isSecret()) {
                    if (next.getValue() == null || next.getValue().isEmpty()) {
                        next.setValue(Collections.singletonList(ComponentRepresentation.SECRET_VALUE));
                    } else {
                        next.setValue(next.getValue().stream().map(StripSecretsUtils::maskNonVaultValue).collect(Collectors.toList()));
                    }
                }
            } else {
                itr.remove();
            }
        }
        return componentDto;
    }

    public static ComponentExportRepresentation strip(ComponentUtil componentUtil, String providerType, ComponentExportRepresentation rep) {
        Map<String, ProviderConfigProperty> configProperties = componentUtil.getComponentConfigProperties(providerType, rep.getProviderId());
        if (rep.getConfig() == null) {
            return rep;
        }

        Iterator<Map.Entry<String, List<String>>> itr = rep.getConfig().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, List<String>> next = itr.next();
            ProviderConfigProperty configProperty = configProperties.get(next.getKey());
            if (configProperty != null) {
                if (configProperty.isSecret()) {
                    if (next.getValue() == null || next.getValue().isEmpty()) {
                        next.setValue(Collections.singletonList(ComponentRepresentation.SECRET_VALUE));
                    } else {
                        next.setValue(next.getValue().stream().map(StripSecretsUtils::maskNonVaultValue).collect(Collectors.toList()));
                    }
                }
            } else {
                itr.remove();
            }
        }

        MultivaluedHashMap<String, ComponentExportRepresentation> sub = rep.getSubComponents();
        for (Map.Entry<String, List<ComponentExportRepresentation>> ent : sub.entrySet()) {
            for (ComponentExportRepresentation c : ent.getValue()) {
                strip(componentUtil, ent.getKey(), c);
            }
        }
        return rep;
    }

}

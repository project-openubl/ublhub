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
package io.github.project.openubl.ublhub.mapper;

import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.keys.Attributes;
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.utils.StripSecretsUtils;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

@Mapper(componentModel = "cdi", builder = @Builder(disableBuilder = true))
public abstract class ComponentMapper {

    @Inject
    StripSecretsUtils stripSecretsUtils;

    public abstract ComponentDto toDto(ComponentModel model, @Context boolean internal);

    @AfterMapping
    protected void afterMappingToDto(ComponentModel model, @MappingTarget ComponentDto dto, @Context boolean internal) {
        if (!internal) {
            stripSecretsUtils.strip(dto);
        }
    }

    public abstract ComponentModel toModel(ComponentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "providerId", source = "providerId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "providerType", source = "providerType", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parentId", source = "parentId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "subType", source = "subType", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract ComponentModel updateModelFromDto(ComponentDto dto, @MappingTarget ComponentModel model);

    @BeforeMapping
    protected void beforeMappingToComponentModel(ComponentDto dto, @MappingTarget ComponentModel model) {
        if (dto.getConfig() != null) {
            Set<String> keys = new HashSet<>(dto.getConfig().keySet());
            for (String k : keys) {
                List<String> values = dto.getConfig().get(k);
                if (values == null || values.isEmpty() || values.get(0) == null || values.get(0).trim().isEmpty()) {
                    dto.getConfig().remove(k);
                } else {
                    ListIterator<String> itr = values.listIterator();
                    while (itr.hasNext()) {
                        String v = itr.next();
                        if (v == null || v.trim().isEmpty() || v.equals(ComponentRepresentation.SECRET_VALUE)) {
                            itr.remove();
                        }
                    }

                    if (values.isEmpty()) {
                        dto.getConfig().put(k, model.getConfig().get(k));
                    }
                }
            }
        }
    }
}

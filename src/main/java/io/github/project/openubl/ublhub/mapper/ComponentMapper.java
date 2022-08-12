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
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.utils.StripSecretsUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import javax.inject.Inject;

@Mapper(componentModel = "cdi", builder = @Builder(disableBuilder = true))
public abstract class ComponentMapper {

    @Inject
    StripSecretsUtils stripSecretsUtils;

    public abstract ComponentDto toDto(ComponentModel model, @Context boolean internal);

    public abstract ComponentModel toModel(ComponentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "providerId", source = "providerId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "providerType", source = "providerType", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parentId", source = "parentId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "subType", source = "subType", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract ComponentModel updateModelFromDto(ComponentDto dto, @MappingTarget ComponentModel model, @Context boolean internal);

    @AfterMapping
    protected void afterMapping(ComponentModel model, @MappingTarget ComponentDto dto, @Context boolean internal) {
        if (!internal) {
            stripSecretsUtils.strip(dto);
        }
    }
}

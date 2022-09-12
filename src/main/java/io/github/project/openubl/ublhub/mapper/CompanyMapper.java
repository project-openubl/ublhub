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

import io.github.project.openubl.ublhub.dto.CompanyDto;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", builder = @Builder(disableBuilder = true))
public abstract class CompanyMapper {

    @Mapping(target = "sunatWebServices.factura", source = "sunat.sunatUrlFactura")
    @Mapping(target = "sunatWebServices.guia", source = "sunat.sunatUrlGuiaRemision")
    @Mapping(target = "sunatWebServices.retencion", source = "sunat.sunatUrlPercepcionRetencion")
    @Mapping(target = "sunatCredentials.username", source = "sunat.sunatUsername")
    @Mapping(target = "sunatCredentials.password", ignore = true)
    public abstract CompanyDto toDto(CompanyEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sunat.sunatUrlFactura", source = "sunatWebServices.factura")
    @Mapping(target = "sunat.sunatUrlGuiaRemision", source = "sunatWebServices.guia")
    @Mapping(target = "sunat.sunatUrlPercepcionRetencion", source = "sunatWebServices.retencion")
    @Mapping(target = "sunat.sunatUsername", source = "sunatCredentials.username", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sunat.sunatPassword", source = "sunatCredentials.password", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract CompanyEntity updateEntityFromDto(CompanyDto dto, @MappingTarget CompanyEntity entity);

}

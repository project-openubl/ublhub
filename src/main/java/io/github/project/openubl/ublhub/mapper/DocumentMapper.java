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

import io.github.project.openubl.ublhub.dto.DocumentDto;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Date;

@Mapper(componentModel = "cdi", builder = @Builder(disableBuilder = true))
public interface DocumentMapper {

    @Mapping(target = "status.inProgress", source = "jobInProgress")

    @Mapping(target = "status.xmlData.ruc", source = "xmlData.ruc")
    @Mapping(target = "status.xmlData.serieNumero", source = "xmlData.serieNumero")
    @Mapping(target = "status.xmlData.tipoDocumento", source = "xmlData.tipoDocumento")

    @Mapping(target = "status.sunat", source = "sunatResponse")
    @Mapping(target = "status.error", source = "error")
    DocumentDto toDto(UBLDocumentEntity entity);

    @AfterMapping
    default void afterMapping(UBLDocumentEntity entity, @MappingTarget DocumentDto dto) {
        if (dto.getStatus().getSunat() != null) {
            dto.getStatus().getSunat().setHasCdr(entity.getCdrFileId() != null);
        }
    }

    default Long dateToLong(Date date) {
        return date.getTime();
    }
}

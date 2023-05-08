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

import io.github.project.openubl.ublhub.models.jpa.entities.XMLDataEntity;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi", builder = @Builder(disableBuilder = true))
public abstract class XmlContentMapper {

    @Mapping(target = "documentType", source = "tipoDocumento")
    @Mapping(target = "documentID", source = "serieNumero")
    @Mapping(target = "ruc", source = "ruc")
    @Mapping(target = "voidedLineDocumentTypeCode", source = "bajaCodigoTipoDocumento")
    public abstract XmlContent toXmlContent(XMLDataEntity entity);

    @Mapping(target = "tipoDocumento", source = "documentType")
    @Mapping(target = "serieNumero", source = "documentID")
    @Mapping(target = "ruc", source = "ruc")
    @Mapping(target = "bajaCodigoTipoDocumento", source = "voidedLineDocumentTypeCode")
    public abstract XMLDataEntity toEntity(XmlContent xmlContent);
}

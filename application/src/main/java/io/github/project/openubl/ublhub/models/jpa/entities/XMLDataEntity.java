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
package io.github.project.openubl.ublhub.models.jpa.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class XMLDataEntity {

    @NotNull
    @Size(max = 11)
    @Column(name = "xml_ruc")
    private String ruc;

    @NotNull
    @Size(max = 50)
    @Column(name = "xml_serie_numero")
    private String serieNumero;

    @NotNull
    @Size(max = 50)
    @Column(name = "xml_tipo_documento")
    private String tipoDocumento;

    @Size(max = 50)
    @Column(name = "xml_baja_codigo_tipo_documento")
    private String bajaCodigoTipoDocumento;

}

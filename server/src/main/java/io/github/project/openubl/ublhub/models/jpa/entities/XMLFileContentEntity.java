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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "xml_file_content")
public class XMLFileContentEntity extends BaseEntity {

    @Id
    public String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    public UBLDocumentEntity document;

    @NotNull
    @Size(max = 11)
    @Column(name = "ruc")
    public String ruc;

    @NotNull
    @Size(max = 50)
    @Column(name = "serie_numero")
    public String serieNumero;

    @NotNull
    @Size(max = 50)
    @Column(name = "tipo_documento")
    public String tipoDocumento;

    @Size(max = 50)
    @Column(name = "baja_codigo_tipo_documento")
    public String bajaCodigoTipoDocumento;

}

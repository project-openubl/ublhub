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
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SunatEntity {

    @NotNull
    @Size(max = 255)
    @Column(name = "sunat_username")
    private String sunatUsername;

    @NotNull
    @Size(max = 255)
    @Column(name = "sunat_password")
    private String sunatPassword;

    @NotNull
    @URL
    @Size(max = 255)
    @Column(name = "sunat_url_factura")
    private String sunatUrlFactura;

    @NotNull
    @URL
    @Size(max = 255)
    @Column(name = "sunat_url_guia_remision")
    private String sunatUrlGuiaRemision;

    @NotNull
    @URL
    @Size(max = 255)
    @Column(name = "sunat_url_percepcion_retencion")
    private String sunatUrlPercepcionRetencion;

}

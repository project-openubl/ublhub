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

import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class SunatEntity {

    @NotNull
    @URL
    @Column(name = "sunat_url_factura")
    public String sunatUrlFactura;

    @NotNull
    @URL
    @Column(name = "sunat_url_guia_remision")
    public String sunatUrlGuiaRemision;

    @NotNull
    @URL
    @Column(name = "sunat_url_percepcion_retencion")
    public String sunatUrlPercepcionRetencion;

    @NotNull
    @Column(name = "sunat_username")
    public String sunatUsername;

    @NotNull
    @Column(name = "sunat_password")
    public String sunatPassword;

}

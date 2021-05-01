/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.models.jpa.entities;

import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class SunatUrlsEntity {

    @NotNull
    @URL
    @Column(name = "sunat_url_factura")
    private String sunatUrlFactura;

    @NotNull
    @URL
    @Column(name = "sunat_url_guia_remision")
    private String sunatUrlGuiaRemision;

    @NotNull
    @URL
    @Column(name = "sunat_url_percepcion_retencion")
    private String sunatUrlPercepcionRetencion;

    public String getSunatUrlFactura() {
        return sunatUrlFactura;
    }

    public void setSunatUrlFactura(String sunatUrlFactura) {
        this.sunatUrlFactura = sunatUrlFactura;
    }

    public String getSunatUrlGuiaRemision() {
        return sunatUrlGuiaRemision;
    }

    public void setSunatUrlGuiaRemision(String sunatUrlGuiaRemision) {
        this.sunatUrlGuiaRemision = sunatUrlGuiaRemision;
    }

    public String getSunatUrlPercepcionRetencion() {
        return sunatUrlPercepcionRetencion;
    }

    public void setSunatUrlPercepcionRetencion(String sunatUrlPercepcionRetencion) {
        this.sunatUrlPercepcionRetencion = sunatUrlPercepcionRetencion;
    }

    public static final class Builder {
        private String sunatUrlFactura;
        private String sunatUrlGuiaRemision;
        private String sunatUrlPercepcionRetencion;

        private Builder() {
        }

        public static Builder aSunatUrlsEntity() {
            return new Builder();
        }

        public Builder withSunatUrlFactura(String sunatUrlFactura) {
            this.sunatUrlFactura = sunatUrlFactura;
            return this;
        }

        public Builder withSunatUrlGuiaRemision(String sunatUrlGuiaRemision) {
            this.sunatUrlGuiaRemision = sunatUrlGuiaRemision;
            return this;
        }

        public Builder withSunatUrlPercepcionRetencion(String sunatUrlPercepcionRetencion) {
            this.sunatUrlPercepcionRetencion = sunatUrlPercepcionRetencion;
            return this;
        }

        public SunatUrlsEntity build() {
            SunatUrlsEntity sunatUrlsEntity = new SunatUrlsEntity();
            sunatUrlsEntity.setSunatUrlFactura(sunatUrlFactura);
            sunatUrlsEntity.setSunatUrlGuiaRemision(sunatUrlGuiaRemision);
            sunatUrlsEntity.setSunatUrlPercepcionRetencion(sunatUrlPercepcionRetencion);
            return sunatUrlsEntity;
        }
    }
}

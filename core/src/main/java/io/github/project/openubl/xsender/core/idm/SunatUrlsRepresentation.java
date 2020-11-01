/**
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
package io.github.project.openubl.xsender.core.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.validator.constraints.URL;

@RegisterForReflection
public class SunatUrlsRepresentation {

    @URL
    private String factura;

    @URL
    private String guia;

    @URL
    private String retencion;

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getGuia() {
        return guia;
    }

    public void setGuia(String guia) {
        this.guia = guia;
    }

    public String getRetencion() {
        return retencion;
    }

    public void setRetencion(String retencion) {
        this.retencion = retencion;
    }


    public static final class Builder {
        private String factura;
        private String guiaRemision;
        private String percepcionRetencion;

        private Builder() {
        }

        public static Builder aSunatUrlsRepresentation() {
            return new Builder();
        }

        public Builder withFactura(String factura) {
            this.factura = factura;
            return this;
        }

        public Builder withGuiaRemision(String guiaRemision) {
            this.guiaRemision = guiaRemision;
            return this;
        }

        public Builder withPercepcionRetencion(String percepcionRetencion) {
            this.percepcionRetencion = percepcionRetencion;
            return this;
        }

        public SunatUrlsRepresentation build() {
            SunatUrlsRepresentation sunatUrlsRepresentation = new SunatUrlsRepresentation();
            sunatUrlsRepresentation.setFactura(factura);
            sunatUrlsRepresentation.setGuia(guiaRemision);
            sunatUrlsRepresentation.setRetencion(percepcionRetencion);
            return sunatUrlsRepresentation;
        }
    }
}

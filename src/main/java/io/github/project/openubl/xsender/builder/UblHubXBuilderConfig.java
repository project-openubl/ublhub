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
package io.github.project.openubl.xsender.builder;

import io.github.project.openubl.xmlbuilderlib.config.Config;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog10;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog7;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog9;

import java.math.BigDecimal;

public class UblHubXBuilderConfig implements Config {

    private BigDecimal igv;
    private BigDecimal ivap;
    private String defaultMoneda;
    private String defaultUnidadMedida;
    private Catalog9 defaultTipoNotaCredito;
    private Catalog10 defaultTipoNotaDebito;
    private BigDecimal defaultIcb;
    private Catalog7 defaultTipoIgv;

    @Override
    public BigDecimal getIgv() {
        return igv;
    }

    @Override
    public BigDecimal getIvap() {
        return ivap;
    }

    @Override
    public String getDefaultMoneda() {
        return defaultMoneda;
    }

    @Override
    public String getDefaultUnidadMedida() {
        return defaultUnidadMedida;
    }

    @Override
    public Catalog9 getDefaultTipoNotaCredito() {
        return defaultTipoNotaCredito;
    }

    @Override
    public Catalog10 getDefaultTipoNotaDebito() {
        return defaultTipoNotaDebito;
    }

    @Override
    public BigDecimal getDefaultIcb() {
        return defaultIcb;
    }

    @Override
    public Catalog7 getDefaultTipoIgv() {
        return defaultTipoIgv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public void setIvap(BigDecimal ivap) {
        this.ivap = ivap;
    }

    public void setDefaultMoneda(String defaultMoneda) {
        this.defaultMoneda = defaultMoneda;
    }

    public void setDefaultUnidadMedida(String defaultUnidadMedida) {
        this.defaultUnidadMedida = defaultUnidadMedida;
    }

    public void setDefaultTipoNotaCredito(Catalog9 defaultTipoNotaCredito) {
        this.defaultTipoNotaCredito = defaultTipoNotaCredito;
    }

    public void setDefaultTipoNotaDebito(Catalog10 defaultTipoNotaDebito) {
        this.defaultTipoNotaDebito = defaultTipoNotaDebito;
    }

    public void setDefaultIcb(BigDecimal defaultIcb) {
        this.defaultIcb = defaultIcb;
    }

    public void setDefaultTipoIgv(Catalog7 defaultTipoIgv) {
        this.defaultTipoIgv = defaultTipoIgv;
    }
}

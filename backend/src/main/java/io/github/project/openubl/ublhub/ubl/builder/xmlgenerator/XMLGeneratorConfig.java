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
package io.github.project.openubl.ublhub.ubl.builder.xmlgenerator;

import io.github.project.openubl.xmlbuilderlib.config.Config;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog10;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog7;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog9;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.math.BigDecimal;

@Default
@ApplicationScoped
public class XMLGeneratorConfig implements Config {

    @ConfigProperty(name = "openubl.xbuilder.igv")
    public BigDecimal igv;

    @ConfigProperty(name = "openubl.xbuilder.ivap")
    public BigDecimal ivap;

    @ConfigProperty(name = "openubl.xbuilder.defaultMoneda")
    public String defaultMoneda;

    @ConfigProperty(name = "openubl.xbuilder.defaultUnidadMedida")
    public String defaultUnidadMedida;

    @ConfigProperty(name = "openubl.xbuilder.defaultTipoNotaCredito")
    public String defaultTipoNotaCredito;

    @ConfigProperty(name = "openubl.xbuilder.defaultTipoNotaDebito")
    public String defaultTipoNotaDebito;

    @ConfigProperty(name = "openubl.xbuilder.defaultIcb")
    public BigDecimal defaultIcb;

    @ConfigProperty(name = "openubl.xbuilder.defaultTipoIgv")
    public String defaultTipoIgv;

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
        return Catalog.valueOfCode(Catalog9.class, defaultTipoNotaCredito)
                .orElseThrow(() -> new IllegalStateException("Invalid defaultTipoNotaCredito in config"));
    }

    @Override
    public Catalog10 getDefaultTipoNotaDebito() {
        return Catalog.valueOfCode(Catalog10.class, defaultTipoNotaDebito)
                .orElseThrow(() -> new IllegalStateException("Invalid defaultTipoNotaDebito in config"));
    }

    @Override
    public BigDecimal getDefaultIcb() {
        return defaultIcb;
    }

    @Override
    public Catalog7 getDefaultTipoIgv() {
        return Catalog.valueOfCode(Catalog7.class, defaultTipoIgv)
                .orElseThrow(() -> new IllegalStateException("Invalid defaultTipoIgv in config"));
    }

}

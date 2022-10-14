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
package io.github.project.openubl.ublhub.ubl.content.models.standard.general;

import io.github.project.openubl.ublhub.ubl.content.catalogs.Catalog16;
import io.github.project.openubl.ublhub.ubl.content.catalogs.Catalog7;
import io.github.project.openubl.ublhub.ubl.content.catalogs.validation.CatalogConstraint;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class DocumentoDetalle {

    @Min(0)
    public Integer index;

    @NotNull
    @NotBlank
    public String descripcion;

    public String unidadMedida;

    @NotNull
    @Positive
    @Digits(integer = 100, fraction = 3)
    public BigDecimal cantidad;

    @NotNull
    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal precio;
    public boolean precioConImpuestos;

    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal precioReferencia;

    @CatalogConstraint(value = Catalog16.class)
    public String precioReferenciaTipo;

    // Impuestos

    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal igv;

    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal igvBaseImponible;

    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal igvTasa;

    @CatalogConstraint(value = Catalog7.class)
    public String igvTipo;

    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal icb;

    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal icbTasa;

    public boolean icbAplica;

    // Totales
    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal totalImpuestos;

    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal valorVentaSinImpuestos;

}

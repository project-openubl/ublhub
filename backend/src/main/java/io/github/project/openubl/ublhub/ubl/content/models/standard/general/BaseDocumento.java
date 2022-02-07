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

import io.github.project.openubl.ublhub.ubl.content.catalogs.CatalogContadoCredito;
import io.github.project.openubl.ublhub.ubl.content.catalogs.validation.CatalogConstraint;
import io.github.project.openubl.ublhub.ubl.content.models.common.Cliente;
import io.github.project.openubl.ublhub.ubl.content.models.common.Firmante;
import io.github.project.openubl.ublhub.ubl.content.models.common.Proveedor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public abstract class BaseDocumento {

    @Size(min = 3, max = 3)
    public String moneda;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^[F|f|B|b].*$")
    @Size(min = 4, max = 4)
    public String serie;

    @NotNull
    @Min(1)
    @Max(99999999)
    public Integer numero;

    public LocalDate fechaEmision;
    public LocalTime horaEmision;

    @NotNull
    @Valid
    public Cliente cliente;

    @NotNull
    @Valid
    public Proveedor proveedor;

    @Valid
    public Firmante firmante;

    @NotNull
    @NotEmpty
    @Valid
    public List<DocumentoDetalle> detalle;

    @Valid
    public TotalImporte totalImporte;

    @Valid
    public TotalImpuestos totalImpuestos;


    @CatalogConstraint(value = CatalogContadoCredito.class)
    public String formaDePago;

    @Valid
    public List<CuotaDePago> formaDePagoCuotas;


    @Valid
    public List<GuiaRemisionRelacionada> guiasRemisionRelacionadas;
}

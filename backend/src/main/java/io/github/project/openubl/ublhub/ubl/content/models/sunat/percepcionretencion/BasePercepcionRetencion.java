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
package io.github.project.openubl.ublhub.ubl.content.models.sunat.percepcionretencion;

import io.github.project.openubl.ublhub.ubl.content.models.common.Cliente;
import io.github.project.openubl.ublhub.ubl.content.models.common.Firmante;
import io.github.project.openubl.ublhub.ubl.content.models.common.Proveedor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public abstract class BasePercepcionRetencion {

    @NotNull
    @Min(1)
    @Max(99999999)
    public Integer numero;

    public Long fechaEmision;

    public String observacion;

    @NotNull
    @Valid
    public Proveedor proveedor;

    @NotNull
    @Valid
    public Cliente cliente;

    @Valid
    public Firmante firmante;

    @NotNull
    @NotEmpty
    @Valid
    public List<PercepcionRetencionDetalle> detalle;

}

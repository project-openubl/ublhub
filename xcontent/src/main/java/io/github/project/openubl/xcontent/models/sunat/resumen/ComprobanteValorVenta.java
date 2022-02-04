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
package io.github.project.openubl.xcontent.models.sunat.resumen;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ComprobanteValorVenta {

    @NotNull
    @Min(0)
    public BigDecimal importeTotal;

    @Min(0)
    public BigDecimal otrosCargos;

    @Min(0)
    public BigDecimal gravado;

    @Min(0)
    public BigDecimal exonerado;

    @Min(0)
    public BigDecimal inafecto;

    @Min(0)
    public BigDecimal gratuito;

}

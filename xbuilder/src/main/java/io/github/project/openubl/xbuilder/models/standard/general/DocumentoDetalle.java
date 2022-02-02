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
package io.github.project.openubl.xbuilder.models.standard.general;

import io.github.project.openubl.xcontent.catalogs.Catalog7;
import io.github.project.openubl.xcontent.catalogs.validation.CatalogConstraint;
import io.github.project.openubl.xbuilder.validation.DocumentLineInputModel_CantidadValidaICBConstraint;
import io.github.project.openubl.xbuilder.validation.DocumentLineInputModel_CantidadValidaICBGroupValidation;
import io.github.project.openubl.xbuilder.validation.DocumentLineInputModel_PrecioConstraint;
import io.github.project.openubl.xbuilder.validation.HighLevelGroupValidation;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@DocumentLineInputModel_PrecioConstraint(groups = HighLevelGroupValidation.class)
@DocumentLineInputModel_CantidadValidaICBConstraint(groups = DocumentLineInputModel_CantidadValidaICBGroupValidation.class)
public class DocumentoDetalle {

    @NotNull
    @NotBlank
    public String descripcion;

    public String unidadMedida;

    @NotNull
    @Positive
    @Digits(integer = 100, fraction = 3)
    public BigDecimal cantidad;

    /**
     * Precio sin impuestos
     */
    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal precioUnitario;

    /**
     * Precio con impuestos
     */
    @Positive
    @Digits(integer = 100, fraction = 2)
    public BigDecimal precioConIgv;

    @CatalogConstraint(value = Catalog7.class)
    public String tipoIgv;

    public boolean icb;

}


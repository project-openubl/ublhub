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

import io.github.project.openubl.xbuilder.validation.CuotaDePagoInputModel_MontoPorcentajeConstraint;
import io.github.project.openubl.xbuilder.validation.HighLevelGroupValidation;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@CuotaDePagoInputModel_MontoPorcentajeConstraint(groups = HighLevelGroupValidation.class)
public class CuotaDePago {

    @Positive
    public BigDecimal monto;

    @Positive
    @Max(100)
    public BigDecimal porcentaje;

    @NotNull
    public Long fechaPago;

}

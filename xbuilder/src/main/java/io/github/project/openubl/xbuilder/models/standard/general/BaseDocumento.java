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

import io.github.project.openubl.xbuilder.models.common.Cliente;
import io.github.project.openubl.xbuilder.models.common.Firmante;
import io.github.project.openubl.xbuilder.models.common.Proveedor;
import io.github.project.openubl.xbuilder.validation.DocumentInputModel_PuedeCrearComprobanteConSerieFConstraint;
import io.github.project.openubl.xbuilder.validation.DocumentInputModel_PuedeCrearComprobanteConSerieFGroupValidation;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@DocumentInputModel_PuedeCrearComprobanteConSerieFConstraint(groups = DocumentInputModel_PuedeCrearComprobanteConSerieFGroupValidation.class)
public abstract class BaseDocumento {

    @NotNull
    @NotBlank
    @Pattern(regexp = "^[F|f|B|b].*$")
    @Size(min = 4, max = 4)
    public String serie;

    @NotNull
    @Min(1)
    @Max(99999999)
    public Integer numero;

    public Long fechaEmision;

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
    public List<GuiaRemisionRelacionada> guiasRemisionRelacionadas;

}

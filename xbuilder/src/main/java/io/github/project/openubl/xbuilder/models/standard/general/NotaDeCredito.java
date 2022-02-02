/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * <p>
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.eclipse.org/legal/epl-2.0/
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xbuilder.models.standard.general;

import io.github.project.openubl.xcontent.catalogs.Catalog9;
import io.github.project.openubl.xcontent.catalogs.validation.CatalogConstraint;
import io.github.project.openubl.xbuilder.validation.CuotaDePagoInputModel_Porcentaje100CollectionConstraint;
import io.github.project.openubl.xbuilder.validation.HighLevelGroupValidation;

import javax.validation.Valid;
import java.util.List;

public class NotaDeCredito extends BaseDocumentoNota {

    @CatalogConstraint(value = Catalog9.class)
    public String tipoNota;

    @Valid
    @CuotaDePagoInputModel_Porcentaje100CollectionConstraint(groups = HighLevelGroupValidation.class)
    public List<CuotaDePago> cuotasDePago;

    @Valid
    public List<DocumentoTributarioRelacionado_CreditNote> otrosDocumentosTributariosRelacionados;

}

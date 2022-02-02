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
package io.github.project.openubl.xbuilder.validation;

import io.github.project.openubl.xcontent.catalogs.Catalog;
import io.github.project.openubl.xcontent.catalogs.Catalog6;
import io.github.project.openubl.xbuilder.models.standard.general.BaseDocumento;
import io.github.project.openubl.xbuilder.utils.UBLRegex;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DocumentInputModel_PuedeCrearComprobanteConSerieFValidator implements ConstraintValidator<DocumentInputModel_PuedeCrearComprobanteConSerieFConstraint, BaseDocumento> {

    public static final String message = "Comprobantes que empiecen con serie 'F' sólo pueden ser creados por proveedores con RUC";

    @Override
    public void initialize(DocumentInputModel_PuedeCrearComprobanteConSerieFConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(BaseDocumento value, ConstraintValidatorContext context) {
        if (value.serie == null || value.cliente == null || value.cliente.tipoDocumentoIdentidad == null) {
            throw new IllegalStateException("Values needed for validation are null. Make sure you call Default.clas validation group before calling this validator");
        }

        String serie = value.serie;
        Catalog6 catalog6 = Catalog.valueOfCode(Catalog6.class, value.cliente.tipoDocumentoIdentidad)
                .orElseThrow(Catalog.invalidCatalogValue);

        boolean isInvalid = UBLRegex.FACTURA_SERIE_REGEX.matcher(serie).find() && !catalog6.equals(Catalog6.RUC);

        if (isInvalid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
        }

        return !isInvalid;
    }

}

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

import io.github.project.openubl.xbuilder.models.standard.general.DocumentoDetalle;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DocumentLineInputModel_PrecioValidator implements ConstraintValidator<DocumentLineInputModel_PrecioConstraint, DocumentoDetalle> {

    public static final String message = "Se requiere precioUnitario o precioConIgv";

    @Override
    public void initialize(DocumentLineInputModel_PrecioConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(DocumentoDetalle value, ConstraintValidatorContext context) {
        boolean isValid = value.precioUnitario != null || value.precioConIgv != null;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
        }

        return isValid;
    }

}

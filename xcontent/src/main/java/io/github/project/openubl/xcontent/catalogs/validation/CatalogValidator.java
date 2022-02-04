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
package io.github.project.openubl.xcontent.catalogs.validation;

import io.github.project.openubl.xcontent.catalogs.Catalog;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CatalogValidator implements ConstraintValidator<CatalogConstraint, String> {

    protected Class<? extends Enum<? extends Catalog>> catalog;

    @Override
    public void initialize(CatalogConstraint catalogConstraint) {
        catalog = catalogConstraint.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null values are valid
        if (value == null) {
            return true;
        }

        Enum<? extends Catalog>[] enumConstants = catalog.getEnumConstants();
        List<String> validValues = Arrays.stream(enumConstants).flatMap(f -> {
            Catalog catalog = (Catalog) f;
            return Stream.of(f.toString(), catalog.getCode());
        }).collect(Collectors.toList());

        boolean isValid = validValues.stream().anyMatch(p -> p.equalsIgnoreCase(value));
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Possible values: " + String.join(", ", validValues))
                    .addConstraintViolation();
        }

        return isValid;
    }
}

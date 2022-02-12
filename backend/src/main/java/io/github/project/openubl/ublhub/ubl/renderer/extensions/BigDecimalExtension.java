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
package io.github.project.openubl.ublhub.ubl.renderer.extensions;

import io.quarkus.qute.TemplateExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

@TemplateExtension
public class BigDecimalExtension {

    public static BigDecimal percentage(BigDecimal value) {
        return value.multiply(new BigDecimal("100"));
    }

    public static BigDecimal currency(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

}

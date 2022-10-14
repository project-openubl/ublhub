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
package io.github.project.openubl.ublhub.keys.component.utils;

import io.github.project.openubl.ublhub.keys.qualifiers.ComponentProviderType;

import javax.enterprise.util.AnnotationLiteral;

public class ComponentProviderLiteral extends AnnotationLiteral<ComponentProviderType> implements ComponentProviderType {

    private final Class<?> providerType;

    public ComponentProviderLiteral(Class<?> providerType) {
        this.providerType = providerType;
    }

    @Override
    public Class<?> providerType() {
        return providerType;
    }
}
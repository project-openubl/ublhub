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
package io.github.project.openubl.ublhub.documents.idgenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.lang.annotation.Annotation;

@ApplicationScoped
public class IDGeneratorManager {

    @Inject
    @Any
    Instance<IDGenerator> idGenerators;

    public IDGenerator selectIDGenerator(IDGeneratorType providerType) {
        Annotation annotation = new IDGeneratorProviderLiteral(providerType);
        return idGenerators.select(annotation).get();
    }

}

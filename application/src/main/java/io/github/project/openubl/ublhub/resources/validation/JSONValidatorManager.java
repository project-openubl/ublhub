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
package io.github.project.openubl.ublhub.resources.validation;

import io.github.project.openubl.ublhub.dto.DocumentInputDto;
import io.vertx.core.json.JsonObject;
import org.apache.camel.ProducerTemplate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class JSONValidatorManager {

    @Inject
    ProducerTemplate producerTemplate;

    public Boolean validateJsonObject(JsonObject jsonObject) {
        return producerTemplate.requestBody("direct:validate-json-document", jsonObject.toString(), Boolean.class);
    }

    public DocumentInputDto getDocumentInputDtoFromJsonObject(JsonObject json) {
        JsonObject spec = json.getJsonObject("spec");

        JsonObject document = spec.getJsonObject("document");
        spec.remove("document");

        DocumentInputDto dto = json.mapTo(DocumentInputDto.class);
        dto.getSpec().setDocument(document);

        return dto;
    }
}

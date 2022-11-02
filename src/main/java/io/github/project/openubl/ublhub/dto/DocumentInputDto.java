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
package io.github.project.openubl.ublhub.dto;

import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.crypto.Algorithm;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class DocumentInputDto {

    @Schema(required = true)
    @NotNull
    private Kind kind;

    @Schema(nullable = true)
    @Valid
    private DocumentInputDto.Metadata metadata;

    @Schema(required = true)
    @Valid
    @NotNull
    private DocumentInputDto.Spec spec;

    public enum Kind {
        Invoice,
        CreditNote,
        DebitNote;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class Metadata {
        @ArraySchema
        private List<String> labels;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class Spec {
        @Schema(description = "ID Generator config", nullable = true)
        @Valid
        @NotNull
        private ID id;

        @Schema(description = "XML Signature config", nullable = true)
        @Valid
        private Signature signature;

        @Schema(description = "Document config", required = true)
        private JsonObject document;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class ID {
        @Schema(required = true)
        @NotNull
        private IDGeneratorType type;
        private Map<String, String> config;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class Signature {
        @Schema(required = true, allowableValues = {Algorithm.RS256, Algorithm.RS384, Algorithm.RS512})
        @NotNull
        private String algorithm;
    }
}

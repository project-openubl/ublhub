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

import io.github.project.openubl.ublhub.models.JobPhaseType;
import io.github.project.openubl.ublhub.models.JobRecoveryActionType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class DocumentDto {

    private Long id;
    private Long created;
    private Long updated;
    private Status status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class Status {
        private boolean inProgress;

        private XMLData xmlData;
        private Sunat sunat;
        private Error error;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class XMLData {
        private String ruc;
        private String serieNumero;
        private String tipoDocumento;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class Sunat {
        private Integer code;
        private String ticket;
        private String status;
        private String description;
        private boolean hasCdr;
        private List<String> notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @RegisterForReflection
    public static class Error {
        private JobPhaseType phase;
        private String description;
        private int recoveryActionCount;
        private JobRecoveryActionType recoveryAction;
    }
}

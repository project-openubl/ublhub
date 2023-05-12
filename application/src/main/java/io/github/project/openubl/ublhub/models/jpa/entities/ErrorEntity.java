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
package io.github.project.openubl.ublhub.models.jpa.entities;

import io.github.project.openubl.ublhub.models.JobPhaseType;
import io.github.project.openubl.ublhub.models.JobRecoveryActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ErrorEntity {

    @Size(max = 255)
    @Column(name = "error_description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "error_phase")
    private JobPhaseType phase;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_recovery_action")
    private JobRecoveryActionType recoveryAction;

    @NotNull
    @Column(name = "error_count")
    private int count;

}

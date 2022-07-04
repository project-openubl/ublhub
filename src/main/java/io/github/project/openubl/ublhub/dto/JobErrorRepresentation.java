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

@RegisterForReflection
public class JobErrorRepresentation {

    private String description;
    private JobPhaseType phase;
    private JobRecoveryActionType recoveryAction;
    private int recoveryActionCount;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JobPhaseType getPhase() {
        return phase;
    }

    public void setPhase(JobPhaseType phase) {
        this.phase = phase;
    }

    public JobRecoveryActionType getRecoveryAction() {
        return recoveryAction;
    }

    public void setRecoveryAction(JobRecoveryActionType recoveryAction) {
        this.recoveryAction = recoveryAction;
    }

    public int getRecoveryActionCount() {
        return recoveryActionCount;
    }

    public void setRecoveryActionCount(int recoveryActionCount) {
        this.recoveryActionCount = recoveryActionCount;
    }
}

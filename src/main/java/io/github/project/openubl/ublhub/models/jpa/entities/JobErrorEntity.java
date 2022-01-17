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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "job_error")
public class JobErrorEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    private String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    private UBLDocumentEntity document;

    @Size(max = 255)
    @Column(name = "description")
    public String description;

    @Size(max = 255)
    @Enumerated(EnumType.STRING)
    @Column(name = "phase")
    public JobPhaseType phase;

    @Size(max = 255)
    @Enumerated(EnumType.STRING)
    @Column(name = "recovery_action")
    public JobRecoveryActionType recoveryAction;

    @NotNull
    @Column(name = "recovery_action_count")
    public int recoveryActionCount;

}

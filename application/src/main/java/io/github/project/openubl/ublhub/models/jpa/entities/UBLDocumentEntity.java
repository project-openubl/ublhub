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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ubl_document")
public class UBLDocumentEntity extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    private Long id;

    @NotNull
    @Column(name = "project")
    private String project;

    @NotNull
    @Size(max = 255)
    @Column(name = "xml_file_id")
    private String xmlFileId;

    @Size(max = 255)
    @Column(name = "cdr_file_id")
    private String cdrFileId;

    @NotNull
    @Type(type = "org.hibernate.type.YesNoType")
    @Column(name = "job_in_progress")
    private boolean jobInProgress;

    @Valid
    @Embedded
    private XMLDataEntity xmlData;

    @Valid
    @Embedded
    private SUNATResponseEntity sunatResponse;

    @Valid
    @Embedded
    private ErrorEntity error;

    @Version
    @Column(name = "version")
    private int version;
}

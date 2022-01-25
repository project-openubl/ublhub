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

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ubl_document")
public class UBLDocumentEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    public String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "namespace_id")
    public NamespaceEntity namespace;

    @NotNull
    @Size(max = 255)
    @Column(name = "xml_file_id")
    public String xmlFileId;

    @Size(max = 255)
    @Column(name = "cdr_file_id")
    public String cdrFileId;

    @NotNull
    @Type(type = "org.hibernate.type.YesNoType")
    @Column(name = "job_in_progress")
    public boolean jobInProgress;

    @Valid
    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public XMLFileContentEntity xmlFileContent;

    @Valid
    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public SUNATResponseEntity sunatResponse;

    @Valid
    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public JobErrorEntity jobError;
}

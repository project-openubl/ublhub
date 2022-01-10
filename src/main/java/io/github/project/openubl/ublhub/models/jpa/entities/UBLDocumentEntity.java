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

import io.github.project.openubl.ublhub.models.ErrorType;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ubl_document")
public class UBLDocumentEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    public String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(foreignKey = @ForeignKey, name = "namespace_id")
    public NamespaceEntity namespace;

    @NotNull
    @Type(type = "org.hibernate.type.YesNoType")
    @Column(name = "in_progress")
    public boolean inProgress;

    @Column(name = "scheduled_delivery")
    @Temporal(TemporalType.TIMESTAMP)
    public Date scheduledDelivery;

    @Type(type = "org.hibernate.type.YesNoType")
    @Column(name = "file_valid")
    public Boolean fileValid;

    @Enumerated(EnumType.STRING)
    @Column(name = "error")
    public ErrorType error;

    @NotNull
    @Column(name = "retries")
    public int retries;

    // XML Content

    @Size(max = 11)
    @Column(name = "ruc")
    public String ruc;

    @Size(max = 50)
    @Column(name = "document_id")
    public String documentID;

    @Size(max = 50)
    @Column(name = "document_type")
    public String documentType;

    @Size(max = 50)
    @Column(name = "voided_line_document_type_code")
    public String voidedLineDocumentTypeCode;

    // Storage

    @NotNull
    @Size(max = 255)
    @Column(name = "storage_file")
    public String storageFile;

    @Size(max = 255)
    @Column(name = "storage_cdr")
    public String storageCdr;

    //

    @Size(max = 50)
    @Column(name = "sunat_ticket")
    public String sunatTicket;

    @Size(max = 50)
    @Column(name = "sunat_status")
    public String sunatStatus;

    @Column(name = "sunat_code")
    public Integer sunatCode;

    @Size(max = 255)
    @Column(name = "sunat_description")
    public String sunatDescription;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "value")
    @CollectionTable(name = "ubl_document_sunat_notes", joinColumns = {@JoinColumn(name = "ubl_document_id")})
    public Set<@NotNull @Size(max = 255) String> sunatNotes = new HashSet<>();

}

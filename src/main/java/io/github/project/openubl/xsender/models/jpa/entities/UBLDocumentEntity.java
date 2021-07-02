/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.models.jpa.entities;

import io.github.project.openubl.xsender.models.ErrorType;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "ubl_document")
public class UBLDocumentEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    public String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "namespace_id")
    public NamespaceEntity namespace;

    @Type(type = "org.hibernate.type.YesNoType")
    @Column(name = "in_progress")
    public boolean inProgress;

    @NotNull
    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdOn;

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

    @Column(name = "ruc")
    public String ruc;

    @Column(name = "document_id")
    public String documentID;

    @Column(name = "document_type")
    public String documentType;

    @Column(name = "voided_line_document_type_code")
    public String voidedLineDocumentTypeCode;

    // Storage

    @NotNull
    @Column(name = "storage_file")
    public String storageFile;

    @Column(name = "storage_cdr")
    public String storageCdr;

    //

    @Column(name = "sunat_ticket")
    public String sunatTicket;

    @Column(name = "sunat_status")
    public String sunatStatus;

    @Column(name = "sunat_code")
    public Integer sunatCode;

    @Column(name = "sunat_description")
    public String sunatDescription;

    @ElementCollection
    @Column(name = "value")
    @CollectionTable(name = "ubl_document_sunat_notes", joinColumns = {@JoinColumn(name = "ubl_document_id")})
    public Set<String> sunatNotes;

    @Version
    @Column(name = "version")
    public int version;
}

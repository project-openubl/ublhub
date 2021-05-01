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

import io.github.project.openubl.xsender.models.EventStatusType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "UBL_DOCUMENT_EVENT")
public class UBLDocumentEventEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "document_id")
    private UBLDocumentEntity ublDocument;

    @NotNull
    @Column(name = "description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatusType status;

    @NotNull
    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UBLDocumentEntity getUblDocument() {
        return ublDocument;
    }

    public void setUblDocument(UBLDocumentEntity ublDocument) {
        this.ublDocument = ublDocument;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventStatusType getStatus() {
        return status;
    }

    public void setStatus(EventStatusType STATUS) {
        this.status = STATUS;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public static final class Builder {
        private String id;
        private UBLDocumentEntity ublDocument;
        private String description;
        private EventStatusType status;
        private Date createdOn;

        private Builder() {
        }

        public static Builder anUBLDocumentEventEntity() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withUblDocument(UBLDocumentEntity ublDocument) {
            this.ublDocument = ublDocument;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withStatus(EventStatusType status) {
            this.status = status;
            return this;
        }

        public Builder withCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public UBLDocumentEventEntity build() {
            UBLDocumentEventEntity uBLDocumentEventEntity = new UBLDocumentEventEntity();
            uBLDocumentEventEntity.setId(id);
            uBLDocumentEventEntity.setUblDocument(ublDocument);
            uBLDocumentEventEntity.setDescription(description);
            uBLDocumentEventEntity.setStatus(status);
            uBLDocumentEventEntity.setCreatedOn(createdOn);
            return uBLDocumentEventEntity;
        }
    }
}

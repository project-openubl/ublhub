/**
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
package io.github.project.openubl.xsender.core.models.jpa.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "UBL_DOCUMENT")
public class SendEventEntity extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "DOCUMENT_ID")
    private UBLDocumentEntity document;

    @NotNull
    @Column(name = "URL")
    private String url;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @NotNull
    @Column(name = "SUCCESSFUL")
    private boolean successful;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UBLDocumentEntity getDocument() {
        return document;
    }

    public void setDocument(UBLDocumentEntity document) {
        this.document = document;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public static final class Builder {
        private String id;
        private UBLDocumentEntity document;
        private String url;
        private Date createdOn;
        private boolean successful;

        private Builder() {
        }

        public static Builder aSendEventEntity() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withDocument(UBLDocumentEntity document) {
            this.document = document;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder withSuccessful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public SendEventEntity build() {
            SendEventEntity sendEventEntity = new SendEventEntity();
            sendEventEntity.setId(id);
            sendEventEntity.setDocument(document);
            sendEventEntity.setUrl(url);
            sendEventEntity.setCreatedOn(createdOn);
            sendEventEntity.setSuccessful(successful);
            return sendEventEntity;
        }
    }
}

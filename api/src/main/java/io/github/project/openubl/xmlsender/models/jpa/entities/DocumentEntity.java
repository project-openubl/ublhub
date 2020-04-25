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
package io.github.project.openubl.xmlsender.models.jpa.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.github.project.openubl.xmlsender.models.DocumentType;
import io.github.project.openubl.xmlsender.models.DeliveryStatusType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "DOCUMENT")
@Cacheable
public class DocumentEntity extends PanacheEntity {

    @NotNull
    @Column(name = "ruc")
    public String ruc;

    @NotNull
    @Column(name = "document_id")
    public String documentID;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    public DocumentType documentType;

    @NotNull
    @Column(name = "filename_without_extension")
    public String filenameWithoutExtension;

    @NotNull
    @Column(name = "file_id")
    public String fileID;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    public DeliveryStatusType deliveryStatus;

    @NotNull
    @Column(name = "delivery_url")
    public String deliveryURL;

    @Column(name = "crd_id")
    public String cdrID;

    @Column(name = "sunat_username")
    public String sunatUsername;

    @Column(name = "sunat_password")
    public String sunatPassword;

    @Column(name = "sunat_ticket")
    public String sunatTicket;

    @Column(name = "sunat_status")
    public String sunatStatus;

    @Column(name = "sunat_code")
    public Integer sunatCode;

    @Column(name = "sunat_description")
    public String sunatDescription;

    @Column(name = "custom_id")
    public String customId;

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getFilenameWithoutExtension() {
        return filenameWithoutExtension;
    }

    public void setFilenameWithoutExtension(String filenameWithoutExtension) {
        this.filenameWithoutExtension = filenameWithoutExtension;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public DeliveryStatusType getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatusType deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeliveryURL() {
        return deliveryURL;
    }

    public void setDeliveryURL(String deliveryURL) {
        this.deliveryURL = deliveryURL;
    }

    public String getCdrID() {
        return cdrID;
    }

    public void setCdrID(String cdrID) {
        this.cdrID = cdrID;
    }

    public String getSunatUsername() {
        return sunatUsername;
    }

    public void setSunatUsername(String sunatUsername) {
        this.sunatUsername = sunatUsername;
    }

    public String getSunatPassword() {
        return sunatPassword;
    }

    public void setSunatPassword(String sunatPassword) {
        this.sunatPassword = sunatPassword;
    }

    public String getSunatTicket() {
        return sunatTicket;
    }

    public void setSunatTicket(String sunatTicket) {
        this.sunatTicket = sunatTicket;
    }

    public String getSunatStatus() {
        return sunatStatus;
    }

    public void setSunatStatus(String sunatStatus) {
        this.sunatStatus = sunatStatus;
    }

    public Integer getSunatCode() {
        return sunatCode;
    }

    public void setSunatCode(Integer sunatCode) {
        this.sunatCode = sunatCode;
    }

    public String getSunatDescription() {
        return sunatDescription;
    }

    public void setSunatDescription(String sunatDescription) {
        this.sunatDescription = sunatDescription;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }
    
    public static final class Builder {
        public String ruc;
        public String documentID;
        public DocumentType documentType;
        public String filenameWithoutExtension;
        public String fileID;
        public DeliveryStatusType deliveryStatus;
        public String deliveryURL;
        public String cdrID;
        public String sunatUsername;
        public String sunatPassword;
        public String sunatTicket;
        public String sunatStatus;
        public Integer sunatCode;
        public String sunatDescription;
        public String customId;

        private Builder() {
        }

        public static Builder aDocumentEntity() {
            return new Builder();
        }

        public Builder withRuc(String ruc) {
            this.ruc = ruc;
            return this;
        }

        public Builder withDocumentID(String documentID) {
            this.documentID = documentID;
            return this;
        }

        public Builder withDocumentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder withFilenameWithoutExtension(String filenameWithoutExtension) {
            this.filenameWithoutExtension = filenameWithoutExtension;
            return this;
        }

        public Builder withFileID(String fileID) {
            this.fileID = fileID;
            return this;
        }

        public Builder withDeliveryStatus(DeliveryStatusType deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public Builder withDeliveryURL(String deliveryURL) {
            this.deliveryURL = deliveryURL;
            return this;
        }

        public Builder withCdrID(String cdrID) {
            this.cdrID = cdrID;
            return this;
        }

        public Builder withSunatUsername(String sunatUsername) {
            this.sunatUsername = sunatUsername;
            return this;
        }

        public Builder withSunatPassword(String sunatPassword) {
            this.sunatPassword = sunatPassword;
            return this;
        }

        public Builder withSunatTicket(String sunatTicket) {
            this.sunatTicket = sunatTicket;
            return this;
        }

        public Builder withSunatStatus(String sunatStatus) {
            this.sunatStatus = sunatStatus;
            return this;
        }

        public Builder withSunatCode(Integer sunatCode) {
            this.sunatCode = sunatCode;
            return this;
        }

        public Builder withSunatDescription(String sunatDescription) {
            this.sunatDescription = sunatDescription;
            return this;
        }

        public Builder withCustomId(String customId) {
            this.customId = customId;
            return this;
        }

        public DocumentEntity build() {
            DocumentEntity documentEntity = new DocumentEntity();
            documentEntity.deliveryStatus = this.deliveryStatus;
            documentEntity.ruc = this.ruc;
            documentEntity.sunatTicket = this.sunatTicket;
            documentEntity.deliveryURL = this.deliveryURL;
            documentEntity.documentType = this.documentType;
            documentEntity.sunatDescription = this.sunatDescription;
            documentEntity.customId = this.customId;
            documentEntity.fileID = this.fileID;
            documentEntity.cdrID = this.cdrID;
            documentEntity.documentID = this.documentID;
            documentEntity.sunatStatus = this.sunatStatus;
            documentEntity.sunatCode = this.sunatCode;
            documentEntity.filenameWithoutExtension = this.filenameWithoutExtension;
            documentEntity.sunatUsername = this.sunatUsername;
            documentEntity.sunatPassword = this.sunatPassword;
            return documentEntity;
        }
    }
}

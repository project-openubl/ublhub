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
import io.github.project.openubl.xmlsender.models.FileDeliveryStatusType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Cacheable
public class FileDeliveryEntity extends PanacheEntity {

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
    @Column(name = "filename")
    public String filename;

    @NotNull
    @Column(name = "file_id")
    public String fileID;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    public FileDeliveryStatusType deliveryStatus;

    @NotNull
    @Column(name = "server_url")
    public String serverUrl;

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

    public static final class Builder {
        public String ruc;
        public String documentID;
        public DocumentType documentType;
        public String filename;
        public String fileID;
        public FileDeliveryStatusType deliveryStatus;
        public String serverUrl;
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

        public static Builder aFileDeliveryEntity() {
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

        public Builder withFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder withFileID(String fileID) {
            this.fileID = fileID;
            return this;
        }

        public Builder withDeliveryStatus(FileDeliveryStatusType deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public Builder withServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
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

        public FileDeliveryEntity build() {
            FileDeliveryEntity fileDeliveryEntity = new FileDeliveryEntity();
            fileDeliveryEntity.sunatPassword = this.sunatPassword;
            fileDeliveryEntity.sunatStatus = this.sunatStatus;
            fileDeliveryEntity.fileID = this.fileID;
            fileDeliveryEntity.serverUrl = this.serverUrl;
            fileDeliveryEntity.ruc = this.ruc;
            fileDeliveryEntity.sunatUsername = this.sunatUsername;
            fileDeliveryEntity.filename = this.filename;
            fileDeliveryEntity.sunatDescription = this.sunatDescription;
            fileDeliveryEntity.sunatCode = this.sunatCode;
            fileDeliveryEntity.documentID = this.documentID;
            fileDeliveryEntity.cdrID = this.cdrID;
            fileDeliveryEntity.customId = this.customId;
            fileDeliveryEntity.documentType = this.documentType;
            fileDeliveryEntity.deliveryStatus = this.deliveryStatus;
            fileDeliveryEntity.sunatTicket = this.sunatTicket;
            return fileDeliveryEntity;
        }
    }
}

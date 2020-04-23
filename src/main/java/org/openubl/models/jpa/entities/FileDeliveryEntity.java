package org.openubl.models.jpa.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.openubl.models.DocumentType;
import org.openubl.models.FileDeliveryStatusType;

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
            fileDeliveryEntity.documentType = this.documentType;
            fileDeliveryEntity.sunatStatus = this.sunatStatus;
            fileDeliveryEntity.filename = this.filename;
            fileDeliveryEntity.cdrID = this.cdrID;
            fileDeliveryEntity.serverUrl = this.serverUrl;
            fileDeliveryEntity.deliveryStatus = this.deliveryStatus;
            fileDeliveryEntity.sunatTicket = this.sunatTicket;
            fileDeliveryEntity.customId = this.customId;
            fileDeliveryEntity.ruc = this.ruc;
            fileDeliveryEntity.sunatDescription = this.sunatDescription;
            fileDeliveryEntity.documentID = this.documentID;
            fileDeliveryEntity.fileID = this.fileID;
            fileDeliveryEntity.sunatCode = this.sunatCode;
            return fileDeliveryEntity;
        }
    }
}

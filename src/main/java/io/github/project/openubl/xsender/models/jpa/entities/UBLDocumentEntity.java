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
package io.github.project.openubl.xsender.models.jpa.entities;

import io.github.project.openubl.xmlsenderws.webservices.models.DeliveryURLType;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "UBL_DOCUMENT")
public class UBLDocumentEntity extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "COMPANY_ID")
    private CompanyEntity company;

    @NotNull
    @Column(name = "RUC")
    private String ruc;

    @NotNull
    @Column(name = "DOCUMENT_ID")
    private String documentID;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_TYPE")
    private DocumentType documentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "DELIVERY_TYPE")
    private DeliveryURLType deliveryType;

    @NotNull
    @Column(name = "FILENAME")
    private String filename;

    @NotNull
    @Column(name = "STORAGE_FILE")
    private String storageFile;

    @Column(name = "STORAGE_CDR")
    private String storageCdr;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "DELIVERY_STATUS")
    private DeliveryStatusType deliveryStatus;

    @Column(name = "SUNAT_TICKET")
    private String sunatTicket;

    @Column(name = "SUNAT_STATUS")
    private String sunatStatus;

    @Column(name = "SUNAT_CODE")
    private Integer sunatCode;

    @Column(name = "SUNAT_DESCRIPTION")
    private String sunatDescription;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }

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

    public DeliveryURLType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryURLType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(String storageFile) {
        this.storageFile = storageFile;
    }

    public String getStorageCdr() {
        return storageCdr;
    }

    public void setStorageCdr(String storageCdr) {
        this.storageCdr = storageCdr;
    }

    public DeliveryStatusType getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatusType deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UBLDocumentEntity that = (UBLDocumentEntity) o;
        return company.equals(that.company) &&
                documentID.equals(that.documentID) &&
                documentType == that.documentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, documentID, documentType);
    }

    public static final class Builder {
        private String id;
        private CompanyEntity company;
        private String ruc;
        private String documentID;
        private DocumentType documentType;
        private DeliveryURLType deliveryType;
        private String filename;
        private String storageFile;
        private String storageCdr;
        private DeliveryStatusType deliveryStatus;
        private String sunatTicket;
        private String sunatStatus;
        private Integer sunatCode;
        private String sunatDescription;

        private Builder() {
        }

        public static Builder anUBLDocumentEntity() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCompany(CompanyEntity company) {
            this.company = company;
            return this;
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

        public Builder withDeliveryType(DeliveryURLType deliveryType) {
            this.deliveryType = deliveryType;
            return this;
        }

        public Builder withFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder withStorageFile(String storageFile) {
            this.storageFile = storageFile;
            return this;
        }

        public Builder withStorageCdr(String storageCdr) {
            this.storageCdr = storageCdr;
            return this;
        }

        public Builder withDeliveryStatus(DeliveryStatusType deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
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

        public UBLDocumentEntity build() {
            UBLDocumentEntity uBLDocumentEntity = new UBLDocumentEntity();
            uBLDocumentEntity.setId(id);
            uBLDocumentEntity.setCompany(company);
            uBLDocumentEntity.setRuc(ruc);
            uBLDocumentEntity.setDocumentID(documentID);
            uBLDocumentEntity.setDocumentType(documentType);
            uBLDocumentEntity.setDeliveryType(deliveryType);
            uBLDocumentEntity.setFilename(filename);
            uBLDocumentEntity.setStorageFile(storageFile);
            uBLDocumentEntity.setStorageCdr(storageCdr);
            uBLDocumentEntity.setDeliveryStatus(deliveryStatus);
            uBLDocumentEntity.setSunatTicket(sunatTicket);
            uBLDocumentEntity.setSunatStatus(sunatStatus);
            uBLDocumentEntity.setSunatCode(sunatCode);
            uBLDocumentEntity.setSunatDescription(sunatDescription);
            return uBLDocumentEntity;
        }
    }
}

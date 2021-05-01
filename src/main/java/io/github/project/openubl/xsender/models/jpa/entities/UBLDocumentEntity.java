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

import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "UBL_DOCUMENT")
public class UBLDocumentEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "company_id")
    private CompanyEntity company;

    @NotNull
    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Type(type = "org.hibernate.type.YesNoType")
    @Column(name = "valid")
    private Boolean valid;

    @Column(name = "validation_error")
    private String validationError;

    @NotNull
    @Column(name = "retries")
    private int retries;

    @Column(name = "will_retry_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date willRetryOn;

    // XML Content

    @Column(name = "ruc")
    private String ruc;

    @Column(name = "document_id")
    private String documentID;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "voided_line_document_type_code")
    private String voidedLineDocumentTypeCode;

    // Storage

    @NotNull
    @Column(name = "storage_file")
    private String storageFile;

    @Column(name = "storage_cdr")
    private String storageCdr;

    //

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    private DeliveryStatusType deliveryStatus;

    @Column(name = "sunat_ticket")
    private String sunatTicket;

    @Column(name = "sunat_status")
    private String sunatStatus;

    @Column(name = "sunat_code")
    private Integer sunatCode;

    @Column(name = "sunat_description")
    private String sunatDescription;

    @ElementCollection
    @Column(name="value")
    @CollectionTable(name = "ubl_document_sunat_notes", joinColumns={ @JoinColumn(name="ubl_document_id") })
    private Set<String> sunatNotes;

    //

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ublDocument")
    private List<UBLDocumentEventEntity> sunatEvents = new ArrayList<>();

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

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getValidationError() {
        return validationError;
    }

    public void setValidationError(String validationError) {
        this.validationError = validationError;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public Date getWillRetryOn() {
        return willRetryOn;
    }

    public void setWillRetryOn(Date willRetryOn) {
        this.willRetryOn = willRetryOn;
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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getVoidedLineDocumentTypeCode() {
        return voidedLineDocumentTypeCode;
    }

    public void setVoidedLineDocumentTypeCode(String voidedLineDocumentTypeCode) {
        this.voidedLineDocumentTypeCode = voidedLineDocumentTypeCode;
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

    public List<UBLDocumentEventEntity> getSunatEvents() {
        return sunatEvents;
    }

    public void setSunatEvents(List<UBLDocumentEventEntity> sunatEvents) {
        this.sunatEvents = sunatEvents;
    }

    public Set<String> getSunatNotes() {
        return sunatNotes;
    }

    public void setSunatNotes(Set<String> sunatNotes) {
        this.sunatNotes = sunatNotes;
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
        private Date createdOn;
        private Boolean valid;
        private String validationError;
        private int retries;
        private Date willRetryOn;
        private String ruc;
        private String documentID;
        private String documentType;
        private String voidedLineDocumentTypeCode;
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

        public Builder withCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder withValid(Boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder withValidationError(String validationError) {
            this.validationError = validationError;
            return this;
        }

        public Builder withRetries(int retries) {
            this.retries = retries;
            return this;
        }

        public Builder withWillRetryOn(Date willRetryOn) {
            this.willRetryOn = willRetryOn;
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

        public Builder withDocumentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder withVoidedLineDocumentTypeCode(String voidedLineDocumentTypeCode) {
            this.voidedLineDocumentTypeCode = voidedLineDocumentTypeCode;
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
            uBLDocumentEntity.setCreatedOn(createdOn);
            uBLDocumentEntity.setValid(valid);
            uBLDocumentEntity.setValidationError(validationError);
            uBLDocumentEntity.setRetries(retries);
            uBLDocumentEntity.setWillRetryOn(willRetryOn);
            uBLDocumentEntity.setRuc(ruc);
            uBLDocumentEntity.setDocumentID(documentID);
            uBLDocumentEntity.setDocumentType(documentType);
            uBLDocumentEntity.setVoidedLineDocumentTypeCode(voidedLineDocumentTypeCode);
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

package org.openubl.xml;

import org.openubl.providers.DocumentType;

public class SunatDocumentModel {

    private DocumentType documentType;
    private String documentID;
    private String ruc;

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public static final class Builder {
        private DocumentType documentType;
        private String documentID;
        private String ruc;

        private Builder() {
        }

        public static Builder aSunatDocumentModel() {
            return new Builder();
        }

        public Builder withDocumentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder withDocumentID(String documentID) {
            this.documentID = documentID;
            return this;
        }

        public Builder withRuc(String ruc) {
            this.ruc = ruc;
            return this;
        }

        public SunatDocumentModel build() {
            SunatDocumentModel sunatDocumentModel = new SunatDocumentModel();
            sunatDocumentModel.setDocumentType(documentType);
            sunatDocumentModel.setDocumentID(documentID);
            sunatDocumentModel.setRuc(ruc);
            return sunatDocumentModel;
        }
    }
}

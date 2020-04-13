package org.openubl.xml.ubl;

public class XmlContentModel {

    private String documentType;
    private String documentID;
    private String ruc;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
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
        private String documentType;
        private String documentID;
        private String ruc;

        private Builder() {
        }

        public static Builder aSunatDocumentModel() {
            return new Builder();
        }

        public Builder withDocumentType(String documentType) {
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

        public XmlContentModel build() {
            XmlContentModel xmlContentModel = new XmlContentModel();
            xmlContentModel.setDocumentType(documentType);
            xmlContentModel.setDocumentID(documentID);
            xmlContentModel.setRuc(ruc);
            return xmlContentModel;
        }
    }
}

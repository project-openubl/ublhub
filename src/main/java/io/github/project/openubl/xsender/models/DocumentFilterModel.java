package io.github.project.openubl.xsender.models;

public class DocumentFilterModel {

    private String ruc;
    private String documentType;

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public static final class DocumentFilterModelBuilder {
        private String ruc;
        private String documentType;

        private DocumentFilterModelBuilder() {
        }

        public static DocumentFilterModelBuilder aDocumentFilterModel() {
            return new DocumentFilterModelBuilder();
        }

        public DocumentFilterModelBuilder withRuc(String ruc) {
            this.ruc = ruc;
            return this;
        }

        public DocumentFilterModelBuilder withDocumentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public DocumentFilterModel build() {
            DocumentFilterModel documentFilterModel = new DocumentFilterModel();
            documentFilterModel.setRuc(ruc);
            documentFilterModel.setDocumentType(documentType);
            return documentFilterModel;
        }
    }
}

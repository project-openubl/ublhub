package org.openubl.providers;

import java.util.Optional;
import java.util.stream.Stream;

public enum DocumentType {
    INVOICE("Invoice"),
    CREDIT_NOTE("CreditNote"),
    DEBIT_NOTE("DebitNote"),
    VOIDED_DOCUMENT("VoidedDocuments"),
    SUMMARY_DOCUMENT("SummaryDocuments");

    private String documentType;

    DocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public static Optional<DocumentType> valueFromDocumentType(String documentType) {
        return Stream.of(DocumentType.values())
                .filter(p -> p.getDocumentType().equals(documentType))
                .findFirst();
    }
}

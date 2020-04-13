package org.openubl.exceptions;

public class UnsupportedDocumentTypeException extends Exception {
    public UnsupportedDocumentTypeException(Exception e) {
        super(e);
    }

    public UnsupportedDocumentTypeException(String messasge) {
        super(messasge);
    }
}

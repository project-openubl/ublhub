package org.openubl.exceptions;

public class WSNotAvailableException extends Exception {
    public WSNotAvailableException(Exception e) {
        super(e);
    }

    public WSNotAvailableException(String messasge) {
        super(messasge);
    }
}

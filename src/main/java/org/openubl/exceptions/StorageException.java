package org.openubl.exceptions;

public class StorageException extends Exception {
    public StorageException(Exception e) {
        super(e);
    }

    public StorageException(String message) {
        super(message);
    }
}

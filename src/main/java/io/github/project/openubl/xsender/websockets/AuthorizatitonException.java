package io.github.project.openubl.xsender.websockets;

public class AuthorizatitonException extends Exception {
    public AuthorizatitonException() {
    }

    public AuthorizatitonException(String message) {
        super(message);
    }

    public AuthorizatitonException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizatitonException(Throwable cause) {
        super(cause);
    }

    public AuthorizatitonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

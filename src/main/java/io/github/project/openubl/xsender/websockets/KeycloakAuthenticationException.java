package io.github.project.openubl.xsender.websockets;

public class KeycloakAuthenticationException extends Exception {
    public KeycloakAuthenticationException() {
    }

    public KeycloakAuthenticationException(String message) {
        super(message);
    }

    public KeycloakAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakAuthenticationException(Throwable cause) {
        super(cause);
    }

    public KeycloakAuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

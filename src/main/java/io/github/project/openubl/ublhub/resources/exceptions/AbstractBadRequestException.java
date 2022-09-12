package io.github.project.openubl.ublhub.resources.exceptions;

public abstract class AbstractBadRequestException extends Exception {
    public AbstractBadRequestException(String e) {
        super(e);
    }
}

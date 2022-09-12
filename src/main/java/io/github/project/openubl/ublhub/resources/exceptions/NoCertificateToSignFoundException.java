package io.github.project.openubl.ublhub.resources.exceptions;

public class NoCertificateToSignFoundException extends AbstractBadRequestException {
    public NoCertificateToSignFoundException(String e) {
        super(e);
    }
}

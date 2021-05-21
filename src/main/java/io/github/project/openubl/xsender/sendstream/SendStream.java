package io.github.project.openubl.xsender.sendstream;

import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;

public interface SendStream {

    void send(UBLDocumentEntity documentEntity);

}

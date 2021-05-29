package io.github.project.openubl.xsender.sender.amqp;

import io.github.project.openubl.xsender.events.DocumentEvent;
import io.github.project.openubl.xsender.events.DocumentEventBroadcaster;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.sender.SenderManager;
import io.github.project.openubl.xsender.sender.SenderProvider;
import io.github.project.openubl.xsender.sender.WSNotAvailableException;
import io.github.project.openubl.xsender.sender.XSenderManager;
import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;

@Transactional
@ApplicationScoped
@SenderProvider(SenderProvider.Type.AMQP)
public class AMQPSenderManager implements SenderManager {

    @Inject
    XSenderManager xSenderManager;

    @Inject
    UBLDocumentRepository documentRepository;

    // AMQP

    @Inject
    @Channel("send-document-sunat-emiter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> documentEmitter;

    @Inject
    @Channel("verify-ticket-sunat-emiter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> ticketEmitter;

    // Events

    @Inject
    DocumentEventBroadcaster documentEventBroadcaster;

    @Override
    public void fireSendDocument(String id) {
        documentEmitter.send(id);
    }

    // AMQP handlers

    @Blocking(ordered = false)
    @Incoming("send-document-sunat-incoming")
    @Outgoing("verify-ticket-sunat")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public String sendFile(String documentId) {
        String ticket = null;
        boolean shouldSendFile = xSenderManager.validateFile(documentId);
        if (shouldSendFile) {
            try {
                ticket = xSenderManager.sendFile(documentId).orElse(null);
            } catch (WSNotAvailableException e) {
                handleRetry(documentId, documentEmitter);
            }
        }

        // Fire event
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        DocumentEvent event = new DocumentEvent(documentId, documentEntity.getNamespace().getId());
        documentEventBroadcaster.fire(event);

        return ticket != null ? documentId : null;
    }

    @Blocking(ordered = false)
    @Incoming("verify-ticket-sunat-incoming")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public void verifyTicket(String documentId) {
        try {
            xSenderManager.checkTicket(documentId);
        } catch (WSNotAvailableException e) {
            handleRetry(documentId, ticketEmitter);
        }

        // Fire event
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        DocumentEvent event = new DocumentEvent(documentId, documentEntity.getNamespace().getId());
        documentEventBroadcaster.fire(event);
    }

    private void handleRetry(String documentId, Emitter<String> emitter) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);

        int retries = documentEntity.getRetries();
        Date date = null;
        if (retries <= 2) {
            retries++;

            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();

            calendar.add(Calendar.MINUTE, (int) Math.pow(5, retries));
            date = calendar.getTime();

            OutgoingAmqpMetadata metadata = OutgoingAmqpMetadata.builder()
                    .withMessageAnnotations("x-opt-delivery-delay", calendar.getTimeInMillis() - currentTime)
                    .build();

            Message<String> message = Message.of(documentId).addMetadata(metadata);
            emitter.send(message);
        } else {
            documentEntity.setError("Número de reintentos de reenvío agotados");
        }

        // Save
        documentEntity.setInProgress(false);
        documentEntity.setRetries(retries);
        documentEntity.setScheduledDelivery(date);
        documentRepository.persist(documentEntity);
    }

}

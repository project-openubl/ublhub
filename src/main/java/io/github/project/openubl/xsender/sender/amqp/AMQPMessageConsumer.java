/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.sender.amqp;

import io.github.project.openubl.xsender.events.DocumentEvent;
import io.github.project.openubl.xsender.events.DocumentEventManager;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.sender.MessageSenderManager;
import io.github.project.openubl.xsender.sender.common.WSException;
import io.github.project.openubl.xsender.sender.common.XSenderManager;
import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.*;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class AMQPMessageConsumer {

    private static final Logger LOG = Logger.getLogger(AMQPMessageConsumer.class);

    @Inject
    UserTransaction transaction;

    @Inject
    FilesManager filesManager;

    @Inject
    XSenderManager xSenderManager;

    @Inject
    MessageSenderManager messageSenderManager;

    @Inject
    DocumentEventManager documentEventManager;

    @Inject
    UBLDocumentRepository documentRepository;

    protected OutgoingAmqpMetadata createScheduledMessage(Date scheduleDelivery) {
        return OutgoingAmqpMetadata.builder()
                .withMessageAnnotations("x-opt-delivery-delay", scheduleDelivery.getTime() - Calendar.getInstance().getTimeInMillis())
                .build();
    }

    @Blocking(ordered = false)
    @Incoming("send-document-sunat-incoming")
    @Outgoing("verify-ticket-sunat")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Message<String> sendFile(Message<String> inMessage) {
        String documentId = inMessage.getPayload();

        UBLDocumentEntity documentEntity;
        NamespaceEntity namespaceEntity;

        try {
            transaction.begin();

            // Fetch document
            documentEntity = documentRepository.findById(documentId);
            namespaceEntity = documentEntity.getNamespace();

            byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());

            // Clear status
            documentEntity.setInProgress(false);
            documentEntity.setError(null);
            documentEntity.setScheduledDelivery(null);

            // Validate and send file
            xSenderManager.validateFileEnrich(documentEntity, file);
            if (documentEntity.getFileValid()) {
                try {
                    xSenderManager.sendFileEnrich(documentEntity, file);
                } catch (WSException e) {
                    handleRetry(documentEntity);
                }
            }

            // Force not to change 'inProgress' if conditions match
            if (documentEntity.getError() == null && documentEntity.getSunatTicket() != null) {
                documentEntity.setInProgress(true);
            }
            documentRepository.persist(documentEntity);

            // Commit transaction
            transaction.commit();
        } catch (NotSupportedException | SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
            LOG.error(e);

            try {
                transaction.rollback();
            } catch (SystemException systemException) {
                LOG.error(systemException);
            }

            inMessage.nack(e);
            return null;
        }

        // Event: document has changed
        DocumentEvent event = new DocumentEvent(documentEntity.getId(), namespaceEntity.getId());
        documentEventManager.fire(event);

        // Result
        if (documentEntity.getError() == null && documentEntity.getSunatTicket() != null) {
            return Message.of(documentEntity.getId())
                    .withAck(inMessage::ack)
                    .withNack(throwable -> messageSenderManager.handleDocumentMessageError(documentId, throwable));
        }
        if (documentEntity.getScheduledDelivery() != null) {
            OutgoingAmqpMetadata outgoingAmqpMetadata = createScheduledMessage(documentEntity.getScheduledDelivery());
            Message<String> scheduledMessage = Message.of(documentEntity.getId())
                    .withAck(() -> CompletableFuture.completedFuture(null))
                    .withNack(throwable -> messageSenderManager.handleDocumentMessageError(inMessage.getPayload(), new Exception("Could not schedule in AMQP")))
                    .withMetadata(Metadata.of(outgoingAmqpMetadata));
            messageSenderManager.sendToDocumentQueue(scheduledMessage);
        }

        inMessage.ack();
        return null;
    }

    @Blocking(ordered = false)
    @Incoming("verify-ticket-sunat-incoming")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> verifyTicket(Message<String> inMessage) {
        String documentId = inMessage.getPayload();

        UBLDocumentEntity documentEntity;
        NamespaceEntity namespaceEntity;

        try {
            transaction.begin();

            // Fetch data
            documentEntity = documentRepository.findById(documentId);
            namespaceEntity = documentEntity.getNamespace();

            // Clear status
            documentEntity.setInProgress(false);
            documentEntity.setError(null);
            documentEntity.setScheduledDelivery(null);

            // Verify ticket
            try {
                xSenderManager.checkTicket(documentEntity);
            } catch (WSException e) {
                handleRetry(documentEntity);
            }

            documentRepository.persist(documentEntity);

            // Commit transaction
            transaction.commit();
        } catch (NotSupportedException | SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
            LOG.error(e);

            try {
                transaction.rollback();
            } catch (SystemException systemException) {
                LOG.error(systemException);
            }

            return inMessage.nack(e);
        }

        // Event: document has changed
        DocumentEvent event = new DocumentEvent(documentEntity.getId(), namespaceEntity.getId());
        documentEventManager.fire(event);

        if (documentEntity.getScheduledDelivery() != null) {
            OutgoingAmqpMetadata outgoingAmqpMetadata = createScheduledMessage(documentEntity.getScheduledDelivery());
            Message<String> scheduledMessage = Message.of(documentEntity.getId())
                    .withNack(throwable -> messageSenderManager.handleDocumentMessageError(documentId, new Exception("Could not schedule in AMQP")))
                    .withMetadata(Metadata.of(outgoingAmqpMetadata));
            messageSenderManager.sendToTicketQueue(scheduledMessage);
        }

        return inMessage.ack();
    }

    private void handleRetry(UBLDocumentEntity documentEntity) {
        int retries = documentEntity.getRetries();
        Date scheduleDelivery = null;
        if (retries <= 2) {
            retries++;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (int) Math.pow(5, retries));
            scheduleDelivery = calendar.getTime();
        } else {
            documentEntity.setError(ErrorType.RETRY_CONSUMED);
        }

        // Result
        documentEntity.setRetries(retries);
        documentEntity.setScheduledDelivery(scheduleDelivery);
    }

}

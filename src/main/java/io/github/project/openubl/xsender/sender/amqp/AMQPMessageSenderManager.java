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
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.sender.MessageSenderManager;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@ApplicationScoped
public class AMQPMessageSenderManager implements MessageSenderManager {

    private static final Logger LOG = Logger.getLogger(AMQPMessageSenderManager.class);

    @Inject
    UserTransaction transaction;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    DocumentEventManager documentEventManager;

    @Inject
    @Channel("send-document-sunat-emitter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> documentEmitter;

    @Inject
    @Channel("verify-ticket-sunat-emitter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> ticketEmitter;

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    @Override
    public void sendToDocumentQueue(Message<String> message) {
        documentEmitter.send(message);
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    @Override
    public void sendToTicketQueue(Message<String> message) {
        ticketEmitter.send(message);
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public CompletionStage<Void> handleDocumentMessageError(String documentId, Throwable e) {
        LOG.error(e);
        try {
            transaction.begin();

            UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
            NamespaceEntity namespaceEntity = documentEntity.getNamespace();

            documentEntity.setInProgress(false);
            documentEntity.setError(ErrorType.AMQP_SCHEDULE);
            documentEntity.setScheduledDelivery(null);

            transaction.commit();

            // Document has changed
            DocumentEvent event = new DocumentEvent(documentEntity.getId(), namespaceEntity.getId());
            documentEventManager.fire(event);
        } catch (HeuristicRollbackException | SystemException | HeuristicMixedException | NotSupportedException | RollbackException heuristicRollbackException) {
            LOG.error(e);
            try {
                transaction.rollback();
            } catch (SystemException systemException) {
                LOG.error(systemException);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

}

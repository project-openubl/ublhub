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
package io.github.project.openubl.xsender.scheduler.consumers.amqp;

import io.github.project.openubl.xsender.scheduler.consumers.DocumentUni;
import io.github.project.openubl.xsender.scheduler.consumers.DocumentUniSend;
import io.github.project.openubl.xsender.scheduler.consumers.DocumentUniTicket;
import io.github.project.openubl.xsender.scheduler.consumers.EventManagerUtils;
import io.github.project.openubl.xsender.exceptions.AbstractSendFileException;
import io.github.project.openubl.xsender.exceptions.CheckTicketAtSUNATException;
import io.github.project.openubl.xsender.exceptions.SaveFileException;
import io.github.project.openubl.xsender.exceptions.SendFileToSUNATException;
import io.github.project.openubl.xsender.models.ErrorType;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

@ApplicationScoped
public class AMQPEventManager {

    @Inject
    EventManagerUtils eventManagerUtils;

    @Inject
    @Channel("send-document-sunat-emitter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> documentEmitter;

    @Inject
    @Channel("verify-ticket-sunat-emitter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> ticketEmitter;

    private void handleRetry(DocumentUni document) {
        int retries = document.getRetries();
        Date scheduleDelivery = null;
        if (retries <= 2) {
            retries++;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (int) Math.pow(5, retries));
            scheduleDelivery = calendar.getTime();
        } else {
            document.setError(ErrorType.RETRY_CONSUMED);
        }

        // Result
        document.setRetries(retries);
        document.setScheduledDelivery(scheduleDelivery);
    }

    protected OutgoingAmqpMetadata createScheduledMessage(Date scheduleDelivery) {
        return OutgoingAmqpMetadata.builder()
                .withMessageAnnotations("x-opt-delivery-delay", scheduleDelivery.getTime() - Calendar.getInstance().getTimeInMillis())
                .build();
    }

    @Incoming("send-document-sunat-incoming")
    @Outgoing("verify-ticket-sunat")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    protected Uni<Message<String>> sendFile(Message<String> inMessage) {
        return eventManagerUtils.initDocumentUniSend(inMessage.getPayload())
                // Process file
                .chain(documentUni -> eventManagerUtils.enrichWithFileAsBytes(documentUni)
                        .chain(fileBytes -> eventManagerUtils.enrichWithFileContent(documentUni, fileBytes))
                        .chain(xmlContentModel -> eventManagerUtils.enrichWithWsConfig(documentUni, xmlContentModel))
                        .chain(wsConfig -> eventManagerUtils.enrichWithSendingFile(documentUni, wsConfig, 1))
                        .chain(billServiceModel -> eventManagerUtils.enrichSavingCDRIfExists(documentUni, billServiceModel))

                        .map(unused -> documentUni)

                        .onFailure(throwable -> throwable instanceof AbstractSendFileException)
                        .recoverWithUni(throwable -> {
                            Uni<DocumentUniSend> result = Uni.createFrom().item(documentUni);
                            if (throwable instanceof SendFileToSUNATException) {
                                result = result.invoke(() -> {
                                    handleRetry(documentUni);
                                    if (documentUni.getScheduledDelivery() != null) {
                                        OutgoingAmqpMetadata outgoingAmqpMetadata = createScheduledMessage(documentUni.getScheduledDelivery());
                                        Message<String> scheduledMessage = Message
                                                .of(inMessage.getPayload())
                                                .withMetadata(Metadata.of(outgoingAmqpMetadata));
                                        documentEmitter.send(scheduledMessage);
                                    }
                                });
                            }
                            return result;
                        })
                )
                // Persist changes in DB
                .chain(documentUni -> eventManagerUtils.documentUniToEntity(documentUni)
                        .map(documentEntity -> documentUni)
                )
                // Final decision
                .chain(documentUni -> {
                    Uni<Message<String>> result;
                    if (documentUni.getError() != null) {
                        switch (documentUni.getError()) {
                            case FETCH_FILE:
                                result = Uni.createFrom()
                                        .completionStage(inMessage.nack(new IllegalStateException("Uncontrolled error=" + documentUni.getError())))
                                        .chain(unused -> Uni.createFrom().nullItem());
                                break;
                            case READ_FILE:
                            case UNSUPPORTED_DOCUMENT_TYPE:
                            case COMPANY_NOT_FOUND:
                            case SEND_FILE:
                                result = Uni.createFrom()
                                        .completionStage(inMessage.ack())
                                        .chain(unused -> Uni.createFrom().nullItem());
                                break;
                            case SAVE_CRD_FILE:
                                result = Uni.createFrom()
                                        .completionStage(inMessage.nack(new SaveFileException(ErrorType.SAVE_CRD_FILE.getMessage())))
                                        .chain(unused -> Uni.createFrom().nullItem());
                                break;
                            default:
                                throw new IllegalStateException("Uncontrolled error=" + documentUni.getError());
                        }
                    } else {
                        if (documentUni.getBillServiceModel() != null && documentUni.getBillServiceModel().getTicket() != null) {
                            result = Uni.createFrom().item(Message.of(inMessage.getPayload()));
                        } else {
                            result = Uni.createFrom()
                                    .completionStage(inMessage.ack())
                                    .chain(unused -> Uni.createFrom().nullItem());
                        }
                    }
                    return result;
                });
    }

    @Incoming("verify-ticket-sunat-incoming")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    protected Uni<Void> verifyTicket(Message<String> inMessage) {
        return eventManagerUtils.initDocumentUniTicket(inMessage.getPayload())
                // Process ticket
                .chain(documentUniTicket -> eventManagerUtils.enrichWithWsConfig(documentUniTicket)
                        .chain(() -> eventManagerUtils.enrichWithCheckingTicket(documentUniTicket, 1))
                        .chain(billServiceModel -> eventManagerUtils.enrichSavingCDRIfExists(documentUniTicket, billServiceModel))

                        .map(unused -> documentUniTicket)

                        .onFailure(throwable -> throwable instanceof AbstractSendFileException)
                        .recoverWithUni(throwable -> {
                            Uni<DocumentUniTicket> result = Uni.createFrom().item(documentUniTicket);
                            if (throwable instanceof CheckTicketAtSUNATException) {
                                result = result.invoke(() -> {
                                    handleRetry(documentUniTicket);
                                    if (documentUniTicket.getScheduledDelivery() != null) {
                                        OutgoingAmqpMetadata outgoingAmqpMetadata = createScheduledMessage(documentUniTicket.getScheduledDelivery());
                                        Message<String> scheduledMessage = Message
                                                .of(inMessage.getPayload())
                                                .withMetadata(Metadata.of(outgoingAmqpMetadata));
                                        ticketEmitter.send(scheduledMessage);
                                    }
                                });
                            }
                            return result;
                        })
                )
                // Persist in DB
                .chain(documentUni -> eventManagerUtils.documentUniToEntity(documentUni)
                        .map(documentEntity -> documentUni)
                )
                // Final decision
                .chain(documentCache -> {
                    Uni<Void> result;
                    if (documentCache.getError() != null) {
                        switch (documentCache.getError()) {
                            case READ_FILE:
                            case UNSUPPORTED_DOCUMENT_TYPE:
                            case COMPANY_NOT_FOUND:
                            case SEND_FILE:
                            case CHECK_TICKET:
                                result = Uni.createFrom()
                                        .completionStage(inMessage.ack())
                                        .chain(unused -> Uni.createFrom().nullItem());
                                break;
                            case SAVE_CRD_FILE:
                                result = Uni.createFrom()
                                        .completionStage(inMessage.nack(new SaveFileException(ErrorType.SAVE_CRD_FILE.getMessage())))
                                        .chain(unused -> Uni.createFrom().nullItem());
                                break;
                            default:
                                throw new IllegalStateException("Uncontrolled error=" + documentCache.getError());
                        }
                    } else {
                        result = Uni.createFrom()
                                .completionStage(inMessage.ack())
                                .chain(unused -> Uni.createFrom().nullItem());
                    }
                    return result;
                });
    }

}

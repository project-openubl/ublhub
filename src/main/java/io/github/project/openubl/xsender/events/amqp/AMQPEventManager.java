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
package io.github.project.openubl.xsender.events.amqp;

import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xsender.events.EventManager;
import io.github.project.openubl.xsender.exceptions.*;
import io.github.project.openubl.xsender.files.FilesMutiny;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.sender.XSenderMutiny;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

@ApplicationScoped
public class AMQPEventManager implements EventManager {

    @Inject
    FilesMutiny filesMutiny;

    @Inject
    XSenderMutiny xSenderMutiny;

    @Inject
    UBLDocumentRepository documentRepository;

//    @Inject
//    @Channel("document-event-emitter")
//    @OnOverflow(value = OnOverflow.Strategy.LATEST)
//    Emitter<DocumentEvent> eventEmitter;

    @Inject
    @Channel("send-document-sunat-emitter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> documentEmitter;

//    private CompletionStage<Void> withNack(String documentId) {
//        return Panache
//                .withTransaction(() -> documentRepository
//                        .findById(documentId)
//                        .invoke(documentEntity -> {
//                            documentEntity.inProgress = false;
//                            documentEntity.error = ErrorType.AMQP_SCHEDULE;
//                            documentEntity.scheduledDelivery = null;
//                        })
//                )
//                .chain(documentEntity -> sendDocumentEvent(new DocumentEvent(documentEntity.id, documentEntity.namespace.id)))
//                .subscribeAsCompletionStage();
//    }

    private Uni<XmlContentModel> getXmlContent(UBLDocumentEntity documentEntity) {
        XmlContentModel xmlContentModel = new XmlContentModel();
        xmlContentModel.setRuc(documentEntity.ruc);
        xmlContentModel.setDocumentType(documentEntity.documentType);
        xmlContentModel.setDocumentID(documentEntity.documentID);
        xmlContentModel.setVoidedLineDocumentTypeCode(documentEntity.voidedLineDocumentTypeCode);

        return Uni.createFrom().item(xmlContentModel);
    }

    protected OutgoingAmqpMetadata createScheduledMessage(Date scheduleDelivery) {
        return OutgoingAmqpMetadata.builder()
                .withMessageAnnotations("x-opt-delivery-delay", scheduleDelivery.getTime() - Calendar.getInstance().getTimeInMillis())
                .build();
    }

    private void handleRetry(DocumentCache document) {
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
        document.scheduledDelivery = scheduleDelivery;
    }

//    @Override
//    public Uni<Void> sendDocumentEvent(DocumentEvent event) {
//        return Uni.createFrom()
//                .completionStage(eventEmitter.send(event))
//                .onFailure().recoverWithNull();
//    }

    @Override
    public Uni<Void> sendDocumentToSUNAT(String documentId) {
        return Uni.createFrom()
                .completionStage(documentEmitter.send(documentId));
    }

    @Incoming("send-document-sunat-incoming")
    @Outgoing("verify-ticket-sunat")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    protected Uni<Message<String>> sendFile(Message<String> inMessage) {
        return Panache
                .withTransaction(() -> documentRepository
                        .findById(inMessage.getPayload())
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Document id=" + inMessage.getPayload() + " was not found"))
                        .onItem().ifNotNull().transform(documentEntity -> {
                            DocumentCache documentCache = new DocumentCache(documentEntity.id, documentEntity.storageFile, documentEntity.namespace.id);
                            documentCache.setRetries(documentEntity.retries);
                            return documentCache;
                        })
                )
                .invoke(documentCache -> {
                    documentCache.setError(null);
                    documentCache.setFileValid(null);
                    documentCache.setScheduledDelivery(null);
                    documentCache.setInProgress(false);
                })
                .chain(documentCache -> filesMutiny
                        .getFileAsBytesAfterUnzip(documentCache.getStorageFile())
                        .onFailure(throwable -> throwable instanceof FetchFileException).invoke(throwable -> {
                            documentCache.setError(ErrorType.FETCH_FILE);
                        })
                        .invoke(documentCache::setFile)

                        .chain(fileBytes -> xSenderMutiny
                                .getFileContent(fileBytes)
                                .onFailure(throwable -> throwable instanceof ReadFileException).invoke(throwable -> {
                                    ReadFileException readFileException = (ReadFileException) throwable;

                                    documentCache.setFileValid(false);
                                    documentCache.setDocumentType(readFileException.getDocumentType());
                                    documentCache.setError(readFileException.getDocumentType() == null ? ErrorType.READ_FILE : ErrorType.UNSUPPORTED_DOCUMENT_TYPE);
                                })
                                .invoke(xmlContentModel -> {
                                    documentCache.setFileValid(true);

                                    documentCache.setRuc(xmlContentModel.getRuc());
                                    documentCache.setDocumentID(xmlContentModel.getDocumentID());
                                    documentCache.setDocumentType(xmlContentModel.getDocumentType());
                                    documentCache.setVoidedLineDocumentTypeCode(xmlContentModel.getVoidedLineDocumentTypeCode());
                                })
                        )

                        .chain(xmlContentModel -> xSenderMutiny
                                .getWsConfig(documentCache.getNamespaceId(), xmlContentModel.getRuc())
                                .onFailure(throwable -> throwable instanceof NoCompanyWithRucException).invoke(throwable -> {
                                    documentCache.setError(ErrorType.COMPANY_NOT_FOUND);
                                })
                                .invoke(documentCache::setWsConfig)
                        )

                        .chain(wsConfig -> xSenderMutiny
                                .sendFile(documentCache.getFile(), wsConfig)
                                .onFailure(throwable -> throwable instanceof SendFileToSUNATException).invoke(throwable -> {
                                    documentCache.setError(ErrorType.SEND_FILE);
                                })
                                .invoke(billServiceModel -> {
                                    documentCache.setSunatStatus(billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null);
                                    documentCache.setSunatCode(billServiceModel.getCode());
                                    documentCache.setSunatDescription(billServiceModel.getDescription());
                                    documentCache.setSunatTicket(billServiceModel.getTicket());
                                    documentCache.setSunatNotes(billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null);
                                    documentCache.setCdrFile(billServiceModel.getCdr());
                                })
                        )

                        .chain(billServiceModel -> Uni.createFrom()
                                .item(billServiceModel.getCdr())
                                .onItem().ifNotNull().transformToUni(cdrBytes -> filesMutiny
                                        .createFile(cdrBytes, false)
                                        .map(Optional::of)
                                        .onFailure(throwable -> throwable instanceof SaveFileException).invoke(throwable -> {
                                            documentCache.setError(ErrorType.SAVE_CRD_FILE);
                                        })
                                )
                                .onItem().ifNull().continueWith(Optional::empty)
                                .invoke(cdrFileIdOptional -> {
                                    documentCache.setCdrFileId(cdrFileIdOptional.orElse(null));
                                })
                        )

                        .map(unused -> documentCache)
                        .onFailure(throwable -> throwable instanceof AbstractSendFileException).recoverWithUni(throwable -> {
                            Uni<DocumentCache> result = Uni.createFrom().item(documentCache);
                            if (throwable instanceof SendFileToSUNATException) {
                                result = result.invoke(() -> {
                                    handleRetry(documentCache);
                                    if (documentCache.getScheduledDelivery() != null) {
                                        OutgoingAmqpMetadata outgoingAmqpMetadata = createScheduledMessage(documentCache.getScheduledDelivery());
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
                .chain(documentCache -> Panache
                        .withTransaction(() -> documentRepository
                                .findById(documentCache.getId())
                                .invoke(documentEntity -> {
                                    documentEntity.error = documentCache.getError();
                                    documentEntity.fileValid = documentCache.getFileValid();
                                    documentEntity.inProgress = documentCache.getSunatTicket() != null && documentCache.getError() == null;
                                    documentEntity.scheduledDelivery = documentCache.getScheduledDelivery();

                                    documentEntity.retries = documentCache.getRetries();

                                    documentEntity.ruc = documentCache.getRuc();
                                    documentEntity.documentID = documentCache.getDocumentID();
                                    documentEntity.documentType = documentCache.getDocumentType();
                                    documentEntity.voidedLineDocumentTypeCode = documentCache.getVoidedLineDocumentTypeCode();

                                    documentEntity.sunatStatus = documentCache.getSunatStatus();
                                    documentEntity.sunatCode = documentCache.getSunatCode();
                                    documentEntity.sunatDescription = documentCache.getSunatDescription();
                                    documentEntity.sunatTicket = documentCache.getSunatTicket();
                                    documentEntity.sunatNotes = documentCache.getSunatNotes();

                                    documentEntity.storageCdr = documentCache.getCdrFileId();
                                })
                        )
                        .map(documentEntity -> documentCache)
                )
                .chain(documentCache -> {
                    Uni<Message<String>> result;
                    if (documentCache.getError() != null) {
                        switch (documentCache.getError()) {
                            case FETCH_FILE:
                                result = Uni.createFrom()
                                        .completionStage(inMessage.nack(new IllegalStateException("Uncontrolled error=" + documentCache.getError())))
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
                                throw new IllegalStateException("Uncontrolled error=" + documentCache.getError());
                        }
                    } else {
                        if (documentCache.getSunatTicket() != null) {
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

//    @Incoming("verify-ticket-sunat-incoming")
//    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
//    protected Uni<Void> verifyTicket(Message<String> inMessage) {
//        return Panache
//                .withTransaction(() -> documentRepository
//                        .findById(inMessage.getPayload())
//                        .onItem().ifNull().failWith(() -> new IllegalStateException("Document id=" + inMessage.getPayload() + " was not found"))
//                        .onItem().ifNotNull().transform(documentEntity -> {
//                            DocumentCache documentCache = new DocumentCache(documentEntity.id, documentEntity.storageFile, documentEntity.namespace.id);
//                            documentCache.setRetries(documentEntity.retries);
//                            return documentCache;
//                        })
//                )
//                .invoke(documentCache -> {
//                    documentCache.setError(null);
//                    documentCache.setFileValid(null);
//                    documentCache.setScheduledDelivery(null);
//                    documentCache.setInProgress(false);
//                })
//                .chain(documentCache -> Uni.combine().all()
//                        .unis(
//                                getXmlContent(null),
//                                xSenderMutiny.getXSenderRequiredData(documentCache.getNamespaceId(), documentCache.getRuc())
//                        )
//                        .asTuple()
//                        .chain(tuple -> xSenderMutiny.verifyTicket(documentEntity.sunatTicket, tuple.getItem1(), tuple.getItem2()))
//                )
//                .chain(() -> Uni.createFrom().completionStage(inMessage.ack()));
//    }

}

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
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
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

    @Inject
    CompanyRepository companyRepository;

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

    private void handleRetry(DocumentUniSend document) {
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
                .withTransaction(() -> findDocumentById(inMessage.getPayload())
                        .onItem().ifNotNull().transform(documentEntity -> DocumentUniSendBuilder.aDocumentUniSend()
                                .withNamespaceId(documentEntity.namespace.id)
                                .withId(documentEntity.id)
                                .withRetries(documentEntity.retries)
                                .withXmlFileId(documentEntity.storageFile)
                                .build()
                        )
                )
                .invoke(documentUni -> {
                    documentUni.setError(null);
                    documentUni.setInProgress(false);
                    documentUni.setScheduledDelivery(null);

                    documentUni.setFileValid(null);
                })
                .chain(documentUni -> filesMutiny
                        .getFileAsBytesAfterUnzip(documentUni.getXmlFileId())
                        .onFailure(throwable -> throwable instanceof FetchFileException).invoke(throwable -> {
                            documentUni.setError(ErrorType.FETCH_FILE);
                        })
                        .invoke(documentUni::setFile)

                        .chain(fileBytes -> xSenderMutiny
                                .getFileContent(fileBytes)
                                .onFailure(throwable -> throwable instanceof ReadFileException).invoke(throwable -> {
                                    ReadFileException readFileException = (ReadFileException) throwable;

                                    XmlContentModel xmlContentModel = new XmlContentModel();
                                    xmlContentModel.setDocumentType(readFileException.getDocumentType());

                                    documentUni.setError(readFileException.getDocumentType() == null ? ErrorType.READ_FILE : ErrorType.UNSUPPORTED_DOCUMENT_TYPE);
                                    documentUni.setFileValid(false);
                                    documentUni.setXmlContent(xmlContentModel);
                                })
                                .invoke(xmlContentModel -> {
                                    documentUni.setFileValid(true);
                                    documentUni.setXmlContent(xmlContentModel);
                                })
                        )

                        .chain(xmlContentModel -> xSenderMutiny
                                .getXSenderConfig(documentUni.getNamespaceId(), xmlContentModel.getRuc())
                                .onFailure(throwable -> throwable instanceof NoCompanyWithRucException).invoke(throwable -> {
                                    documentUni.setError(ErrorType.COMPANY_NOT_FOUND);
                                })
                                .invoke(documentUni::setWsConfig)
                        )

                        .chain(wsConfig -> xSenderMutiny
                                .sendFile(documentUni.getFile(), wsConfig)
                                .onFailure(throwable -> throwable instanceof SendFileToSUNATException).invoke(throwable -> {
                                    documentUni.setError(ErrorType.SEND_FILE);
                                })
                                .invoke(billServiceModel -> {
                                    documentUni.setBillServiceModel(billServiceModel);
                                })
                        )

                        .chain(billServiceModel -> Uni.createFrom()
                                .item(billServiceModel.getCdr())
                                .onItem().ifNotNull().transformToUni(cdrBytes -> filesMutiny
                                        .createFile(cdrBytes, false)
                                        .map(Optional::of)
                                        .onFailure(throwable -> throwable instanceof SaveFileException).invoke(throwable -> {
                                            documentUni.setError(ErrorType.SAVE_CRD_FILE);
                                        })
                                )
                                .onItem().ifNull().continueWith(Optional::empty)
                                .invoke(cdrFileIdOptional -> {
                                    documentUni.setCdrFileId(cdrFileIdOptional.orElse(null));
                                })
                        )

                        .map(unused -> documentUni)
                        .onFailure(throwable -> throwable instanceof AbstractSendFileException).recoverWithUni(throwable -> {
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
                .chain(documentUni -> Panache
                        .withTransaction(() -> documentRepository
                                .findById(documentUni.getId())
                                .invoke(documentEntity -> {
                                    documentEntity.error = documentUni.getError();
                                    documentEntity.fileValid = documentUni.getFileValid();
                                    documentEntity.inProgress = documentUni.getError() == null && documentUni.getBillServiceModel() != null && documentUni.getBillServiceModel().getTicket() != null;
                                    documentEntity.scheduledDelivery = documentUni.getScheduledDelivery();

                                    documentEntity.retries = documentUni.getRetries();

                                    if (documentUni.getXmlContent() != null) {
                                        documentEntity.ruc = documentUni.getXmlContent().getRuc();
                                        documentEntity.documentID = documentUni.getXmlContent().getDocumentID();
                                        documentEntity.documentType = documentUni.getXmlContent().getDocumentType();
                                        documentEntity.voidedLineDocumentTypeCode = documentUni.getXmlContent().getVoidedLineDocumentTypeCode();
                                    }

                                    if (documentUni.getBillServiceModel() != null) {
                                        documentEntity.sunatStatus = documentUni.getBillServiceModel().getStatus() != null ? documentUni.getBillServiceModel().getStatus().toString() : null;
                                        documentEntity.sunatCode = documentUni.getBillServiceModel().getCode();
                                        documentEntity.sunatDescription = documentUni.getBillServiceModel().getDescription();
                                        documentEntity.sunatTicket = documentUni.getBillServiceModel().getTicket();
                                        documentEntity.sunatNotes = documentUni.getBillServiceModel().getNotes() != null ? new HashSet<>(documentUni.getBillServiceModel().getNotes()) : new HashSet<>();
                                    }

                                    documentEntity.storageCdr = documentUni.getCdrFileId();
                                })
                        )
                        .map(documentEntity -> documentUni)
                )
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

//    @Incoming("verify-ticket-sunat-incoming")
//    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
//    protected Uni<Void> verifyTicket(Message<String> inMessage) {
//        return Panache
//                .withTransaction(() -> findDocumentById(inMessage.getPayload())
//                        .onItem().ifNotNull().transform(documentEntity -> {
//                            XmlContentModel xmlContent = XmlContentModel.Builder.aXmlContentModel()
//                                    .withRuc(documentEntity.ruc)
//                                    .withDocumentType(documentEntity.documentType)
//                                    .withDocumentID(documentEntity.documentID)
//                                    .withVoidedLineDocumentTypeCode(documentEntity.voidedLineDocumentTypeCode)
//                                    .build();
//
//                            return DocumentTicketCacheBuilder.aDocumentTicketCache()
//                                    .withNamespaceId(documentEntity.namespace.id)
//                                    .withId(documentEntity.id)
//                                    .withTicket(documentEntity.sunatTicket)
//                                    .withXmlContent(xmlContent)
//                                    .build();
//                        })
//                        .chain(documentTicketCache -> companyRepository
//                                .findByRuc(documentTicketCache.getNamespaceId(), documentTicketCache.getXmlContent().getRuc())
//                                .onItem().ifNull().failWith(() -> new NoCompanyWithRucException("No company with ruc found"))
//                                .onItem().ifNotNull().transform(companyEntity -> XSenderConfigBuilder.aXSenderConfig()
//                                        .withFacturaUrl(companyEntity.sunatUrls.sunatUrlFactura)
//                                        .withGuiaUrl(companyEntity.sunatUrls.sunatUrlGuiaRemision)
//                                        .withPercepcionRetencionUrl(companyEntity.sunatUrls.sunatUrlPercepcionRetencion)
//                                        .withUsername(companyEntity.sunatCredentials.sunatUsername)
//                                        .withPassword(companyEntity.sunatCredentials.sunatPassword)
//                                        .build()
//                                )
//                                .map(xSenderConfig -> {
//                                    documentTicketCache.setWsConfig(xSenderConfig);
//                                    return documentTicketCache;
//                                })
//                        )
//                )
//                .invoke(documentTicketCache -> {
//                    documentTicketCache.setError(null);
//                    documentTicketCache.setScheduledDelivery(null);
//                    documentTicketCache.setInProgress(false);
//                })
//                .chain(documentTicketCache -> xSenderMutiny
//                        .verifyTicket(documentTicketCache.getTicket(), documentTicketCache.getXmlContent(), documentTicketCache.getWsConfig())
//                        .chain(billServiceModel -> Uni.createFrom()
//                                .item(billServiceModel.getCdr())
//                                .onItem().ifNotNull().transformToUni(cdrBytes -> filesMutiny
//                                        .createFile(cdrBytes, false)
//                                        .map(Optional::of)
//                                        .onFailure(throwable -> throwable instanceof SaveFileException).invoke(throwable -> {
//                                            documentTicketCache.setError(ErrorType.SAVE_CRD_FILE);
//                                        })
//                                )
//                                .onItem().ifNull().continueWith(Optional::empty)
//                                .invoke(cdrFileIdOptional -> {
//                                    documentTicketCache.setCdrFileId(cdrFileIdOptional.orElse(null));
//                                })
//                        )
//
//                        .map(unused -> documentTicketCache)
//                        .onFailure(throwable -> throwable instanceof AbstractSendFileException).recoverWithUni(throwable -> {
//                            Uni<DocumentUniTicket> result = Uni.createFrom().item(documentTicketCache);
//                            if (throwable instanceof SendFileToSUNATException) {
//                                result = result.invoke(() -> {
//                                    handleRetry(documentCache);
//                                    if (documentTicketCache.getScheduledDelivery() != null) {
//                                        OutgoingAmqpMetadata outgoingAmqpMetadata = createScheduledMessage(documentTicketCache.getScheduledDelivery());
//                                        Message<String> scheduledMessage = Message
//                                                .of(inMessage.getPayload())
//                                                .withMetadata(Metadata.of(outgoingAmqpMetadata));
//                                        documentEmitter.send(scheduledMessage);
//                                    }
//                                });
//                            }
//                            return result;
//                        })
//                )
//                .chain(documentTicketCache -> Panache
//                        .withTransaction(() -> documentRepository
//                                .findById(documentTicketCache.getId())
//                                .invoke(documentEntity -> {
//                                    documentEntity.error = documentTicketCache.getError();
//                                    documentEntity.inProgress = false;
//                                    documentEntity.scheduledDelivery = documentTicketCache.getScheduledDelivery();
//
//                                    documentEntity.retries = documentTicketCache.getRetries();
//
//                                    documentEntity.sunatStatus = documentTicketCache.getSunatStatus();
//                                    documentEntity.sunatCode = documentTicketCache.getSunatCode();
//                                    documentEntity.sunatDescription = documentTicketCache.getSunatDescription();
//                                    documentEntity.sunatTicket = documentTicketCache.getSunatTicket();
//                                    documentEntity.sunatNotes = documentTicketCache.getSunatNotes();
//
//                                    documentEntity.storageCdr = documentTicketCache.getCdrFileId();
//                                })
//                        )
//                        .map(documentEntity -> documentTicketCache)
//                )
//                .chain(documentCache -> {
//                    Uni<Void> result;
//                    if (documentCache.getError() != null) {
//                        switch (documentCache.getError()) {
//                            case READ_FILE:
//                            case UNSUPPORTED_DOCUMENT_TYPE:
//                            case COMPANY_NOT_FOUND:
//                            case SEND_FILE:
//                                result = Uni.createFrom()
//                                        .completionStage(inMessage.ack())
//                                        .chain(unused -> Uni.createFrom().nullItem());
//                                break;
//                            case SAVE_CRD_FILE:
//                                result = Uni.createFrom()
//                                        .completionStage(inMessage.nack(new SaveFileException(ErrorType.SAVE_CRD_FILE.getMessage())))
//                                        .chain(unused -> Uni.createFrom().nullItem());
//                                break;
//                            default:
//                                throw new IllegalStateException("Uncontrolled error=" + documentCache.getError());
//                        }
//                    } else {
//                        result = Uni.createFrom()
//                                .completionStage(inMessage.ack())
//                                .chain(unused -> Uni.createFrom().nullItem());
//                    }
//                    return result;
//                });
//    }

    protected Uni<UBLDocumentEntity> findDocumentById(String documentId) {
        return documentRepository
                .findById(documentId)
                .onItem().ifNull().failWith(() -> new IllegalStateException("Document id=" + documentId + " was not found"));
    }
}

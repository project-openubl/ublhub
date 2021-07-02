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

import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xsender.events.DocumentEvent;
import io.github.project.openubl.xsender.events.EventManager;
import io.github.project.openubl.xsender.exceptions.*;
import io.github.project.openubl.xsender.files.FilesMutiny;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.sender.XSenderMutiny;
import io.github.project.openubl.xsender.sender.XSenderRequiredData;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

    private void handleRetry(UBLDocumentEntity documentEntity) {
        int retries = documentEntity.retries;
        Date scheduleDelivery = null;
        if (retries <= 2) {
            retries++;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (int) Math.pow(5, retries));
            scheduleDelivery = calendar.getTime();
        } else {
            documentEntity.error = ErrorType.RETRY_CONSUMED;
        }

        // Result
        documentEntity.retries = retries;
        documentEntity.scheduledDelivery = scheduleDelivery;
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
                .withTransaction(() -> documentRepository.findById(inMessage.getPayload()))
                .onItem().ifNull().failWith(() -> new IllegalStateException("Document id=" + inMessage.getPayload() + " was not found"))
                .invoke(documentEntity -> {
                    documentEntity.error = null;
                    documentEntity.scheduledDelivery = null;
                    documentEntity.inProgress = false;
                })
                .chain(documentEntity -> {
                    Uni<byte[]> fileBytesUni = filesMutiny.getFileAsBytesAfterUnzip(documentEntity.storageFile)
                            .onFailure(throwable -> throwable instanceof FetchFileException).invoke(throwable -> {
                                documentEntity.error = ErrorType.FETCH_FILE;
                            });

                    return fileBytesUni
                            .chain(fileBytes -> {
                                Uni<XmlContentModel> xmlContentUni = xSenderMutiny.getFileContent(fileBytes)
                                        .invoke(xmlContentModel -> {
                                            documentEntity.fileValid = true;

                                            documentEntity.ruc = xmlContentModel.getRuc();
                                            documentEntity.documentID = xmlContentModel.getDocumentID();
                                            documentEntity.documentType = xmlContentModel.getDocumentType();
                                            documentEntity.voidedLineDocumentTypeCode = xmlContentModel.getVoidedLineDocumentTypeCode();
                                        })
                                        .onFailure(throwable -> throwable instanceof ReadFileException).invoke(throwable -> {
                                            ReadFileException readFileException = (ReadFileException) throwable;

                                            documentEntity.fileValid = false;
                                            documentEntity.documentType = readFileException.getDocumentType();
                                            documentEntity.error = readFileException.getDocumentType() == null ? ErrorType.READ_FILE : ErrorType.UNSUPPORTED_DOCUMENT_TYPE;
                                        });

                                Uni<XSenderRequiredData> xSenderRequiredDataUni = xmlContentUni
                                        .chain(xmlContentModel -> xSenderMutiny.getXSenderRequiredData(documentEntity.namespace, xmlContentModel.getRuc()))
                                        .onFailure(throwable -> throwable instanceof NoCompanyWithRucException).invoke(throwable -> {
                                            documentEntity.error = ErrorType.COMPANY_NOT_FOUND;
                                        });

                                Uni<BillServiceModel> billServiceModelUni = xSenderRequiredDataUni
                                        .chain(xSenderRequiredData -> xSenderMutiny.sendFile(fileBytes, xSenderRequiredData))
                                        .invoke(billServiceModel -> {
                                            documentEntity.sunatStatus = billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null;
                                            documentEntity.sunatCode = billServiceModel.getCode();
                                            documentEntity.sunatDescription = billServiceModel.getDescription();
                                            documentEntity.sunatTicket = billServiceModel.getTicket();
                                            documentEntity.sunatNotes = billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null;
                                        })
                                        .onFailure(throwable -> throwable instanceof SendFileToSUNATException).invoke(throwable -> {
                                            documentEntity.error = ErrorType.SEND_FILE;
                                        });

                                Uni<String> cdrUni = billServiceModelUni
                                        .map(BillServiceModel::getCdr)
                                        .onItem().ifNotNull().transformToUni(cdrBytes -> filesMutiny.createFile(cdrBytes, false))
                                        .invoke(cdrId -> documentEntity.storageCdr = cdrId)
                                        .onFailure(throwable -> throwable instanceof SaveFileException).invoke(throwable -> {
                                            documentEntity.error = ErrorType.SAVE_CRD_FILE;
                                        });

                                return cdrUni.map(billServiceModel -> documentEntity);
                            })
                            .onFailure(throwable -> throwable instanceof AbstractSendFileException).recoverWithUni(throwable -> {
                                Uni<UBLDocumentEntity> result = Uni.createFrom().item(documentEntity);
                                if (throwable instanceof SendFileToSUNATException) {
                                    return result.invoke(() -> {
                                        handleRetry(documentEntity);
                                        if (documentEntity.scheduledDelivery != null) {
                                            OutgoingAmqpMetadata outgoingAmqpMetadata = createScheduledMessage(documentEntity.scheduledDelivery);
                                            Message<String> scheduledMessage = Message
                                                    .of(inMessage.getPayload())
                                                    .withMetadata(Metadata.of(outgoingAmqpMetadata));
                                            documentEmitter.send(scheduledMessage);
                                        }
                                    });
                                } else {
                                    return result;
                                }
                            });
                })
                .chain(documentEntity -> Panache
                        .withTransaction(() -> {
                            documentEntity.inProgress = documentEntity.sunatTicket != null && documentEntity.error == null;
                            return documentRepository.persist(documentEntity);
                        })
                        .map(unused -> documentEntity)
                )
                .chain(documentEntity -> {
                    Uni<Message<String>> result;

                    if (documentEntity.error != null) {
                        switch (documentEntity.error) {
                            case FETCH_FILE:
                                result = Uni.createFrom()
                                        .completionStage(inMessage.nack(new IllegalStateException("Uncontrolled error=" + documentEntity.error)))
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
                                throw new IllegalStateException("Uncontrolled error=" + documentEntity.error);
                        }
                    } else {
                        if (documentEntity.sunatTicket != null) {
                            result = Uni.createFrom().item(inMessage);
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
        return Panache
                .withTransaction(() -> documentRepository.findById(inMessage.getPayload()))
                .invoke(documentEntity -> documentEntity.error = null)
                .chain(documentEntity -> Uni.combine().all()
                        .unis(
                                getXmlContent(documentEntity),
                                xSenderMutiny.getXSenderRequiredData(documentEntity.namespace, documentEntity.ruc)
                        )
                        .asTuple()
                        .chain(tuple -> xSenderMutiny.verifyTicket(documentEntity.sunatTicket, tuple.getItem1(), tuple.getItem2()))
                )
                .chain(() -> Uni.createFrom().completionStage(inMessage.ack()));
    }

}

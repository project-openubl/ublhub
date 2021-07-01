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
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
                .invoke(documentEntity -> documentEntity.error = null)
                .chain(documentEntity -> filesMutiny
                        .getFileAsBytesAfterUnzip(documentEntity.storageFile)
                        .chain(bytes -> xSenderMutiny
                                .getFileContent(bytes)
                                .invoke(xmlContentModel -> {
                                    documentEntity.fileValid = true;

                                    documentEntity.ruc = xmlContentModel.getRuc();
                                    documentEntity.documentID = xmlContentModel.getDocumentID();
                                    documentEntity.documentType = xmlContentModel.getDocumentType();
                                    documentEntity.voidedLineDocumentTypeCode = xmlContentModel.getVoidedLineDocumentTypeCode();
                                })

                                .chain(xmlContentModel -> xSenderMutiny.getXSenderRequiredData(documentEntity.namespace, xmlContentModel.getRuc()))
                                .chain(xSenderRequiredData -> xSenderMutiny.sendFile(bytes, xSenderRequiredData))
                                .invoke(billServiceModel -> {
                                    documentEntity.sunatStatus = billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null;
                                    documentEntity.sunatCode = billServiceModel.getCode();
                                    documentEntity.sunatDescription = billServiceModel.getDescription();
                                    documentEntity.sunatTicket = billServiceModel.getTicket();
                                    documentEntity.sunatNotes = billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null;
                                })
                                .map(BillServiceModel::getCdr)
                                .onItem().ifNotNull().transformToUni(cdrBytes -> filesMutiny.createFile(cdrBytes, false))
                                .invoke(cdrId -> documentEntity.storageCdr = cdrId)
                                .map(unused -> documentEntity)

                                .onFailure().recoverWithItem(throwable -> {
                                    if (throwable instanceof FetchFileException) {
                                        documentEntity.error = ErrorType.FETCH_FILE;
                                    } else if (throwable instanceof ReadFileException) {
                                        documentEntity.error = ErrorType.READ_FILE;
                                        documentEntity.fileValid = false;
                                    } else if (throwable instanceof DocumentTypeNotSupportedException) {
                                        documentEntity.error = ErrorType.UNSUPPORTED_DOCUMENT_TYPE;
                                        documentEntity.fileValid = false;
                                        documentEntity.documentType = ((DocumentTypeNotSupportedException) throwable).getDocumentType();
                                    } else if (throwable instanceof NoCompanyWithRucException) {
                                        documentEntity.error = ErrorType.COMPANY_NOT_FOUND;
                                    } else if (throwable instanceof SendFileToSUNATException) {
                                        documentEntity.error = ErrorType.SEND_FILE;
                                    } else if (throwable instanceof SaveFileException) {
                                        documentEntity.error = ErrorType.SAVE_CRD_FILE;
                                    } else {
                                        documentEntity.error = ErrorType.UNKNOWN;
                                    }

                                    return documentEntity;
                                })
                        )
                )
                .chain(documentEntity -> Panache
                        .withTransaction(() -> {
                            documentEntity.inProgress = documentEntity.sunatTicket != null;
                            return documentRepository.persist(documentEntity);
                        })
                        .map(unused -> documentEntity)
                )
                .chain(documentEntity -> {
                    if (documentEntity.error == ErrorType.FETCH_FILE) {
                        return Uni.createFrom()
                                .completionStage(
                                        inMessage.nack(new FetchFileException(ErrorType.FETCH_FILE.getMessage()))
                                )
                                .map(unused -> null);
                    } else if (documentEntity.error == ErrorType.READ_FILE) {
                        return Uni.createFrom()
                                .completionStage(inMessage.ack())
                                .map(unused -> null);
                    } else if (documentEntity.error == ErrorType.UNSUPPORTED_DOCUMENT_TYPE) {
                        return Uni.createFrom()
                                .completionStage(inMessage.ack())
                                .map(unused -> null);
                    } else if (documentEntity.error == ErrorType.COMPANY_NOT_FOUND) {
                        return Uni.createFrom()
                                .completionStage(inMessage.ack())
                                .map(unused -> null);
                    } else if (documentEntity.error == ErrorType.SEND_FILE) {
                        // retry
                        return Uni.createFrom()
                                .completionStage(inMessage.ack())
                                .map(unused -> null);
                    } else if (documentEntity.error == ErrorType.SAVE_CRD_FILE) {
                        // retry
                        return Uni.createFrom()
                                .completionStage(
                                        inMessage.nack(new FetchFileException(ErrorType.SAVE_CRD_FILE.getMessage()))
                                )
                                .map(unused -> null);
                    }

                    if (documentEntity.sunatTicket != null) {
                        return Uni.createFrom().item(Message.of(documentEntity.id)
                                        .withAck(inMessage::ack)
//                                .withNack(throwable -> withNack(documentEntity.id))
                        );
                    } else {
                        return Uni.createFrom()
                                .completionStage(inMessage.ack())
                                .map(unused -> null);
                    }
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

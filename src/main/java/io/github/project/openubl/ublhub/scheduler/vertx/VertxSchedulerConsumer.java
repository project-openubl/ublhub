/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.scheduler.vertx;

import io.github.project.openubl.ublhub.files.FilesMutiny;
import io.github.project.openubl.ublhub.files.exceptions.PersistFileException;
import io.github.project.openubl.ublhub.files.exceptions.ReadFileException;
import io.github.project.openubl.ublhub.models.JobPhaseType;
import io.github.project.openubl.ublhub.models.JobRecoveryActionType;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.JobErrorEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SUNATResponseEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.XMLFileContentEntity;
import io.github.project.openubl.ublhub.scheduler.SchedulerManager;
import io.github.project.openubl.ublhub.scheduler.exceptions.FetchFileException;
import io.github.project.openubl.ublhub.ubl.sender.XMLSenderManager;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ConnectToSUNATException;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ReadXMLFileContentException;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.HashSet;

@ApplicationScoped
public class VertxSchedulerConsumer {

    static final int INITIAL_DELAY = 100;

    @Inject
    SchedulerManager schedulerManager;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    FilesMutiny filesMutiny;

    @Inject
    XMLSenderManager xSenderMutiny;

    @ConsumeEvent(VertxScheduler.VERTX_SEND_FILE_SCHEDULER_BUS_NAME)
    public Uni<Void> sendFile(String documentId) {
        return Uni.createFrom().item(documentId)
                .onItem().delayIt().by(Duration.ofMillis(INITIAL_DELAY))
                .chain(() -> Panache.withTransaction(() -> documentRepository
                        .findById(documentId)
                        .onItem().ifNull().failWith(IllegalStateException::new)
                        .onFailure(IllegalStateException.class)
                        .retry().withBackOff(Duration.ofSeconds(1)).withJitter(0.2).atMost(3)

                        .chain(documentEntity -> filesMutiny
                                // Download file
                                .getFileAsBytesAfterUnzip(documentEntity.xmlFileId)
                                .onFailure(ReadFileException.class).recoverWithUni(throwable -> Uni.createFrom()
                                        .failure(new FetchFileException(JobPhaseType.FETCH_XML_FILE, throwable))
                                )
                                .chain(xmlFile -> xSenderMutiny
                                        // Read file content
                                        .getXMLContent(xmlFile)
                                        .onFailure(ReadXMLFileContentException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                .failure(new FetchFileException(JobPhaseType.READ_XML_FILE, throwable))
                                        )
                                        .chain(xmlFileContent -> {
                                            documentEntity.xmlFileContent = new XMLFileContentEntity();

                                            XMLFileContentEntity xmlFileContentEntity = documentEntity.xmlFileContent;
                                            xmlFileContentEntity.document = documentEntity;

                                            xmlFileContentEntity.ruc = xmlFileContent.getRuc();
                                            xmlFileContentEntity.tipoDocumento = xmlFileContent.getDocumentType();
                                            xmlFileContentEntity.serieNumero = xmlFileContent.getDocumentID();
                                            xmlFileContentEntity.bajaCodigoTipoDocumento = xmlFileContent.getVoidedLineDocumentTypeCode();

                                            // Read SUNAT configuration
                                            return xSenderMutiny.getXSenderConfig(documentEntity.namespace.id, xmlFileContent.getRuc());
                                        })

                                        // Send file to SUNAT
                                        .chain(sunatConfig -> xSenderMutiny.sendToSUNAT(xmlFile, sunatConfig))
                                        .onFailure(ConnectToSUNATException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                .failure(new FetchFileException(JobPhaseType.SEND_XML_FILE, throwable))
                                        )
                                        .invoke(sunatResponse -> {
                                            documentEntity.sunatResponse = new SUNATResponseEntity();

                                            SUNATResponseEntity sunatResponseEntity = documentEntity.sunatResponse;
                                            sunatResponseEntity.document = documentEntity;

                                            sunatResponseEntity.code = sunatResponse.getCode();
                                            sunatResponseEntity.ticket = sunatResponse.getTicket();
                                            sunatResponseEntity.description = sunatResponse.getDescription();
                                            sunatResponseEntity.status = sunatResponse.getStatus().toString();
                                            sunatResponseEntity.notes = new HashSet<>(sunatResponse.getNotes());
                                        })

                                        // Save CDR
                                        .map(BillServiceModel::getCdr)
                                        .onItem().ifNotNull().transformToUni(cdrFile -> filesMutiny
                                                .createFile(cdrFile, false)
                                                .onFailure(PersistFileException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                        .failure(new FetchFileException(JobPhaseType.SAVE_CDR, throwable))
                                                )
                                                .invoke(cdrFileId -> documentEntity.cdrFileId = cdrFileId)
                                        )
                                )
                                .map(unused -> documentEntity)
                        )
                        .onItemOrFailure().transformToUni((documentEntity, throwable) -> {
                            if (throwable instanceof FetchFileException) {
                                FetchFileException jobException = (FetchFileException) throwable;
                                documentEntity.jobError = new JobErrorEntity();

                                JobErrorEntity jobErrorEntity = documentEntity.jobError;
                                jobErrorEntity.phase = jobException.getPhase();

                                switch (jobException.getPhase()) {
                                    case FETCH_XML_FILE: {
                                        jobErrorEntity.description = "No se pudo descargar el XML para ser procesado";
                                        jobErrorEntity.recoveryAction = JobRecoveryActionType.RETRY_SEND;
                                        jobErrorEntity.recoveryActionCount = 1;

                                        break;
                                    }
                                    case READ_XML_FILE: {
                                        jobErrorEntity.description = "El contenido del XML no es válido";
                                        break;
                                    }
                                    case SEND_XML_FILE: {
                                        jobErrorEntity.description = "No se pudo enviar el XML a la SUNAT";
                                        jobErrorEntity.recoveryAction = JobRecoveryActionType.RETRY_SEND;
                                        jobErrorEntity.recoveryActionCount = 1;
                                        break;
                                    }
                                    case SAVE_CDR: {
                                        jobErrorEntity.description = "No se pudo guardar el CDR en el storage";
                                        jobErrorEntity.recoveryAction = JobRecoveryActionType.RETRY_FETCH_CDR;
                                        jobErrorEntity.recoveryActionCount = 1;
                                        break;
                                    }
                                }
                            }

                            documentEntity.jobInProgress = false;
                            return documentEntity.<UBLDocumentEntity>persist();
                        })
                ))
                .chain(documentEntity -> {
                    if (documentEntity.sunatResponse != null && documentEntity.sunatResponse.ticket != null) {
                        return schedulerManager.sendVerifyTicketAtSUNAT(documentEntity);
                    } else {
                        return Uni.createFrom().voidItem();
                    }
                });
    }

    @ConsumeEvent(VertxScheduler.VERTX_CHECK_TICKET_SCHEDULER_BUS_NAME)
    public Uni<Void> checkTicket(String documentId) {
        return Uni.createFrom().item(documentId)
                .onItem().delayIt().by(Duration.ofMillis(INITIAL_DELAY))
                .chain(() -> Panache.withTransaction(() -> documentRepository
                        .findById(documentId)
                        .onItem().ifNull().failWith(IllegalStateException::new)
                        .onFailure(IllegalStateException.class)
                        .retry().withBackOff(Duration.ofSeconds(1)).withJitter(0.2).atMost(3)

                        .chain(documentEntity -> xSenderMutiny
                                .getXSenderConfig(documentEntity.namespace.id, documentEntity.xmlFileContent.ruc)
                                .chain(sunatConfig -> {
                                    XmlContentModel xmlContentModel = new XmlContentModel();
                                    xmlContentModel.setRuc(documentEntity.xmlFileContent.ruc);
                                    xmlContentModel.setDocumentType(documentEntity.xmlFileContent.tipoDocumento);
                                    xmlContentModel.setDocumentID(documentEntity.xmlFileContent.serieNumero);
                                    xmlContentModel.setVoidedLineDocumentTypeCode(documentEntity.xmlFileContent.bajaCodigoTipoDocumento);

                                    // Send file to SUNAT
                                    return xSenderMutiny
                                            .verifyTicketAtSUNAT(documentEntity.sunatResponse.ticket, xmlContentModel, sunatConfig)
                                            .onFailure(ConnectToSUNATException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                    .failure(new FetchFileException(JobPhaseType.VERIFY_TICKET))
                                            )
                                            .invoke(sunatResponse -> {
                                                documentEntity.sunatResponse = new SUNATResponseEntity();

                                                SUNATResponseEntity sunatResponseEntity = documentEntity.sunatResponse;
                                                sunatResponseEntity.document = documentEntity;

                                                sunatResponseEntity.code = sunatResponse.getCode();
                                                sunatResponseEntity.ticket = sunatResponse.getTicket();
                                                sunatResponseEntity.description = sunatResponse.getDescription();
                                                sunatResponseEntity.status = sunatResponse.getStatus().toString();
                                                sunatResponseEntity.notes = new HashSet<>(sunatResponse.getNotes());
                                            })

                                            // Save CDR
                                            .map(BillServiceModel::getCdr)
                                            .onItem().ifNotNull().transformToUni(cdrFile -> filesMutiny
                                                    .createFile(cdrFile, false)
                                                    .onFailure(PersistFileException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                            .failure(new FetchFileException(JobPhaseType.SAVE_CDR))
                                                    )
                                                    .invoke(cdrFileId -> documentEntity.cdrFileId = cdrFileId)
                                            );
                                })
                                .map(unused -> documentEntity)
                        )
                        .onItemOrFailure().transformToUni((documentEntity, throwable) -> {
                            if (throwable instanceof FetchFileException) {
                                FetchFileException jobException = (FetchFileException) throwable;
                                documentEntity.jobError = new JobErrorEntity();

                                JobErrorEntity jobErrorEntity = documentEntity.jobError;
                                jobErrorEntity.phase = jobException.getPhase();

                                if (jobException.getPhase() == JobPhaseType.VERIFY_TICKET) {
                                    jobErrorEntity.description = "No se pudo verificar el ticket en la SUNAT";
                                    jobErrorEntity.recoveryAction = JobRecoveryActionType.RETRY_SEND;
                                    jobErrorEntity.recoveryActionCount = 1;
                                }
                            }

                            documentEntity.jobInProgress = false;
                            return documentEntity.<UBLDocumentEntity>persist();
                        })
                ))
                .replaceWithVoid();
    }
}

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
import io.github.project.openubl.ublhub.models.jpa.entities.ErrorEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SUNATResponseEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.XMLDataEntity;
import io.github.project.openubl.ublhub.scheduler.SchedulerManager;
import io.github.project.openubl.ublhub.scheduler.exceptions.FetchFileException;
import io.github.project.openubl.ublhub.ubl.sender.XMLSenderManager;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ConnectToSUNATException;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ReadXMLFileContentException;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.HashSet;

@RequestScoped
public class VertxSchedulerConsumer {

    static final int INITIAL_DELAY = 10;

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
        Uni<UBLDocumentEntity> documentEntityUni = documentRepository
                .findById(documentId)
                .onItem().ifNull().failWith(() -> new IllegalStateException("Document id=" + documentId + " was not found for being sent"))
                .onFailure(throwable -> throwable instanceof IllegalStateException)
                .retry().withBackOff(Duration.ofMillis(500)).withJitter(0.2).atMost(3)

                .onItem().ifNull().failWith(IllegalStateException::new)
                .onFailure(IllegalStateException.class)
                .retry().withBackOff(Duration.ofSeconds(1)).withJitter(0.2).atMost(3)

                .chain(documentEntity -> filesMutiny
                        // Download file
                        .getFileAsBytesAfterUnzip(documentEntity.getXmlFileId())
                        .onFailure(ReadFileException.class).recoverWithUni(throwable -> Uni
                                .createFrom()
                                .failure(new FetchFileException(JobPhaseType.FETCH_XML_FILE, throwable))
                        )
                        .chain(xmlFile -> xSenderMutiny
                                // Read file content
                                .getXMLContent(xmlFile)
                                .onFailure(ReadXMLFileContentException.class).recoverWithUni(throwable -> Uni.createFrom()
                                        .failure(new FetchFileException(JobPhaseType.READ_XML_FILE, throwable))
                                )
                                .chain(xmlFileContent -> {
                                    XMLDataEntity xmlDataEntity = documentEntity.getXmlData();
                                    if (xmlDataEntity == null) {
                                        xmlDataEntity = new XMLDataEntity();
                                        documentEntity.setXmlData(xmlDataEntity);
                                    }

                                    xmlDataEntity.setRuc(xmlFileContent.getRuc());
                                    xmlDataEntity.setTipoDocumento(xmlFileContent.getDocumentType());
                                    xmlDataEntity.setSerieNumero(xmlFileContent.getDocumentID());
                                    xmlDataEntity.setBajaCodigoTipoDocumento(xmlFileContent.getVoidedLineDocumentTypeCode());

                                    // Read SUNAT configuration
                                    return xSenderMutiny.getXSenderConfig(documentEntity.getProjectId(), xmlFileContent.getRuc());
                                })

                                // Send file to SUNAT
                                .chain(sunatConfig -> {
                                    return xSenderMutiny.sendToSUNAT(xmlFile, sunatConfig);
                                })
                                .onFailure(ConnectToSUNATException.class).recoverWithUni(throwable -> Uni.createFrom()
                                        .failure(new FetchFileException(JobPhaseType.SEND_XML_FILE, throwable))
                                )
                                .invoke(sunatResponse -> {
                                    SUNATResponseEntity sunatResponseEntity = documentEntity.getSunatResponse();
                                    if (sunatResponseEntity == null) {
                                        sunatResponseEntity = new SUNATResponseEntity();
                                        documentEntity.setSunatResponse(sunatResponseEntity);
                                    }

                                    sunatResponseEntity.setTicket(sunatResponse.getSunat().getTicket());
                                    sunatResponseEntity.setCode(sunatResponse.getMetadata().getResponseCode());
                                    sunatResponseEntity.setDescription(sunatResponse.getMetadata().getDescription());
                                    sunatResponseEntity.setStatus(sunatResponse.getStatus() != null ? sunatResponse.getStatus().toString() : null);
                                    sunatResponseEntity.setNotes(sunatResponse.getMetadata().getNotes() != null ? new HashSet<>(sunatResponse.getMetadata().getNotes()) : null);
                                })

                                // Save CDR
                                .map(sunatResponse -> sunatResponse.getSunat().getCdr())
                                .onItem().ifNotNull().transformToUni(cdrFile -> filesMutiny
                                        .createFile(cdrFile, false)
                                        .onFailure(PersistFileException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                .failure(new FetchFileException(JobPhaseType.SAVE_CDR, throwable))
                                        )
                                        .invoke(cdrFileId -> documentEntity.setCdrFileId(cdrFileId))
                                )
                        )

                        .onItemOrFailure().transformToUni((unused, throwable) -> {
                            if (throwable instanceof FetchFileException) {
                                FetchFileException jobException = (FetchFileException) throwable;

                                ErrorEntity errorEntity = documentEntity.getError();
                                if (errorEntity == null) {
                                    errorEntity = new ErrorEntity();
                                    documentEntity.setError(errorEntity);
                                }

                                errorEntity.setPhase(jobException.getPhase());

                                switch (jobException.getPhase()) {
                                    case FETCH_XML_FILE: {
                                        errorEntity.setDescription("No se pudo descargar el XML para ser procesado");
                                        errorEntity.setRecoveryAction(JobRecoveryActionType.RETRY_SEND);
                                        errorEntity.setCount(1);

                                        break;
                                    }
                                    case READ_XML_FILE: {
                                        errorEntity.setDescription("El contenido del XML no es válido");
                                        break;
                                    }
                                    case SEND_XML_FILE: {
                                        errorEntity.setDescription("No se pudo enviar el XML a la SUNAT");
                                        errorEntity.setRecoveryAction(JobRecoveryActionType.RETRY_SEND);
                                        errorEntity.setCount(1);
                                        break;
                                    }
                                    case SAVE_CDR: {
                                        errorEntity.setDescription("No se pudo guardar el CDR en el storage");
                                        errorEntity.setRecoveryAction(JobRecoveryActionType.RETRY_FETCH_CDR);
                                        errorEntity.setCount(1);
                                        break;
                                    }
                                }
                            }

                            documentEntity.setJobInProgress(false);
                            return documentEntity.<UBLDocumentEntity>persistAndFlush();
                        })

                );

        return Uni.createFrom().item(documentId)
                .onItem().delayIt().by(Duration.ofMillis(INITIAL_DELAY))
                .chain(() -> Panache.withTransaction(() -> documentEntityUni.chain(documentEntity -> {
                    if (documentEntity.getSunatResponse() != null && documentEntity.getSunatResponse().getTicket() != null) {
                        return schedulerManager.sendVerifyTicketAtSUNAT(documentEntity);
                    } else {
                        return Uni.createFrom().voidItem();
                    }
                })));
    }

    @ConsumeEvent(VertxScheduler.VERTX_CHECK_TICKET_SCHEDULER_BUS_NAME)
    public Uni<Void> checkTicket(String documentId) {
        return Uni.createFrom().item(documentId)
                .onItem().delayIt().by(Duration.ofMillis(INITIAL_DELAY))
                .chain(() -> Panache.withTransaction(() -> documentRepository
                        .findById(documentId)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Document id=" + documentId + " was not found for being sent"))
                        .onFailure(throwable -> throwable instanceof IllegalStateException)
                        .retry().withBackOff(Duration.ofMillis(500)).withJitter(0.2).atMost(3)

                        .onItem().ifNull().failWith(IllegalStateException::new)
                        .onFailure(IllegalStateException.class)
                        .retry().withBackOff(Duration.ofSeconds(1)).withJitter(0.2).atMost(3)

                        .chain(documentEntity -> xSenderMutiny
                                .getXSenderConfig(documentEntity.getProjectId(), documentEntity.getXmlData().getRuc())
                                .chain(sunatConfig -> {
                                    XmlContent xmlContentModel = new XmlContent();
                                    xmlContentModel.setRuc(documentEntity.getXmlData().getRuc());
                                    xmlContentModel.setDocumentType(documentEntity.getXmlData().getTipoDocumento());
                                    xmlContentModel.setDocumentID(documentEntity.getXmlData().getSerieNumero());
                                    xmlContentModel.setVoidedLineDocumentTypeCode(documentEntity.getXmlData().getBajaCodigoTipoDocumento());

                                    // Send file to SUNAT
                                    return xSenderMutiny
                                            .verifyTicketAtSUNAT(documentEntity.getSunatResponse().getTicket(), xmlContentModel, sunatConfig)
                                            .onFailure(ConnectToSUNATException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                    .failure(new FetchFileException(JobPhaseType.VERIFY_TICKET))
                                            )
                                            .invoke(sunatResponse -> {
                                                SUNATResponseEntity sunatResponseEntity = documentEntity.getSunatResponse();
                                                if (sunatResponseEntity == null) {
                                                    sunatResponseEntity = new SUNATResponseEntity();
                                                    documentEntity.setSunatResponse(sunatResponseEntity);
                                                }

                                                sunatResponseEntity.setTicket(sunatResponse.getSunat().getTicket());
                                                sunatResponseEntity.setCode(sunatResponse.getMetadata().getResponseCode());
                                                sunatResponseEntity.setDescription(sunatResponse.getMetadata().getDescription());
                                                sunatResponseEntity.setStatus(sunatResponse.getStatus() != null ? sunatResponse.getStatus().toString() : null);
                                                sunatResponseEntity.setNotes(sunatResponse.getMetadata().getNotes() != null ? new HashSet<>(sunatResponse.getMetadata().getNotes()) : null);
                                            })

                                            // Save CDR
                                            .map(sunatResponse -> sunatResponse.getSunat().getCdr())
                                            .onItem().ifNotNull().transformToUni(cdrFile -> filesMutiny
                                                    .createFile(cdrFile, false)
                                                    .onFailure(PersistFileException.class).recoverWithUni(throwable -> Uni.createFrom()
                                                            .failure(new FetchFileException(JobPhaseType.SAVE_CDR))
                                                    )
                                                    .invoke(cdrFileId -> documentEntity.setCdrFileId(cdrFileId))
                                            );
                                })
                                .map(unused -> documentEntity)
                        )
                        .onItemOrFailure().transformToUni((documentEntity, throwable) -> {
                            if (throwable instanceof FetchFileException) {
                                FetchFileException jobException = (FetchFileException) throwable;

                                ErrorEntity errorEntity = documentEntity.getError();
                                if (errorEntity == null) {
                                    errorEntity = new ErrorEntity();
                                    documentEntity.setError(errorEntity);
                                }

                                errorEntity.setPhase(jobException.getPhase());

                                if (jobException.getPhase() == JobPhaseType.VERIFY_TICKET) {
                                    errorEntity.setDescription("No se pudo verificar el ticket en la SUNAT");
                                    errorEntity.setRecoveryAction(JobRecoveryActionType.RETRY_SEND);
                                    errorEntity.setCount(1);
                                }
                            }

                            documentEntity.setJobInProgress(false);
                            return documentEntity.<UBLDocumentEntity>persist();
                        })
                ))
                .onFailure().invoke(throwable -> {
                    System.out.println("");
                })
                .replaceWithVoid();
    }
}

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

import io.github.project.openubl.ublhub.files.FilesManager;
import io.github.project.openubl.ublhub.models.JobPhaseType;
import io.github.project.openubl.ublhub.models.JobRecoveryActionType;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.ErrorEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SUNATResponseEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.XMLDataEntity;
import io.github.project.openubl.ublhub.scheduler.SchedulerManager;
import io.github.project.openubl.ublhub.ubl.sender.XMLSenderConfig;
import io.github.project.openubl.ublhub.ubl.sender.XMLSenderManager;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ConnectToSUNATException;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ReadXMLFileContentException;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import io.github.project.openubl.xsender.models.SunatResponse;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.HashSet;

@RequestScoped
public class VertxSchedulerConsumer {

    static final int INITIAL_DELAY = 10;

    @Inject
    SchedulerManager schedulerManager;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    FilesManager filesManager;

    @Inject
    XMLSenderManager xmlSenderManager;

    @Blocking
    @ConsumeEvent(VertxScheduler.VERTX_SEND_FILE_SCHEDULER_BUS_NAME)
    public void sendFile(String documentId) {
        QuarkusTransaction.begin();

        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            throw new IllegalStateException("Document id=" + documentId + " was not found for being sent");
        }

        try {
            // Download file
            byte[] xmlFile = filesManager.getFileAsBytesAfterUnzip(documentEntity.getXmlFileId());

            // Read file content
            XmlContent xmlContent = xmlSenderManager.getXMLContent(xmlFile);

            if (documentEntity.getXmlData() == null) {
                documentEntity.setXmlData(new XMLDataEntity());
            }

            documentEntity.getXmlData().setRuc(xmlContent.getRuc());
            documentEntity.getXmlData().setTipoDocumento(xmlContent.getDocumentType());
            documentEntity.getXmlData().setSerieNumero(xmlContent.getDocumentID());
            documentEntity.getXmlData().setBajaCodigoTipoDocumento(xmlContent.getVoidedLineDocumentTypeCode());

            // Read SUNAT configuration
            XMLSenderConfig xSenderConfig = xmlSenderManager.getXSenderConfig(documentEntity.getProjectId(), documentEntity.getXmlData().getRuc());

            // Send file to SUNAT
            SunatResponse sunatResponse = xmlSenderManager.sendToSUNAT(xmlFile, xSenderConfig);

            // Save sunat response
            if (documentEntity.getSunatResponse() == null) {
                documentEntity.setSunatResponse(new SUNATResponseEntity());
            }

            documentEntity.getSunatResponse().setTicket(sunatResponse.getSunat().getTicket());
            documentEntity.getSunatResponse().setCode(sunatResponse.getMetadata().getResponseCode());
            documentEntity.getSunatResponse().setDescription(sunatResponse.getMetadata().getDescription());
            documentEntity.getSunatResponse().setStatus(sunatResponse.getStatus() != null ? sunatResponse.getStatus().toString() : null);
            documentEntity.getSunatResponse().setNotes(sunatResponse.getMetadata().getNotes() != null ? new HashSet<>(sunatResponse.getMetadata().getNotes()) : null);

            // Save CDR
            if (sunatResponse.getSunat() != null && sunatResponse.getSunat().getCdr() != null) {
                byte[] cdr = sunatResponse.getSunat().getCdr();
                String cdrFileId = filesManager.createFile(cdr, false);
                documentEntity.setCdrFileId(cdrFileId);
            }
        } catch (ReadXMLFileContentException | ConnectToSUNATException e) {
            ErrorEntity errorEntity = documentEntity.getError();
            if (errorEntity == null) {
                errorEntity = new ErrorEntity();
                documentEntity.setError(errorEntity);
            }

            if (e instanceof ReadXMLFileContentException) {
                errorEntity.setPhase(JobPhaseType.READ_XML_FILE);
                errorEntity.setDescription("No se pudo leer XML");
            } else {
                errorEntity.setPhase(JobPhaseType.SEND_XML_FILE);
                errorEntity.setDescription("No se pudo enviar el XML a la SUNAT");
            }
            errorEntity.setRecoveryAction(JobRecoveryActionType.RETRY_SEND);
            errorEntity.setCount(1);
        }

        boolean shouldVerifyTicket = documentEntity.getSunatResponse() != null && documentEntity.getSunatResponse().getTicket() != null;
        documentEntity.setJobInProgress(shouldVerifyTicket);
        documentEntity.persist();

        QuarkusTransaction.commit();

        if (shouldVerifyTicket) {
            schedulerManager.sendVerifyTicketAtSUNAT(documentEntity);
        }
    }

    @Blocking
    @ConsumeEvent(VertxScheduler.VERTX_CHECK_TICKET_SCHEDULER_BUS_NAME)
    public void checkTicket(String documentId) {
        QuarkusTransaction.begin();

        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            throw new IllegalStateException("Document id=" + documentId + " was not found for being sent");
        }

        try {
            XMLSenderConfig sunatConfig = xmlSenderManager.getXSenderConfig(documentEntity.getProjectId(), documentEntity.getXmlData().getRuc());

            XmlContent xmlContentModel = new XmlContent();
            xmlContentModel.setRuc(documentEntity.getXmlData().getRuc());
            xmlContentModel.setDocumentType(documentEntity.getXmlData().getTipoDocumento());
            xmlContentModel.setDocumentID(documentEntity.getXmlData().getSerieNumero());
            xmlContentModel.setVoidedLineDocumentTypeCode(documentEntity.getXmlData().getBajaCodigoTipoDocumento());

            // Send file to SUNAT
            SunatResponse sunatResponse = xmlSenderManager.verifyTicketAtSUNAT(documentEntity.getSunatResponse().getTicket(), xmlContentModel, sunatConfig);

            if (documentEntity.getSunatResponse() == null) {
                documentEntity.setSunatResponse(new SUNATResponseEntity());
            }

            documentEntity.getSunatResponse().setTicket(sunatResponse.getSunat().getTicket());
            documentEntity.getSunatResponse().setCode(sunatResponse.getMetadata().getResponseCode());
            documentEntity.getSunatResponse().setDescription(sunatResponse.getMetadata().getDescription());
            documentEntity.getSunatResponse().setStatus(sunatResponse.getStatus() != null ? sunatResponse.getStatus().toString() : null);
            documentEntity.getSunatResponse().setNotes(sunatResponse.getMetadata().getNotes() != null ? new HashSet<>(sunatResponse.getMetadata().getNotes()) : null);

            // Save CDR
            if (sunatResponse.getSunat() != null && sunatResponse.getSunat().getCdr() != null) {
                byte[] cdr = sunatResponse.getSunat().getCdr();
                String cdrFileId = filesManager.createFile(cdr, false);
                documentEntity.setCdrFileId(cdrFileId);
            }

            // Final task
            documentEntity.setJobInProgress(false);
        } catch (ConnectToSUNATException e) {
            ErrorEntity errorEntity = documentEntity.getError();
            if (errorEntity == null) {
                errorEntity = new ErrorEntity();
                documentEntity.setError(errorEntity);
            }

            errorEntity.setPhase(JobPhaseType.VERIFY_TICKET);
            errorEntity.setDescription("No se pudo verificar el ticket en la SUNAT");
            errorEntity.setRecoveryAction(JobRecoveryActionType.RETRY_SEND);
            errorEntity.setCount(1);
        }

        documentEntity.persist();
        QuarkusTransaction.commit();
    }
}

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
package io.github.project.openubl.xsender.kafka.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnknownWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.ValidationWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.SmartBillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.custom.CustomSmartBillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentProvider;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.kafka.idm.UBLDocumentSunatEventRepresentation;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.scheduler.Scheduler;
import io.github.project.openubl.xsender.xsender.AppBillServiceConfig;
import io.smallrye.common.annotation.Blocking;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;
import org.quartz.SchedulerException;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DocumentEvents {

    private static final Logger LOG = Logger.getLogger(DocumentEvents.class);

    static final int MAX_STRING = 250;

    public static final String INVALID_FILE_MSG = "Documento no soportado";

    public static final String NS_NOT_FOUND = "Namespace no encontrado";
    public static final String RUC_IN_COMPANY_NOT_FOUND = "No se pudo encontrar una empresa para el RUC especificado";

    @Inject
    Scheduler scheduler;

    @Inject
    FilesManager filesManager;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @Blocking
    @Transactional
    @Incoming("read-document")
    @Outgoing("send-document-sunat")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public UBLDocumentSunatEventRepresentation readDocumentContent(String payload) {
        UBLDocumentSunatEventRepresentation eventRep;

        try {
            String unescapedPayload = objectMapper.readValue(payload, String.class);
            eventRep = objectMapper.readValue(unescapedPayload, UBLDocumentSunatEventRepresentation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }


        UBLDocumentEntity documentEntity = documentRepository.findById(eventRep.getId());

        // Read XML file

        byte[] file = filesManager.getFileAsBytesAfterUnzip(eventRep.getStorageFile());

        XmlContentModel fileContent = null;
        boolean isFileValid;
        String fileValidationError = null;
        try {
            fileContent = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));

            Optional<DocumentType> documentTypeOptional = DocumentType.valueFromDocumentType(fileContent.getDocumentType());
            if (documentTypeOptional.isPresent()) {
                isFileValid = true;
            } else {
                isFileValid = false;
                fileValidationError = INVALID_FILE_MSG;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            isFileValid = false;
            fileValidationError = StringUtils.abbreviate(e.getMessage(), MAX_STRING);
        }

        if (fileContent != null) {
            documentEntity.setRuc(fileContent.getRuc());
            documentEntity.setDocumentID(fileContent.getDocumentID());
            documentEntity.setDocumentType(fileContent.getDocumentType());
            documentEntity.setVoidedLineDocumentTypeCode(fileContent.getVoidedLineDocumentTypeCode());
        }

        documentEntity.setFileValid(isFileValid);
        documentEntity.setFileValidationError(fileValidationError);

        if (!isFileValid) {
            documentEntity.setInProgress(false);
            documentRepository.persist(documentEntity);
            return null;
        }

        // Fetch correct company

        NamespaceEntity namespaceEntity = namespaceRepository.findByName(eventRep.getNamespace()).orElse(null);
        if (namespaceEntity == null) {
            documentEntity.setInProgress(false);
            documentEntity.setError(NS_NOT_FOUND);
            documentRepository.persist(documentEntity);

            LOG.error("Could not find a namespace for the given Event");
            return null;
        }

        CompanyEntity companyEntity = companyRepository.findByRuc(namespaceEntity, fileContent.getRuc()).orElse(null);
        if (companyEntity == null) {
            documentEntity.setInProgress(false);
            documentEntity.setError(RUC_IN_COMPANY_NOT_FOUND);
            documentRepository.persist(documentEntity);

            LOG.warn("Could not find a company with RUC:" + fileContent.getRuc() + " and can not continue.");
            return null;
        }

        //

        documentRepository.persist(documentEntity);

        // Enrich event

        eventRep.setSunatUsername(companyEntity.getSunatCredentials().getSunatUsername());
        eventRep.setSunatPassword(companyEntity.getSunatCredentials().getSunatPassword());

        eventRep.setSunatUrlFactura(companyEntity.getSunatUrls().getSunatUrlFactura());
        eventRep.setSunatUrlGuiaRemision(companyEntity.getSunatUrls().getSunatUrlGuiaRemision());
        eventRep.setSunatUrlPercepcionRetencion(companyEntity.getSunatUrls().getSunatUrlPercepcionRetencion());

        return eventRep;
    }

    @Blocking
    @Transactional
    @Incoming("send-document-sunat-incoming")
    @Outgoing("verify-ticket-sunat")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public UBLDocumentSunatEventRepresentation sendDocument(String payload) {
        UBLDocumentSunatEventRepresentation eventRep;

        try {
            eventRep = objectMapper.readValue(payload, UBLDocumentSunatEventRepresentation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        UBLDocumentEntity documentEntity = documentRepository.findById(eventRep.getId());

        // If file is valid then send it to SUNAT
        BillServiceModel billServiceModel;
        try {
            byte[] file = filesManager.getFileAsBytesAfterUnzip(eventRep.getStorageFile());

            AppBillServiceConfig billServiceConfig = new AppBillServiceConfig(eventRep.getSunatUrlFactura(), eventRep.getSunatUrlPercepcionRetencion(), eventRep.getSunatUrlGuiaRemision());
            SmartBillServiceModel smartBillServiceModel = CustomSmartBillServiceManager.send(file, eventRep.getSunatUsername(), eventRep.getSunatPassword(), billServiceConfig);

            billServiceModel = smartBillServiceModel.getBillServiceModel();
        } catch (UnknownWebServiceException e) {
            int retries = documentEntity.getRetries();
            Date date = null;
            if (retries <= 2) {
                try {
                    retries++;
                    date = scheduler.scheduleSend(documentEntity, retries);
                } catch (SchedulerException schedulerException) {
                    LOG.error(schedulerException);
                }
            }

            // Save
            documentEntity.setInProgress(false);
            documentEntity.setRetries(retries);
            documentEntity.setWillRetryOn(date);
            documentRepository.persist(documentEntity);
            return null;
        } catch (ValidationWebServiceException e) {
            // The error code in the
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            documentEntity.setInProgress(false);
            documentEntity.setError(StringUtils.abbreviate(e.getMessage(), MAX_STRING));
            documentRepository.persist(documentEntity);

            LOG.error(e);
            return null;
        }

        // Save CDR
        String cdrID = null;
        if (billServiceModel != null && billServiceModel.getCdr() != null) {
            cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);
        }

        documentEntity.setStorageCdr(cdrID);

        // Set billService data
        String ticket = null;
        if (billServiceModel != null) {
            documentEntity.setSunatStatus(billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null);
            documentEntity.setSunatCode(billServiceModel.getCode());
            documentEntity.setSunatDescription(billServiceModel.getDescription());
            documentEntity.setSunatTicket(billServiceModel.getTicket());
            documentEntity.setSunatNotes(billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null);

            ticket = billServiceModel.getTicket();
        }

        if (ticket == null) {
            documentEntity.setInProgress(false);
            documentRepository.persist(documentEntity);
            return null;
        }

        eventRep.setTicket(ticket);
        return eventRep;
    }

    @Blocking
    @Transactional
    @Incoming("verify-ticket-sunat-incoming")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public void checkTicket(String payload) {
        UBLDocumentSunatEventRepresentation eventRep;

        try {
            eventRep = objectMapper.readValue(payload, UBLDocumentSunatEventRepresentation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        UBLDocumentEntity documentEntity = documentRepository.findById(eventRep.getId());

        XmlContentModel xmlContentModel = new XmlContentModel();
        xmlContentModel.setRuc(documentEntity.getRuc());
        xmlContentModel.setDocumentType(documentEntity.getDocumentType());
        xmlContentModel.setDocumentID(documentEntity.getDocumentID());
        xmlContentModel.setVoidedLineDocumentTypeCode(documentEntity.getVoidedLineDocumentTypeCode());


        // If there is ticket then verify it
        BillServiceModel billServiceModel;
        try {
            AppBillServiceConfig billServiceConfig = new AppBillServiceConfig(
                    eventRep.getSunatUrlFactura(),
                    eventRep.getSunatUrlPercepcionRetencion(),
                    eventRep.getSunatUrlGuiaRemision()
            );

            billServiceModel = CustomSmartBillServiceManager.getStatus(
                    eventRep.getTicket(),
                    xmlContentModel,
                    eventRep.getSunatUsername(),
                    eventRep.getSunatPassword(),
                    billServiceConfig
            );
        } catch (UnknownWebServiceException e) {
            int retries = documentEntity.getRetries();
            Date date = null;
            if (retries <= 2) {
                try {
                    retries++;
                    date = scheduler.scheduleSend(documentEntity, retries);
                } catch (SchedulerException schedulerException) {
                    LOG.error(schedulerException);
                }
            }

            // Save
            documentEntity.setInProgress(false);
            documentEntity.setRetries(retries);
            documentEntity.setWillRetryOn(date);
            documentRepository.persist(documentEntity);
            return;
        } catch (ValidationWebServiceException e) {
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
        }

        // Save CDR
        String cdrID = null;
        if (billServiceModel != null && billServiceModel.getCdr() != null) {
            cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);
        }

        documentEntity.setStorageCdr(cdrID);

        // Set billService data
        if (billServiceModel != null) {
            documentEntity.setSunatStatus(billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null);
            documentEntity.setSunatCode(billServiceModel.getCode());
            documentEntity.setSunatDescription(billServiceModel.getDescription());
            documentEntity.setSunatTicket(billServiceModel.getTicket());
            documentEntity.setSunatNotes(billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null);
        }

        documentEntity.setInProgress(false);
        documentRepository.persist(documentEntity);
    }

}

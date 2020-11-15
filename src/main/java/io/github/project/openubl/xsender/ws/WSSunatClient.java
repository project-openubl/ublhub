/**
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
package io.github.project.openubl.xsender.ws;

import io.github.project.openubl.xsender.events.EventProvider;
import io.github.project.openubl.xsender.events.EventProviderLiteral;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.github.project.openubl.xsender.models.DocumentEvent;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnknownWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.ValidationWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.managers.BillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.wrappers.ServiceConfig;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Transactional
@ApplicationScoped
public class WSSunatClient {

    private static final Logger LOG = Logger.getLogger(WSSunatClient.class);

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    FilesManager filesManager;

    @ConfigProperty(name = "openubl.event-manager.type")
    EventProvider.Type eventManager;

    @Inject
    Event<DocumentEvent.Delivered> documentDeliveredEvent;

    @Inject
    Event<DocumentEvent.RequireCheckTicket> documentRequireCheckTicketEvent;

    private String getDeliveryUrl(UBLDocumentEntity document) {
        CompanyEntity company = document.getCompany();

        SunatUrlsEntity sunatUrls = company.getSunatUrls();

        switch (document.getDeliveryType()) {
            case BASIC_DOCUMENTS_URL:
                return sunatUrls.getSunatUrlFactura();
            case PERCEPTION_RETENTION_URL:
                return sunatUrls.getSunatUrlPercepcionRetencion();
            case DESPATCH_DOCUMENT_URL:
                return sunatUrls.getSunatUrlGuiaRemision();
            default:
                throw new IllegalArgumentException("DeliveryURLType not supported");
        }
    }

    private SunatCredentialsEntity getCredentials(UBLDocumentEntity document) {
        CompanyEntity company = document.getCompany();
        return company.getSunatCredentials();
    }

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean sendDocument(String documentId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            LOG.warnf("Document[%s] does not exists", documentId);
            return true;
        }

        DocumentType documentType = documentEntity.getDocumentType();
        byte[] file = filesManager.getFileAsBytesWithoutUnzipping(documentEntity.getStorageFile());

        String deliveryUrl = getDeliveryUrl(documentEntity);
        SunatCredentialsEntity credentials = getCredentials(documentEntity);

        // Send to SUNAT
        ServiceConfig config = new ServiceConfig.Builder()
                .url(deliveryUrl)
                .username(credentials.getSunatUsername())
                .password(credentials.getSunatPassword())
                .build();

        BillServiceModel billServiceModel;
        try {
            switch (documentType) {
                case INVOICE:
                case CREDIT_NOTE:
                case DEBIT_NOTE:
                case PERCEPTION:
                case RETENTION:
                    billServiceModel = BillServiceManager.sendBill(FileType.getFilename(documentEntity.getFilename(), FileType.ZIP), file, config);
                    processCDR(billServiceModel, documentEntity);
                    break;
                case VOIDED_DOCUMENT:
                case SUMMARY_DOCUMENT:
                    billServiceModel = BillServiceManager.sendSummary(FileType.getFilename(documentEntity.getFilename(), FileType.ZIP), file, config);
                    processTicket(billServiceModel, documentEntity);
                    break;
                default:
                    LOG.warn("Unsupported file, will acknowledge and delete message:" + documentType);
                    return true;
            }
        } catch (UnknownWebServiceException e) {
            LOG.error(e);
            return false;
        } catch (ValidationWebServiceException e) {
            LOG.error(e.getMessage());
            handleValidationWebServiceException(e, documentEntity);
            return true;
        }

        return true;
    }

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean checkDocumentTicket(String documentId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);

        String deliveryUrl = getDeliveryUrl(documentEntity);
        SunatCredentialsEntity credentials = getCredentials(documentEntity);

        // Send to SUNAT
        BillServiceModel billServiceModel;
        try {
            ServiceConfig config = new ServiceConfig.Builder()
                    .url(deliveryUrl)
                    .username(credentials.getSunatUsername())
                    .password(credentials.getSunatPassword())
                    .build();
            billServiceModel = BillServiceManager.getStatus(documentEntity.getSunatTicket(), config);
        } catch (UnknownWebServiceException e) {
            LOG.error(e);
            return false;
        } catch (ValidationWebServiceException e) {
            LOG.error(e.getMessage());
            handleValidationWebServiceException(e, documentEntity);
            return true;
        }

        processCDR(billServiceModel, documentEntity);
        return true;
    }


    private void handleValidationWebServiceException(ValidationWebServiceException e, UBLDocumentEntity documentEntity) {
        documentEntity.setSunatCode(e.getSUNATErrorCode());
        documentEntity.setSunatDescription(e.getSUNATErrorMessage(255));
        documentEntity.setSunatStatus(BillServiceModel.Status.RECHAZADO.toString());
        documentEntity.setDeliveryStatus(DeliveryStatusType.DELIVERED);

        documentRepository.persist(documentEntity);

        // Fire event
        documentDeliveredEvent
                .select(new EventProviderLiteral(eventManager))
                .fire(documentEntity::getId);
    }

    private void processCDR(BillServiceModel billServiceModel, UBLDocumentEntity documentEntity) {
        // Save CDR in storage
        String cdrID = null;
        if (billServiceModel.getCdr() != null) {
            // The filename does not really matter here
            cdrID = filesManager.createFile(billServiceModel.getCdr(), documentEntity.getFilename(), FileType.ZIP);
        }

        // Update DB
        documentEntity.setStorageCdr(cdrID);
        documentEntity.setSunatCode(billServiceModel.getCode());
        documentEntity.setSunatDescription(billServiceModel.getDescription());
        documentEntity.setSunatStatus(billServiceModel.getStatus().toString());
        documentEntity.setDeliveryStatus(DeliveryStatusType.DELIVERED);

        documentRepository.persist(documentEntity);

        // Fire event
        documentDeliveredEvent
                .select(new EventProviderLiteral(eventManager))
                .fire(documentEntity::getId);
    }

    private void processTicket(BillServiceModel billServiceModel, UBLDocumentEntity documentEntity) {
        // Update DB
        documentEntity.setDeliveryStatus(DeliveryStatusType.SCHEDULED_CHECK_TICKET);
        documentEntity.setSunatTicket(billServiceModel.getTicket());

        documentEntity.persist();

        // Fire event
        documentRequireCheckTicketEvent
                .select(new EventProviderLiteral(eventManager))
                .fire(documentEntity::getId);
    }

}

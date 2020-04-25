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
package io.github.project.openubl.xmlsender.ws;

import io.github.project.openubl.xmlsender.managers.FilesManager;
import io.github.project.openubl.xmlsender.models.DocumentEvent;
import io.github.project.openubl.xmlsender.models.DocumentType;
import io.github.project.openubl.xmlsender.models.DeliveryStatusType;
import io.github.project.openubl.xmlsender.models.FileType;
import io.github.project.openubl.xmlsender.models.jpa.DocumentRepository;
import io.github.project.openubl.xmlsender.models.jpa.entities.DocumentEntity;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnknownWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.managers.BillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.wrappers.ServiceConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.util.Optional;

@Transactional
@ApplicationScoped
public class WSSunatClient {

    private static final Logger LOG = Logger.getLogger(WSSunatClient.class);

    @Inject
    DocumentRepository documentRepository;

    @Inject
    FilesManager filesManager;

    @ConfigProperty(name = "openubl.sunat.username")
    Optional<String> sunatUsername;

    @ConfigProperty(name = "openubl.sunat.password")
    Optional<String> sunatPassword;

    @Inject
    Event<DocumentEvent.Ready> documentReadyEvent;

    @Inject
    Event<DocumentEvent.RequireCheckTicket> documentRequireCheckTicketEvent;

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    public boolean sendDocument(Long documentId) {
        DocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            LOG.warnf("Document[%s] does not exists", documentId);
            return true;
        }

        DocumentType documentType = documentEntity.documentType;
        byte[] file = filesManager.getFileAsBytesWithoutUnzipping(documentEntity.fileID);

        // Send to SUNAT
        ServiceConfig config = new ServiceConfig.Builder()
                .url(documentEntity.deliveryURL)
                .username(documentEntity.sunatUsername != null ? documentEntity.sunatUsername : sunatUsername.orElseThrow(IllegalStateException::new))
                .password(documentEntity.sunatPassword != null ? documentEntity.sunatPassword : sunatPassword.orElseThrow(IllegalStateException::new))
                .build();

        BillServiceModel billServiceModel;
        try {
            switch (documentType) {
                case INVOICE:
                case CREDIT_NOTE:
                case DEBIT_NOTE:
                    billServiceModel = BillServiceManager.sendBill(FileType.getFilename(documentEntity.filenameWithoutExtension, FileType.ZIP), file, config);
                    processCDR(billServiceModel, documentEntity);
                    break;
                case VOIDED_DOCUMENT:
                case SUMMARY_DOCUMENT:
                    billServiceModel = BillServiceManager.sendSummary(FileType.getFilename(documentEntity.filenameWithoutExtension, FileType.ZIP), file, config);
                    processTicket(billServiceModel, documentEntity);
                    break;
                default:
                    LOG.warn("Unsupported file, will acknowledge and delete message:" + documentType);
                    return true;
            }
        } catch (IOException | UnknownWebServiceException e) {
            LOG.error(e);
            return false;
        }

        return true;
    }

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    public boolean checkDocumentTicket(long documentId) {
        DocumentEntity documentEntity = documentRepository.findById(documentId);

        // Send to SUNAT
        BillServiceModel billServiceModel;
        try {
            ServiceConfig config = new ServiceConfig.Builder()
                    .url(documentEntity.deliveryURL)
                    .username(documentEntity.sunatUsername != null ? documentEntity.sunatUsername : sunatUsername.orElseThrow(IllegalStateException::new))
                    .password(documentEntity.sunatPassword != null ? documentEntity.sunatPassword : sunatPassword.orElseThrow(IllegalStateException::new))
                    .build();
            billServiceModel = BillServiceManager.getStatus(documentEntity.sunatTicket, config);
        } catch (UnknownWebServiceException e) {
            LOG.error(e);
            return false;
        }

        processCDR(billServiceModel, documentEntity);
        return true;
    }

    private void processCDR(BillServiceModel billServiceModel, DocumentEntity documentEntity) {
        // Save CDR in storage
        String cdrID = null;
        if (billServiceModel.getCdr() != null) {
            // The filename does not really matter here
            cdrID = filesManager.createFile(billServiceModel.getCdr(), documentEntity.filenameWithoutExtension, FileType.ZIP);
        }

        // Update DB
        documentEntity.setCdrID(cdrID);
        documentEntity.setSunatCode(billServiceModel.getCode());
        documentEntity.setSunatDescription(billServiceModel.getDescription());
        documentEntity.setSunatStatus(billServiceModel.getStatus().toString());
        documentEntity.setDeliveryStatus(DeliveryStatusType.DELIVERED);

        documentRepository.persist(documentEntity);

        // Event
        documentReadyEvent.fire(() -> documentEntity.id);
    }

    private void processTicket(BillServiceModel billServiceModel, DocumentEntity documentEntity) {
        // Update DB
        documentEntity.setDeliveryStatus(DeliveryStatusType.SCHEDULED_CHECK_TICKET);
        documentEntity.setSunatTicket(billServiceModel.getTicket());

        documentEntity.persist();

        // JMS
        documentRequireCheckTicketEvent.fire(() -> documentEntity.id);
    }

}

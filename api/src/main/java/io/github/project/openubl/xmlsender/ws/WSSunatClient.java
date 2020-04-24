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
import javax.inject.Inject;
import javax.jms.*;
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

    @Inject
    ConnectionFactory connectionFactory;

    @ConfigProperty(name = "openubl.jms.delay")
    Long messageDelay;

    @ConfigProperty(name = "openubl.jms.callbackQueue")
    String callbackQueue;

    @ConfigProperty(name = "openubl.jms.ticketQueue")
    String ticketQueue;

    @ConfigProperty(name = "openubl.sunat.username")
    Optional<String> sunatUsername;

    @ConfigProperty(name = "openubl.sunat.password")
    Optional<String> sunatPassword;

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    public boolean sendDocument(Long documentId) {
        DocumentEntity deliveryEntity = documentRepository.findById(documentId);

        DocumentType documentType = deliveryEntity.documentType;
        byte[] file = filesManager.getFileAsBytesWithoutUnzipping(deliveryEntity.fileID);

        // Send to SUNAT
        ServiceConfig config = new ServiceConfig.Builder()
                .url(deliveryEntity.deliveryURL)
                .username(deliveryEntity.sunatUsername != null ? deliveryEntity.sunatUsername : sunatUsername.orElseThrow(IllegalStateException::new))
                .password(deliveryEntity.sunatPassword != null ? deliveryEntity.sunatPassword : sunatPassword.orElseThrow(IllegalStateException::new))
                .build();

        BillServiceModel billServiceModel;
        try {
            switch (documentType) {
                case INVOICE:
                case CREDIT_NOTE:
                case DEBIT_NOTE:
                    billServiceModel = BillServiceManager.sendBill(FileType.getFilename(deliveryEntity.filenameWithoutExtension, FileType.ZIP), file, config);
                    processCDR(billServiceModel, deliveryEntity);
                    break;
                case VOIDED_DOCUMENT:
                case SUMMARY_DOCUMENT:
                    billServiceModel = BillServiceManager.sendSummary(FileType.getFilename(deliveryEntity.filenameWithoutExtension, FileType.ZIP), file, config);
                    processTicket(billServiceModel, deliveryEntity);
                    break;
                default:
                    LOG.warn("Unsupported file, will acknowledge and delete message:" + documentType);
                    return true;
            }
        } catch (IOException | UnknownWebServiceException e) {
            LOG.error(e);

            deliveryEntity.deliveryStatus = DeliveryStatusType.RESCHEDULED_TO_DELIVER;
            documentRepository.persist(deliveryEntity);

            return false;
        }

        return true;
    }

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    public boolean checkDocumentTicket(long documentId) {
        DocumentEntity deliveryEntity = documentRepository.findById(documentId);

        // Send to SUNAT
        BillServiceModel billServiceModel;
        try {
            ServiceConfig config = new ServiceConfig.Builder()
                    .url(deliveryEntity.deliveryURL)
                    .username(deliveryEntity.sunatUsername != null
                            ? deliveryEntity.sunatUsername
                            : sunatUsername.orElseThrow(() -> new IllegalStateException("Could not find a username for sending to SUNAT"))
                    )
                    .password(deliveryEntity.sunatPassword != null
                            ? deliveryEntity.sunatPassword
                            : sunatPassword.orElseThrow(() -> new IllegalStateException("Could not find a username for sending to SUNAT"))
                    )
                    .build();
            billServiceModel = BillServiceManager.getStatus(deliveryEntity.sunatTicket, config);
        } catch (UnknownWebServiceException e) {
            LOG.error(e);

            deliveryEntity.deliveryStatus = DeliveryStatusType.RESCHEDULED_CHECK_TICKET;
            documentRepository.persist(deliveryEntity);

            return false;
        }

        processCDR(billServiceModel, deliveryEntity);
        return true;
    }

    private void processCDR(BillServiceModel billServiceModel, DocumentEntity deliveryEntity) {
        // Save CDR in storage
        String cdrID = null;
        if (billServiceModel.getCdr() != null) {
            // The filename does not really matter here
            cdrID = filesManager.createFile(billServiceModel.getCdr(), deliveryEntity.filenameWithoutExtension, FileType.ZIP);
        }

        // Update DB
        deliveryEntity.cdrID = cdrID;
        deliveryEntity.sunatCode = billServiceModel.getCode();
        deliveryEntity.sunatDescription = billServiceModel.getDescription();
        deliveryEntity.sunatStatus = billServiceModel.getStatus().toString();
        deliveryEntity.deliveryStatus = DeliveryStatusType.SCHEDULED_CALLBACK;

        documentRepository.persist(deliveryEntity);

        // JMS
        produceCallbackMessage(deliveryEntity);
    }

    private void processTicket(BillServiceModel billServiceModel, DocumentEntity deliveryEntity) {
        // Update DB
        deliveryEntity.deliveryStatus = DeliveryStatusType.SCHEDULED_CHECK_TICKET;
        deliveryEntity.sunatTicket = billServiceModel.getTicket();

        deliveryEntity.persist();

        // JMS
        produceTicketMessage(deliveryEntity);
    }

    public void produceTicketMessage(DocumentEntity deliveryEntity) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(messageDelay);
            Queue queue = context.createQueue(ticketQueue);
            Message message = context.createTextMessage(deliveryEntity.id.toString());
            producer.send(queue, message);
        }
    }

    public void produceCallbackMessage(DocumentEntity deliveryEntity) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(messageDelay);
            Queue queue = context.createQueue(callbackQueue);
            Message message = context.createTextMessage(deliveryEntity.id.toString());
            producer.send(queue, message);
        }
    }
}

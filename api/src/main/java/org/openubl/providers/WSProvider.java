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
package org.openubl.providers;

import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnknownWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.managers.BillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.wrappers.ServiceConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.managers.FilesManager;
import org.openubl.models.DocumentType;
import org.openubl.models.FileDeliveryStatusType;
import org.openubl.models.FileType;
import org.openubl.models.jpa.FileDeliveryRepository;
import org.openubl.models.jpa.entities.FileDeliveryEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.util.Optional;

@ApplicationScoped
public class WSProvider {

    private static final Logger LOG = Logger.getLogger(WSProvider.class);

    @Inject
    FileDeliveryRepository fileDeliveryRepository;

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
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean sendFileDelivery(Long id) {
        FileDeliveryEntity deliveryEntity = fileDeliveryRepository.findById(id);
        if (deliveryEntity == null) {
            LOG.warn("Not found entity, will not acknowledge");
            return false;
        }

        DocumentType documentType = deliveryEntity.documentType;

        byte[] file = filesManager.getFileAsBytesWithoutUnzipping(deliveryEntity.fileID);

        // Send to SUNAT
        ServiceConfig config = new ServiceConfig.Builder()
                .url(deliveryEntity.serverUrl)
                .username(deliveryEntity.sunatUsername != null ? deliveryEntity.sunatUsername : sunatUsername.orElseThrow(IllegalStateException::new))
                .password(deliveryEntity.sunatPassword != null ? deliveryEntity.sunatPassword : sunatPassword.orElseThrow(IllegalStateException::new))
                .build();

        BillServiceModel billServiceModel;
        try {
            switch (documentType) {
                case INVOICE:
                case CREDIT_NOTE:
                case DEBIT_NOTE:
                    billServiceModel = BillServiceManager.sendBill(deliveryEntity.filename + ".zip", file, config);
                    processCDR(billServiceModel, deliveryEntity);
                    break;
                case VOIDED_DOCUMENT:
                case SUMMARY_DOCUMENT:
                    billServiceModel = BillServiceManager.sendSummary(deliveryEntity.filename + ".zip", file, config);
                    processTicket(billServiceModel, deliveryEntity);
                    break;
                default:
                    LOG.warn("Unsupported file, will acknowledge and delete message:" + documentType);
                    return true;
            }
        } catch (IOException | UnknownWebServiceException e) {
            LOG.error(e);

            deliveryEntity = fileDeliveryRepository.findById(id);
            deliveryEntity.deliveryStatus = FileDeliveryStatusType.RESCHEDULED_TO_DELIVER;
            fileDeliveryRepository.persist(deliveryEntity);

            return false;
        }

        return true;
    }

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean checkTicket(long fileDeliveryID) {
        FileDeliveryEntity deliveryEntity = fileDeliveryRepository.findById(fileDeliveryID);
        if (deliveryEntity == null) {
            LOG.warn("Not found entity, will not acknowledge");
            return false;
        }

        DocumentType documentType = deliveryEntity.documentType;

        // Send to SUNAT
        BillServiceModel billServiceModel;
        try {
            ServiceConfig config = new ServiceConfig.Builder()
                    .url(deliveryEntity.serverUrl)
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

            deliveryEntity = fileDeliveryRepository.findById(fileDeliveryID);
            deliveryEntity.deliveryStatus = FileDeliveryStatusType.RESCHEDULED_CHECK_TICKET;
            fileDeliveryRepository.persist(deliveryEntity);

            return false;
        }

        processCDR(billServiceModel, deliveryEntity);
        return true;
    }

    @Transactional
    private void processCDR(BillServiceModel billServiceModel, FileDeliveryEntity deliveryEntity) {
        // Save CDR in storage
        String cdrID = null;
        if (billServiceModel.getCdr() != null) {
            // The filename does not really matter here
            cdrID = filesManager.uploadFile(billServiceModel.getCdr(), deliveryEntity.filename, FileType.ZIP);
        }

        // Update DB
        deliveryEntity.cdrID = cdrID;
        deliveryEntity.sunatCode = billServiceModel.getCode();
        deliveryEntity.sunatDescription = billServiceModel.getDescription();
        deliveryEntity.sunatStatus = billServiceModel.getStatus().toString();
        deliveryEntity.deliveryStatus = FileDeliveryStatusType.SCHEDULED_CALLBACK;

        fileDeliveryRepository.persist(deliveryEntity);

        // JMS
        produceCallbackMessage(deliveryEntity);
    }

    @Transactional
    private void processTicket(BillServiceModel billServiceModel, FileDeliveryEntity deliveryEntity) {
        // Update DB
        deliveryEntity.deliveryStatus = FileDeliveryStatusType.SCHEDULED_CHECK_TICKET;
        deliveryEntity.sunatTicket = billServiceModel.getTicket();

        deliveryEntity.persist();

        // JMS
        produceTicketMessage(deliveryEntity);
    }

    public void produceTicketMessage(FileDeliveryEntity deliveryEntity) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(messageDelay);
            Queue queue = context.createQueue(ticketQueue);
            Message message = context.createTextMessage(deliveryEntity.id.toString());
            producer.send(queue, message);
        }
    }

    public void produceCallbackMessage(FileDeliveryEntity deliveryEntity) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(messageDelay);
            Queue queue = context.createQueue(callbackQueue);
            Message message = context.createTextMessage(deliveryEntity.id.toString());
            producer.send(queue, message);
        }
    }
}

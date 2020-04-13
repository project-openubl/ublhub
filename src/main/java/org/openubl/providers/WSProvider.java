package org.openubl.providers;

import io.github.carlosthe19916.webservices.exceptions.UnknownWebServiceException;
import io.github.carlosthe19916.webservices.managers.BillServiceManager;
import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import io.github.carlosthe19916.webservices.wrappers.ServiceConfig;
import org.apache.camel.CamelContext;
import org.jboss.logging.Logger;
import org.openubl.exceptions.WSNotAvailableException;
import org.openubl.jms.SendCallbackJMSProducer;
import org.openubl.models.DocumentType;
import org.openubl.models.FileDeliveryStatusType;
import org.openubl.models.MessageModel;
import org.openubl.models.jpa.FileDeliveryRepository;
import org.openubl.models.jpa.entities.FileDeliveryEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.transaction.Transactional;
import java.io.IOException;

@ApplicationScoped
public class WSProvider {

    private static final Logger LOG = Logger.getLogger(WSProvider.class);

    @Inject
    SendCallbackJMSProducer sendCallbackJMSProducer;

    @Inject
    FileDeliveryRepository fileDeliveryRepository;

    @Inject
    CamelContext camelContext;

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean sendFile(MessageModel messageModel, byte[] file) {
        FileDeliveryEntity deliveryEntity = fileDeliveryRepository.findById(messageModel.getEntityId());
        if (deliveryEntity == null) {
            LOG.warn("Not found entity, will not acknowledge");
            return false;
        }

        DocumentType documentType = deliveryEntity.documentType;

        // Send to SUNAT
        ServiceConfig config = new ServiceConfig.Builder()
                .url(deliveryEntity.serverUrl)
                .username(messageModel.getUsername())
                .password(messageModel.getPassword())
                .build();

        // Send file to SUNAT
        BillServiceModel billServiceModel;
        try {
            switch (documentType) {
                case INVOICE:
                case CREDIT_NOTE:
                case DEBIT_NOTE:
                    billServiceModel = BillServiceManager.sendBill(deliveryEntity.filename, file, config);
                    processCDR(billServiceModel, deliveryEntity);
                    break;
                case VOIDED_DOCUMENT:
                case SUMMARY_DOCUMENT:
                    billServiceModel = BillServiceManager.sendSummary(deliveryEntity.filename, file, config);
                    processTicket(billServiceModel, deliveryEntity);
                    break;
                default:
                    LOG.warn("Unsupported file, will acknowledge and delete message:" + documentType);
                    return true;
            }
        } catch (IOException | UnknownWebServiceException e) {
            LOG.error(e);

            deliveryEntity = fileDeliveryRepository.findById(messageModel.getEntityId());
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
    public boolean checkTicket(MessageModel messageModel, String ticket) {
        FileDeliveryEntity deliveryEntity = fileDeliveryRepository.findById(messageModel.getEntityId());
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
                    .username(messageModel.getUsername())
                    .password(messageModel.getPassword())
                    .build();
            billServiceModel = BillServiceManager.getStatus(ticket, config);
        } catch (UnknownWebServiceException e) {
            LOG.error(e);

            deliveryEntity = fileDeliveryRepository.findById(messageModel.getEntityId());
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
            cdrID = camelContext.createProducerTemplate().requestBody("direct:save-file", billServiceModel.getCdr(), String.class);
        }

        // Update DB
        deliveryEntity.cdrID = cdrID;
        deliveryEntity.sunatCode = billServiceModel.getCode();
        deliveryEntity.sunatDescription = billServiceModel.getDescription();
        deliveryEntity.sunatStatus = billServiceModel.getStatus().toString();
        deliveryEntity.deliveryStatus = FileDeliveryStatusType.SCHEDULED_CALLBACK;

        fileDeliveryRepository.persist(deliveryEntity);

        // JMS
        try {
            sendCallbackJMSProducer.produceCDRMessage(billServiceModel);
        } catch (JMSException e) {
            LOG.error("Error trying to broker a cdr message:", e);

            deliveryEntity.deliveryStatus = FileDeliveryStatusType.ERROR_SCHEDULING_CALLBACK;
            fileDeliveryRepository.persist(deliveryEntity);
        }


    }

    @Transactional
    private void processTicket(BillServiceModel billServiceModel, FileDeliveryEntity deliveryEntity) {
        // Update DB
        deliveryEntity.deliveryStatus = FileDeliveryStatusType.SCHEDULED_CHECK_TICKET;
        deliveryEntity.sunatTicket = billServiceModel.getTicket();

        // JMS
        try {
            sendCallbackJMSProducer.produceTicketMessage(billServiceModel);
        } catch (JMSException e) {
            LOG.error("Error trying to send to broker a ticket message:", e);

            deliveryEntity.deliveryStatus = FileDeliveryStatusType.ERROR_SCHEDULING_CHECK_TICKET;
            fileDeliveryRepository.persist(deliveryEntity);
        }
    }

}

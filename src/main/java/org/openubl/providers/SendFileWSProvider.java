package org.openubl.providers;

import io.github.carlosthe19916.webservices.managers.BillServiceManager;
import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import io.github.carlosthe19916.webservices.wrappers.ServiceConfig;
import org.jboss.logging.Logger;
import org.openubl.jms.SendCallbackJMSProducer;
import org.openubl.jms.SendFileJMSProducer;
import org.openubl.models.DocumentType;
import org.openubl.models.SendFileModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class SendFileWSProvider {

    private static final Logger LOG = Logger.getLogger(SendFileWSProvider.class);

    @Inject
    SendCallbackJMSProducer sendCallbackJMSProducer;

    /**
     * @return true if message was processed and don't need to be redelivered
     */
    public boolean sendFile(SendFileModel sunatMessage, byte[] file) {

        DocumentType documentType = DocumentType.valueFromDocumentType(sunatMessage.getDocumentType())
                .orElseThrow(IllegalAccessError::new);

        BillServiceModel billServiceModel;

        try {
            switch (documentType) {
                case INVOICE:
                case CREDIT_NOTE:
                case DEBIT_NOTE:
                    billServiceModel = sendBill(sunatMessage, file);
                    break;
                case VOIDED_DOCUMENT:
                case SUMMARY_DOCUMENT:
                    billServiceModel = sendSummary(sunatMessage, file);
                    break;
                default:
                    LOG.warn("Unsupported file, will acknoledge:" + documentType);
                    return true;
            }
        } catch (IOException e) {
            LOG.warn("IOException occurred, we will save the file to try to redeliver again, message:" + e.getMessage());
            return true;
        }


        sendCallbackJMSProducer.produceSendCallbackMessage(billServiceModel);
        return true;
    }

    private BillServiceModel sendBill(SendFileModel messageModel, byte[] file) throws IOException {
        ServiceConfig config = new ServiceConfig.Builder()
                .url(messageModel.getServerUrl())
                .username(messageModel.getUsername())
                .password(messageModel.getPassword())
                .build();

        return BillServiceManager.sendBill(messageModel.getFileName(), file, config);
    }

    private BillServiceModel sendSummary(SendFileModel messageModel, byte[] file) throws IOException {
        ServiceConfig config = new ServiceConfig.Builder()
                .url(messageModel.getServerUrl())
                .username(messageModel.getServerUrl())
                .password(messageModel.getPassword())
                .build();

        return BillServiceManager.sendSummary(messageModel.getFileName(), file, config);
    }

}

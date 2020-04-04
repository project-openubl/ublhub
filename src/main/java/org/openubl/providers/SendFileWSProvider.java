package org.openubl.providers;

import io.github.carlosthe19916.webservices.managers.BillServiceManager;
import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import io.github.carlosthe19916.webservices.wrappers.ServiceConfig;
import org.openubl.jms.DefaultJMSProducer;
import org.openubl.models.DocumentType;
import org.openubl.models.SendFileModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class SendFileWSProvider {

    @Inject
    DefaultJMSProducer sendCallbackJMSProducer;

    public void sendFile(SendFileModel sunatMessage, byte[] file) throws IOException {
        DocumentType documentType = DocumentType.valueFromDocumentType(sunatMessage.getDocumentType())
                .orElseThrow(IllegalAccessError::new);

        BillServiceModel billServiceModel;
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
                throw new IllegalStateException("Assert unknown type of document=" + documentType);
        }

        sendCallbackJMSProducer.produceSendCallbackMessage(billServiceModel);
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

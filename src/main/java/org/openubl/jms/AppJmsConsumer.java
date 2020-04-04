package org.openubl.jms;

import io.github.carlosthe19916.webservices.managers.BillServiceManager;
import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import io.github.carlosthe19916.webservices.wrappers.ServiceConfig;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openubl.providers.DocumentType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class AppJmsConsumer implements Runnable {

    @Inject
    ConnectionFactory connectionFactory;

    @ConfigProperty(name = "openubl.queueName")
    String queueName;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    void onStart(@Observes StartupEvent ev) {
        scheduler.scheduleWithFixedDelay(this, 0L, 5L, TimeUnit.SECONDS);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer jmsConsumer = context.createConsumer(context.createQueue(queueName));
            while (true) {
                Message message = jmsConsumer.receive();
                if (message == null) {
                    return;
                }

                Map<String, String> properties = new HashMap<>();
                Enumeration<?> enumeration = message.getPropertyNames();
                if (enumeration != null) {
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        Object value = message.getObjectProperty(key);
                        if (value instanceof String) {
                            properties.put(key, (String) value);
                        }
                    }
                }

                SunatJMSMessageModel sunatMessage = new SunatJMSMessageModel(properties);
                sendDocument(sunatMessage, message.getBody(byte[].class));
            }
        } catch (JMSException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendDocument(SunatJMSMessageModel sunatMessage, byte[] file) throws IOException {
        DocumentType documentType = DocumentType.valueFromDocumentType(sunatMessage.getDocumentType())
                .orElseThrow(IllegalAccessError::new);
        switch (documentType) {
            case INVOICE:
            case CREDIT_NOTE:
            case DEBIT_NOTE:
                sendBill(sunatMessage, file);
                break;
            case VOIDED_DOCUMENT:
            case SUMMARY_DOCUMENT:
                sendSummary(sunatMessage, file);
                break;
            default:
                throw new IllegalStateException("Assert unknown type of document=" + documentType);
        }
    }

    private void sendBill(SunatJMSMessageModel messageModel, byte[] file) throws IOException {
        ServiceConfig config = new ServiceConfig.Builder()
                .url(messageModel.getServerUrl())
                .username(messageModel.getUsername())
                .password(messageModel.getPassword())
                .build();

        BillServiceModel billServiceModel = BillServiceManager.sendBill(
                messageModel.getFileName(), file, config
        );

        informResult(billServiceModel);
    }

    private void sendSummary(SunatJMSMessageModel messageModel, byte[] file) throws IOException {
        ServiceConfig config = new ServiceConfig.Builder()
                .url(messageModel.getServerUrl())
                .username(messageModel.getServerUrl())
                .password(messageModel.getPassword())
                .build();

        BillServiceModel billServiceModel = BillServiceManager.sendSummary(
                messageModel.getFileName(), file, config
        );

        informResult(billServiceModel);
    }

    private void informResult(BillServiceModel billServiceModel) {

    }
}

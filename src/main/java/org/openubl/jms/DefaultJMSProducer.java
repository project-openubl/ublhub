package org.openubl.jms;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openubl.factories.ModelFactory;
import org.openubl.models.SendFileModel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.inject.Inject;
import javax.jms.*;
import java.util.Map;

@ApplicationScoped
public class DefaultJMSProducer {

    @ConfigProperty(name = "openubl.sendFileQueue")
    String sendFileQueue;

    @ConfigProperty(name = "openubl.callbackQueue")
    String callbackQueue;

    @Inject
    ConnectionFactory connectionFactory;

    public void produceSendFileMessage(SendFileModel sendFileModel, byte[] file) {
        sendBytesMessage(sendFileQueue, ModelFactory.getAsMap(sendFileModel), file);
    }

    public void produceSendCallbackMessage(BillServiceModel billServiceModel) {
        sendBytesMessage(callbackQueue, ModelFactory.getAsMap(billServiceModel), billServiceModel.getCdr());
    }

    private void sendBytesMessage(String queueName, Map<String, String> properties, byte[] bytes) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer jmsProducer = context.createProducer();

            Queue queue = context.createQueue(queueName);

            Message message;
            if (bytes != null) {
                BytesMessage bytesMessage = context.createBytesMessage();
                bytesMessage.writeBytes(bytes);

                message = bytesMessage;
            } else {
                message = context.createTextMessage();
            }

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }

            jmsProducer.send(queue, message);
        } catch (Throwable ex) {
            // handle exception (details omitted)
            System.out.println(ex);
        }
    }

}

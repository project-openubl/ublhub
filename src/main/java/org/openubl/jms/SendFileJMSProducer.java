package org.openubl.jms;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openubl.factories.ModelFactory;
import org.openubl.models.SendFileModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import java.util.Map;

@ApplicationScoped
public class SendFileJMSProducer {

    @ConfigProperty(name = "openubl.sendFileQueue")
    String sendFileQueue;

    @Inject
    ConnectionFactory connectionFactory;

    public void produceSendFileMessage(SendFileModel sendFileModel, byte[] file) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer jmsProducer = context.createProducer();

            for (Map.Entry<String, String> entry : ModelFactory.getAsMap(sendFileModel).entrySet()) {
                jmsProducer.setProperty(entry.getKey(), entry.getValue());
            }

            Queue queue = context.createQueue(sendFileQueue);

            BytesMessage message = context.createBytesMessage();
            message.writeBytes(file);

            jmsProducer.send(queue, message);
        } catch (JMSRuntimeException | JMSException ex) {
            // handle exception (details omitted)
            System.out.println(ex);
        }
    }

}

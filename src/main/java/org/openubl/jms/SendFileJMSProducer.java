package org.openubl.jms;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.factories.ModelFactory;
import org.openubl.models.MessageModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.Map;

@ApplicationScoped
public class SendFileJMSProducer {

    private static final Logger LOG = Logger.getLogger(SendFileJMSProducer.class);

    @ConfigProperty(name = "openubl.sendFileQueue")
    String sendFileQueue;

    @ConfigProperty(name = "openubl.message.delay")
    Long messageDelay;

    @Inject
    ConnectionFactory connectionFactory;

    public void produceSendFileMessage(MessageModel messageModel, byte[] file) throws JMSException {
        if (file == null || file.length == 0) {
            throw new IllegalStateException("Invalid file");
        }

        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(messageDelay);

            Queue queue = context.createQueue(sendFileQueue);

            BytesMessage message = context.createBytesMessage();
            message.writeBytes(file);

            for (Map.Entry<String, String> entry : ModelFactory.getAsMap(messageModel).entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }

            producer.send(queue, message);
        } finally {
            LOG.debug(messageModel + " was sent to:" + sendFileQueue);
        }
    }

}

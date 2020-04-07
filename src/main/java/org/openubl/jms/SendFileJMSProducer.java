package org.openubl.jms;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.factories.ModelFactory;
import org.openubl.models.SendFileMessageModel;

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

    @Inject
    ConnectionFactory connectionFactory;

    public void produceSendFileMessage(SendFileMessageModel sendFileMessageModel, byte[] file) throws JMSException {
        if (file == null || file.length == 0) {
            throw new IllegalStateException("Invalid file");
        }

        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer jmsProducer = context.createProducer();

            Queue queue = context.createQueue(sendFileQueue);

            BytesMessage message = context.createBytesMessage();
            message.writeBytes(file);

            for (Map.Entry<String, String> entry : ModelFactory.getAsMap(sendFileMessageModel).entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }

            jmsProducer.send(queue, message);
        } finally {
            LOG.info("File has been sent to the Broker");
        }
    }


}

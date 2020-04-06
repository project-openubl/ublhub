package org.openubl.jms;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.factories.ModelFactory;
import org.openubl.models.SendFileModel;

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

    public void produceSendFileMessage(SendFileModel sendFileModel, byte[] file) {
        if (file == null || file.length == 0) {
            throw new IllegalStateException("Invalid file");
        }

        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer jmsProducer = context.createProducer();

            Queue queue = context.createQueue(sendFileQueue);

            BytesMessage message = context.createBytesMessage();
            message.writeBytes(file);

            for (Map.Entry<String, String> entry : ModelFactory.getAsMap(sendFileModel).entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }

            jmsProducer.send(queue, message);
        } catch (JMSException e) {
            LOG.error("Error trying to send bytes message", e);
        }
    }


}

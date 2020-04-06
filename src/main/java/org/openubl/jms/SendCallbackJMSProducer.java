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
public class SendCallbackJMSProducer {

    private static final Logger LOG = Logger.getLogger(SendCallbackJMSProducer.class);

    @ConfigProperty(name = "openubl.callbackQueue")
    String callbackQueue;

    @Inject
    ConnectionFactory connectionFactory;

    public void produceSendCallbackMessage(BillServiceModel billServiceModel) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer jmsProducer = context.createProducer();

            Queue queue = context.createQueue(callbackQueue);

            Message message;
            if (billServiceModel.getCdr() != null) {
                BytesMessage bytesMessage = context.createBytesMessage();
                bytesMessage.writeBytes(billServiceModel.getCdr());

                message = bytesMessage;
            } else {
                message = context.createTextMessage("No CDR available");
            }

            for (Map.Entry<String, String> entry : ModelFactory.getAsMap(billServiceModel).entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }

            jmsProducer.send(queue, message);
        } catch (JMSException e) {
            LOG.error("Error trying to send bytes message", e);
        }
    }

}

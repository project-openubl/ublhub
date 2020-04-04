package org.openubl.jms;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openubl.factories.ModelFactory;
import org.openubl.models.SendFileModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import java.util.Map;

@ApplicationScoped
public class SendCallbackJMSProducer {

    @ConfigProperty(name = "openubl.callbackQueue")
    String callbackQueue;

    @Inject
    ConnectionFactory connectionFactory;

    public void produceSendCallbackMessage(BillServiceModel billServiceModel) {
//        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
//            JMSProducer jmsProducer = context.createProducer();
//
//            for (Map.Entry<String, Object> entry : ModelFactory.getAsMap(billServiceModel).entrySet()) {
//                jmsProducer.setProperty(entry.getKey(), entry.getValue());
//            }
//
//            Queue queue = context.createQueue(callbackQueue);
//
//            BytesMessage message = context.createBytesMessage();
//            message.writeBytes(billServiceModel.getCdr());
//
//            jmsProducer.send(queue, message);
//        } catch (JMSRuntimeException | JMSException ex) {
//            // handle exception (details omitted)
//            System.out.println(ex);
//        }
    }
}

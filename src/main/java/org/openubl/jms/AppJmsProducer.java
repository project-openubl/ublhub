package org.openubl.jms;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;

@ApplicationScoped
public class AppJmsProducer {

    @Inject
    ConnectionFactory connectionFactory;

    @ConfigProperty(name = "openubl.queueName")
    String queueName;

    public void sendMessage(SunatJMSMessageModel messageModel, byte[] file) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer jmsProducer = context.createProducer();

            jmsProducer.setProperty("fileName", messageModel.getFileName());
            jmsProducer.setProperty("serverUrl", messageModel.getServerUrl());
            jmsProducer.setProperty("documentType", messageModel.getDocumentType());
            jmsProducer.setProperty("username", messageModel.getUsername());
            jmsProducer.setProperty("password", messageModel.getPassword());

            Queue queue = context.createQueue(queueName);

            BytesMessage message = context.createBytesMessage();
            message.writeBytes(file);

            jmsProducer.send(queue, message);
        } catch (JMSRuntimeException | JMSException ex) {
            // handle exception (details omitted)
            System.out.println(ex);
        }
    }
}

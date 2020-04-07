package org.openubl.jms;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.factories.ModelFactory;
import org.openubl.models.SendFileMessageModel;
import org.openubl.providers.SendFileWSProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SendFileJMSConsumer implements Runnable {

    private static final Logger LOG = Logger.getLogger(SendFileJMSConsumer.class);

    @Inject
    SendFileWSProvider sunatWSProvider;

    @Inject
    ConnectionFactory connectionFactory;

    @ConfigProperty(name = "openubl.sendFileQueue")
    String sendFileQueue;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    void onStart(@Observes StartupEvent ev) {
        scheduler.scheduleWithFixedDelay(this, 0L, 5L, TimeUnit.SECONDS);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.CLIENT_ACKNOWLEDGE)) {
            JMSConsumer jmsConsumer = context.createConsumer(context.createQueue(sendFileQueue));
            while (true) {
                Message message = jmsConsumer.receive();
                if (message == null) {
                    return;
                }

                if (!(message instanceof BytesMessage)) {
                    LOG.warn("Consumer can not consume messages other than Bytes Messages");
                }

                SendFileMessageModel model = ModelFactory.getSendFilePropertiesModel(message);
                boolean result = sunatWSProvider.sendFile(model, message.getBody(byte[].class));

                if (result) {
                    message.acknowledge();
                }
            }
        } catch (JMSException e) {
            LOG.error(e);
        } catch (Throwable e) {
            LOG.error("Unexpected exception", e);
        }
    }

}

package org.openubl.jms;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.providers.WSProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SendTicketQueueConsumer implements Runnable {

    private static final Logger LOG = Logger.getLogger(SendTicketQueueConsumer.class);

    @Inject
    WSProvider wsProvider;

    @Inject
    ConnectionFactory connectionFactory;

    @ConfigProperty(name = "openubl.jms.ticketQueue")
    String ticketQueue;

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
            JMSConsumer consumer = context.createConsumer(context.createQueue(ticketQueue));
            while (true) {
                Message message = consumer.receive();
                if (message == null) {
                    return;
                }

                TextMessage textMessage = null;
                if (message instanceof TextMessage) {
                    textMessage = (TextMessage) message;
                }

                if (textMessage == null) {
                    LOG.warn("ticketQueue can process only TextMessages");
                    return;
                }

                String entityId = textMessage.getText();
                boolean result = wsProvider.checkTicket(Long.parseLong(entityId));

                if (result) {
                    message.acknowledge();
                    LOG.debug("Message acknowledged");
                }
            }
        } catch (JMSException e) {
            LOG.error("JMSException", e);
        } catch (Throwable e) {
            LOG.error("Throwable exception", e);
        }
    }

}

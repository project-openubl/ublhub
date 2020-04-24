/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xmlsender.jms;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import io.github.project.openubl.xmlsender.ws.WSSunatClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SendFileQueueConsumer implements Runnable {

    private static final Logger LOG = Logger.getLogger(SendFileQueueConsumer.class);

    @Inject
    WSSunatClient wsSunatClient;

    @Inject
    ConnectionFactory connectionFactory;

    @ConfigProperty(name = "openubl.jms.sendFileQueue")
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
            JMSConsumer consumer = context.createConsumer(context.createQueue(sendFileQueue));
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
                    LOG.warn("sendFileQueue can process only TextMessages");
                    return;
                }

                String documentId = textMessage.getText();
                boolean result = wsSunatClient.sendDocument(Long.valueOf(documentId));

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

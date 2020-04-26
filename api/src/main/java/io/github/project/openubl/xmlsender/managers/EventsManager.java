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
package io.github.project.openubl.xmlsender.managers;

import io.github.project.openubl.xmlsender.models.DocumentEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.jms.*;

@ApplicationScoped
public class EventsManager {

    @ConfigProperty(name = "openubl.jms.delay")
    Long messageDelay;

    @ConfigProperty(name = "openubl.jms.sendFileQueue")
    String sendFileQueue;

    @ConfigProperty(name = "openubl.jms.callbackQueue")
    String callbackQueue;

    @ConfigProperty(name = "openubl.jms.ticketQueue")
    String ticketQueue;

    @Inject
    ConnectionFactory connectionFactory;

    public void onDocumentCreate(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEvent.Created event) {
        produceMessage(sendFileQueue, event.getId());
    }

    public void onDocumentRequireCheckTicket(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEvent.RequireCheckTicket event) {
        produceMessage(ticketQueue, event.getId());
    }

    public void onDocumentDelivered(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEvent.Delivered event) {
        produceMessage(callbackQueue, event.getId());
    }

    private void produceMessage(String queueName, Long documentId) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(messageDelay);
            Queue queue = context.createQueue(queueName);
            Message message = context.createTextMessage(documentId.toString());
            producer.send(queue, message);
        }
    }
}

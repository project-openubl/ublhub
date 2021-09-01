/*
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
package io.github.project.openubl.xsender.events.amqp;

import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class AMQPBroadcasterEventConsumer {

    @Inject
    Event<DocumentRepresentation> event;

    @Incoming("document-event-incoming")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> sendFile(Message<JsonObject> inMessage) {
        DocumentRepresentation rep = inMessage.getPayload().mapTo(DocumentRepresentation.class);
        event.fire(rep);

        return Uni.createFrom().completionStage(inMessage.ack());
    }

}

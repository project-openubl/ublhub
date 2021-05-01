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
package io.github.project.openubl.xsender.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xsender.kafka.idm.CompanyCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.websockets.idm.EventMessage;
import io.github.project.openubl.xsender.websockets.idm.EventSpec;
import io.github.project.openubl.xsender.websockets.idm.TypeMessage;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.impl.ConcurrentHashSet;
import org.apache.kafka.common.header.Header;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/companies")
@ApplicationScoped
public class CompaniesEndpoint {

    private static final Logger LOG = Logger.getLogger(CompaniesEndpoint.class);

    protected static final Map<Session, String> sessions = new ConcurrentHashMap<>();
    protected static final Map<String, Set<Session>> userSessions = new ConcurrentHashMap<>();

    @Inject
    KeycloakAuthenticator keycloakAuthenticator;

    @Inject
    ObjectMapper objectMapper;

    @OnMessage
    public void onMessage(String message, Session session) {
        Optional<String> usernameOptional = keycloakAuthenticator.authenticate(message, session);
        usernameOptional.ifPresent(username -> {
            sessions.put(session, username);

            if (!userSessions.containsKey(username)) {
                userSessions.put(username, new ConcurrentHashSet<>());
            }
            userSessions.get(username).add(session);
        });
    }

    @OnClose
    public void onClose(Session session) {
        String username = sessions.get(session);
        if (username != null) {
            sessions.remove(session);
            userSessions.getOrDefault(username, Collections.emptySet()).remove(session);
        }
    }

    @Incoming("event-company")
    public CompletionStage<Void> companyEvents(KafkaRecord<String, String> record) {
        return CompletableFuture.runAsync(() -> {
            String eventType = getHeaderAsString(record, "eventType");
            String payload = record.getPayload();

            try {
                String unescapedPayload = objectMapper.readValue(payload, String.class);
                CompanyCUDEventRepresentation eventRep = objectMapper.readValue(unescapedPayload, CompanyCUDEventRepresentation.class);

                EventMessage wsMessage = EventMessage.Builder.anEventMessage()
                        .withType(TypeMessage.EVENT)
                        .withSpec(EventSpec.Builder.anEventSpec()
                                .withId(eventRep.getId())
                                .withEntity(EntityType.company)
                                .withEvent(EventType.valueOf(eventType))
                                .build()
                        )
                        .build();

                String wsMessageString = objectMapper.writeValueAsString(wsMessage);
                userSessions.getOrDefault(eventRep.getOwner(), Collections.emptySet()).forEach(session -> session.getAsyncRemote().sendObject(wsMessageString, result -> {
                    if (result.getException() != null) {
                        LOG.error("Unable to send message ", result.getException());
                    }
                }));

                record.ack();
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private String getHeaderAsString(KafkaRecord<?, ?> record, String name) {
        Header header = record.getHeaders().lastHeader(name);
        if (header == null) {
            throw new IllegalArgumentException("Expected record header '" + name + "' not present");
        }

        return new String(header.value(), Charset.forName("UTF-8"));
    }

}

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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xsender.kafka.idm.CompanyCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.websockets.idm.EventMessage;
import io.github.project.openubl.xsender.websockets.idm.EventSpec;
import io.github.project.openubl.xsender.websockets.idm.TypeMessage;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.impl.ConcurrentHashSet;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/namespaces/{namespace}/documents")
@ApplicationScoped
public class DocumentsEndpoint {

    private static final Logger LOG = Logger.getLogger(DocumentsEndpoint.class);

    protected static final Map<Session, String> sessions = new ConcurrentHashMap<>();
    protected static final Map<String, Set<Session>> namespacesSessions = new ConcurrentHashMap<>();

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    KeycloakAuthenticator keycloakAuthenticator;

    @Inject
    ObjectMapper objectMapper;

    @Blocking
    @Transactional
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("namespace") String namespace) {
        Optional<String> usernameOptional = keycloakAuthenticator.authenticate(message, session);

        if (usernameOptional.isPresent()) {
            String username = usernameOptional.get();

            //TODO: move to CDI producer
            ManagedExecutor executor = ManagedExecutor.builder()
                    .maxAsync(5)
                    .propagated(ThreadContext.CDI, ThreadContext.TRANSACTION)
                    .build();

            //TODO: move to CDI producer
            ThreadContext threadContext = ThreadContext.builder()
                    .propagated(ThreadContext.CDI, ThreadContext.TRANSACTION)
                    .build();

            executor.runAsync(threadContext.contextualRunnable(() -> {

                NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespace, username).orElse(null);
                if (namespaceEntity != null) {
                    sessions.put(session, namespaceEntity.getId());

                    String key = namespaceEntity.getId();
                    Set<Session> newValue = namespacesSessions.getOrDefault(namespaceEntity.getId(), new ConcurrentHashSet<>());
                    newValue.add(session);

                    namespacesSessions.put(key, newValue);
                } else {
                    try {
                        String errorMessage = "Unauthorized websocket";
                        session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, errorMessage));
                    } catch (IOException e1) {
                        LOG.warn(e1.getMessage());
                    }
                }

            }));
        }

    }

    @OnClose
    public void onClose(Session session) {
        String companyId = sessions.get(session);
        if (companyId != null) {
            sessions.remove(session);
            namespacesSessions.getOrDefault(companyId, Collections.emptySet()).remove(session);
        }
    }

    @Incoming("event-company")
    public CompletionStage<Void> readEvent(KafkaRecord<String, String> record) {
        return CompletableFuture.runAsync(() -> {
            String eventType = WebsocketUtils.getHeaderAsString(record, "eventType");
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

                namespacesSessions.getOrDefault(eventRep.getNamespace(), Collections.emptySet()).forEach(session -> session.getAsyncRemote().sendObject(wsMessageString, result -> {
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

}

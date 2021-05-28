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

import io.github.project.openubl.xsender.events.DocumentEvent;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ServerEndpoint("/namespaces/{namespaceId}/documents")
@ApplicationScoped
public class DocumentsEndpoint {

    private static final Logger LOG = Logger.getLogger(DocumentsEndpoint.class);

    protected static final Map<Session, String> sessions = new ConcurrentHashMap<>();
    protected static final Map<String, Set<Session>> namespacesSessions = new ConcurrentHashMap<>();

    ExecutorService executor;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    KeycloakAuthenticator keycloakAuthenticator;

    @PostConstruct
    void postConstruct() {
        executor = Executors.newFixedThreadPool(10);
    }

    @Blocking
    @Transactional
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("namespaceId") String namespaceId) {
        Uni.createFrom()
                .completionStage(() -> CompletableFuture.supplyAsync(() -> {
                    String username = keycloakAuthenticator.authenticate(message, session).orElse(null);
                    NamespaceEntity namespaceEntity = null;
                    if (username != null) {
                        namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, username).orElse(null);
                    }
                    return namespaceEntity;
                }, executor))
                .subscribe()
                .with(namespaceEntity -> {
                    if (namespaceEntity != null) {
                        sessions.put(session, namespaceEntity.getId());

                        String key = namespaceEntity.getId();
                        Set<Session> newValue = namespacesSessions.getOrDefault(namespaceEntity.getId(), new ConcurrentHashSet<>());
                        newValue.add(session);

                        namespacesSessions.put(key, newValue);
                    } else {
                        try {
                            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Unauthorized websocket"));
                        } catch (IOException e) {
                            LOG.warn(e.getMessage());
                        }
                    }
                });
    }

    @OnClose
    public void onClose(Session session) {
        String companyId = sessions.get(session);
        if (companyId != null) {
            sessions.remove(session);
            namespacesSessions.getOrDefault(companyId, Collections.emptySet()).remove(session);
        }
    }

    @Incoming("document-event-incoming")
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public void documentEvent(JsonObject event) {
        DocumentEvent documentEvent = event.mapTo(DocumentEvent.class);
        namespacesSessions.getOrDefault(documentEvent.getNamespaceId(), Collections.emptySet())
                .forEach(session -> session.getAsyncRemote().sendObject(documentEvent.getId(), result -> {
                    if (result.getException() != null) {
                        LOG.error("Unable to send message ", result.getException());
                    }
                }));
    }
}

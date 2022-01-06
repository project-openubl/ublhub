/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.ublhub.idm.DocumentRepresentation;
import io.vertx.core.impl.ConcurrentHashSet;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint("/namespaces/{namespaceId}/documents")
public class DocumentsEndpoint {

    private static final Logger LOG = Logger.getLogger(DocumentsEndpoint.class);

    protected static final Map<Session, String> sessions = new ConcurrentHashMap<>();
    protected static final Map<String, Set<Session>> namespacesSessions = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("namespaceId") String namespaceId) {
        sessions.put(session, namespaceId);

        Set<Session> newValue = namespacesSessions.getOrDefault(namespaceId, new ConcurrentHashSet<>());
        newValue.add(session);
        namespacesSessions.put(namespaceId, newValue);
    }

    @OnClose
    public void onClose(Session session) {
        String companyId = sessions.get(session);
        if (companyId != null) {
            sessions.remove(session);
            namespacesSessions.getOrDefault(companyId, Collections.emptySet()).remove(session);
        }
    }

    public void documentEvent(@Observes DocumentRepresentation documentRepresentation) {
        namespacesSessions
                .getOrDefault(documentRepresentation.getNamespaceId(), Collections.emptySet())
                .forEach(session -> {
                    try {
                        session.getAsyncRemote().sendObject(objectMapper.writeValueAsString(documentRepresentation), result -> {
                            if (result.getException() != null) {
                                LOG.error("Unable to send message ", result.getException());
                            }
                        });
                    } catch (JsonProcessingException e) {
                        LOG.error("Unable encode JSON", e);
                    }
                });
    }

}

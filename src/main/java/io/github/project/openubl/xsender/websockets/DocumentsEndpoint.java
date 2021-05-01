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

import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.impl.ConcurrentHashSet;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
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
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/companies/{companyName}/documents")
@ApplicationScoped
public class DocumentsEndpoint {

    private static final Logger LOG = Logger.getLogger(DocumentsEndpoint.class);

    protected static final Map<Session, String> sessions = new ConcurrentHashMap<>();
    protected static final Map<String, Set<Session>> companySessions = new ConcurrentHashMap<>();

    @Inject
    CompanyRepository companyRepository;

    @Inject
    KeycloakAuthenticator keycloakAuthenticator;


    @Transactional
    @Blocking
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("companyName") String companyName) {
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
                Optional<CompanyEntity> companyOptional = companyRepository.findByNameAndOwner(companyName, username);
                if (companyOptional.isPresent()) {
                    CompanyEntity companyEntity = companyOptional.get();
                    sessions.put(session, companyEntity.getId());

                    if (!companySessions.containsKey(companyEntity.getId())) {
                        companySessions.put(companyEntity.getId(), new ConcurrentHashSet<>());
                    }
                    companySessions.get(companyEntity.getId()).add(session);
                } else {
                    try {
                        String errorMessage = "Unauthorized websocket";

                        // Shorten the message to meet the standards for "CloseReason" (only allows 122 chars)
                        if (StringUtils.isNotBlank(errorMessage) && errorMessage.length() >= 122) {
                            errorMessage = errorMessage.substring(0, 122);
                        }

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
            companySessions.getOrDefault(companyId, Collections.emptySet()).remove(session);
        }
    }

//    @Incoming("incoming-document-event")
//    public void companyEvents(DocumentEventKafka event) {
//        String companyId = event.getCompany();
//
//        EventMessage message = EventMessage.Builder.anEventMessage()
//                .withType(TypeMessage.EVENT)
//                .withSpec(EventSpec.Builder.anEventSpec()
//                        .withEntity(EntityType.DOCUMENT)
//                        .withId(event.getId())
//                        .withEvent(EventType.valueOf(event.getEvent()))
//                        .build()
//                )
//                .build();
//
//        String jsonMessage = JsonbBuilder.create().toJson(message);
//
//        companySessions.getOrDefault(companyId, Collections.emptySet()).forEach(session -> {
//            session.getAsyncRemote().sendObject(jsonMessage, result -> {
//                if (result.getException() != null) {
//                    LOG.error("Unable to send message ", result.getException());
//                }
//            });
//        });
//    }

}

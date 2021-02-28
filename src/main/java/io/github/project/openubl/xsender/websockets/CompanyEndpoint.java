package io.github.project.openubl.xsender.websockets;

import io.github.project.openubl.xsender.events.EntityEventProvider;
import io.github.project.openubl.xsender.models.EntityEvent;
import io.github.project.openubl.xsender.models.EntityType;
import io.github.project.openubl.xsender.models.EventType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.vertx.core.impl.ConcurrentHashSet;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
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

@ServerEndpoint("/company/{companyId}")
@ApplicationScoped
public class CompanyEndpoint {

    private static final Logger LOG = Logger.getLogger(CompanyEndpoint.class);

    protected static final Map<String, Set<Session>> companySessions = new ConcurrentHashMap<>();

    @Inject
    KeycloakAuthenticator keycloakAuthenticator;

    @Inject
    CompanyRepository companyRepository;

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("companyId") String companyId) {
        Optional<UserInfo> authenticatedUserInfo = keycloakAuthenticator.authenticate(message, session);
        authenticatedUserInfo.ifPresent(userInfo -> {
            CompanyEntity companyEntity = companyRepository.findById(companyId);
            if (companyEntity.getOwner().equals(userInfo.getPreferred_username())) {
                if (!companySessions.containsKey(companyId)) {
                    companySessions.put(companyId, new ConcurrentHashSet<>());
                }
                companySessions.get(companyId).add(session);
            } else {
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Unauthorized"));
                } catch (IOException e) {
                    LOG.warn(e.getMessage());
                }
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("companyId") String companyId) {
        companySessions.getOrDefault(companyId, Collections.emptySet()).remove(session);
    }

    public void onDocumentEvent(@Observes @EntityEventProvider(EntityType.COMPANY) EntityEvent event) {
        if (event.getType().equals(EventType.UPDATED)) {
            Jsonb jsonb = JsonbBuilder.create();
            String message = jsonb.toJson(event);

            companySessions.getOrDefault(event.getId(), Collections.emptySet()).forEach(session -> {
                session.getAsyncRemote().sendObject(message, result -> {
                    if (result.getException() != null) {
                        LOG.error("Unable to send message ", result.getException());
                    }
                });
            });
        }
    }

}

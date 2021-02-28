package io.github.project.openubl.xsender.websockets;

import io.github.project.openubl.xsender.events.EntityEventProvider;
import io.github.project.openubl.xsender.models.EntityEvent;
import io.github.project.openubl.xsender.models.EntityType;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;

@ServerEndpoint("/companies")
@ApplicationScoped
public class CompaniesEndpoint extends AbstractEndpoint {

    private static final Logger LOG = Logger.getLogger(CompaniesEndpoint.class);

    @OnMessage
    public void onMessage(String message, Session session) {
        handleOnMessage(message, session);
    }

    @OnClose
    public void onClose(Session session) {
        handleOnClose(session);
    }

    public void onDocumentEvent(@Observes @EntityEventProvider(EntityType.COMPANY) EntityEvent event) {
        String companyOwner = event.getOwner();

        Jsonb jsonb = JsonbBuilder.create();
        String message = jsonb.toJson(event);

        userSessions.getOrDefault(companyOwner, Collections.emptySet()).forEach(session -> {
            session.getAsyncRemote().sendObject(message, result -> {
                if (result.getException() != null) {
                    LOG.error("Unable to send message ", result.getException());
                }
            });
        });
    }

}

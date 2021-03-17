package io.github.project.openubl.xsender.websockets;

import io.github.project.openubl.xsender.avro.CompanyEventKafka;
import io.github.project.openubl.xsender.models.EntityType;
import io.github.project.openubl.xsender.models.EventType;
import io.github.project.openubl.xsender.websockets.idm.EventMessage;
import io.github.project.openubl.xsender.websockets.idm.EventSpec;
import io.github.project.openubl.xsender.websockets.idm.TypeMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
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

    @Incoming("incoming-company-event")
    public void companyEvents(CompanyEventKafka event) {
        String companyOwner = event.getOwner();

        EventMessage message = EventMessage.Builder.anEventMessage()
                .withType(TypeMessage.EVENT)
                .withSpec(EventSpec.Builder.anEventSpec()
                        .withEntity(EntityType.COMPANY)
                        .withId(event.getId())
                        .withEvent(EventType.valueOf(event.getEvent()))
                        .build()
                )
                .build();

        String jsonMessage = JsonbBuilder.create().toJson(message);

        userSessions.getOrDefault(companyOwner, Collections.emptySet()).forEach(session -> {
            session.getAsyncRemote().sendObject(jsonMessage, result -> {
                if (result.getException() != null) {
                    LOG.error("Unable to send message ", result.getException());
                }
            });
        });
    }

}

package io.github.project.openubl.xsender.events.vertx;

import io.github.project.openubl.xsender.events.BroadcasterEvent;
import io.github.project.openubl.xsender.events.BroadcasterEventProvider;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.vertx.core.eventbus.EventBus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@BroadcasterEventProvider(BroadcasterEventProvider.Type.VERTX)
public class VertxBroadcasterEvent implements BroadcasterEvent {

    public static final String VERTX_SEND_DOCUMENT_EVENT = "vertx-document-event";

    @Inject
    EventBus eventBus;

    @Override
    public void broadcast(DocumentRepresentation rep) {
        eventBus.send(VERTX_SEND_DOCUMENT_EVENT, rep);
    }

}

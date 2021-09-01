package io.github.project.openubl.xsender.events.amqp;

import io.github.project.openubl.xsender.events.BroadcasterEvent;
import io.github.project.openubl.xsender.events.BroadcasterEventProvider;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@BroadcasterEventProvider(BroadcasterEventProvider.Type.AMQP)
public class AMQPBroadcasterEvent implements BroadcasterEvent {

    @Inject
    @Channel("document-event-emitter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<DocumentRepresentation> emitter;

    @Override
    public void broadcast(DocumentRepresentation rep) {
        emitter.send(rep);
    }

}

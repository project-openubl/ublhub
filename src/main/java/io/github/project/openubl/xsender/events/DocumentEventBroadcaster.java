package io.github.project.openubl.xsender.events;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Transactional
@ApplicationScoped
public class DocumentEventBroadcaster {

    @Inject
    Event<DocumentEvent> documentEvent;

    @Inject
    @Channel("document-event-emiter")
    @OnOverflow(value = OnOverflow.Strategy.LATEST)
    Emitter<DocumentEvent> documentEventBroadcaster;

    public void fire(DocumentEvent event) {
        documentEvent.fire(event);
    }

    protected void onDocumentEventFired(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEvent event) {
        documentEventBroadcaster.send(event);
    }

}

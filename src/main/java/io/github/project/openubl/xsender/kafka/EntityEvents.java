package io.github.project.openubl.xsender.kafka;

import io.github.project.openubl.xsender.avro.CompanyEventKafka;
import io.github.project.openubl.xsender.avro.DocumentEventKafka;
import io.github.project.openubl.xsender.models.CompanyEvent;
import io.github.project.openubl.xsender.models.DocumentEvent;
import io.github.project.openubl.xsender.models.EventType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

@ApplicationScoped
public class EntityEvents {

    @Inject
    @Channel("company-event")
    Emitter<CompanyEventKafka> companyEventEmitter;

    @Inject
    @Channel("document-event")
    Emitter<DocumentEventKafka> documentEventEmitter;

    // Company

    public void onCompanyCreate(@Observes(during = TransactionPhase.AFTER_SUCCESS) CompanyEvent.Created event) {
        broadcastCompanyEvent(event.getId(), EventType.CREATED, event.getOwner());
    }

    public void onCompanyUpdate(@Observes(during = TransactionPhase.AFTER_SUCCESS) CompanyEvent.Updated event) {
        broadcastCompanyEvent(event.getId(), EventType.UPDATED, event.getOwner());
    }

    public void onCompanyDelete(@Observes(during = TransactionPhase.AFTER_SUCCESS) CompanyEvent.Deleted event) {
        broadcastCompanyEvent(event.getId(), EventType.DELETED, event.getOwner());
    }

    public void broadcastCompanyEvent(String entityId, EventType eventType, String owner) {
        CompanyEventKafka message = CompanyEventKafka.newBuilder()
                .setId(entityId)
                .setEvent(eventType.toString())
                .setOwner(owner)
                .build();

        companyEventEmitter.send(message);
    }

    // Document

    public void onDocumentCreate(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEvent.Created event) {
        broadcastDocumentEvent(event.getId(), EventType.CREATED, event.getCompanyId());
    }

    public void onDocumentUpdate(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEvent.Updated event) {
        broadcastDocumentEvent(event.getId(), EventType.UPDATED, event.getCompanyId());
    }

    public void onDocumentDelete(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEvent.Deleted event) {
        broadcastDocumentEvent(event.getId(), EventType.DELETED, event.getCompanyId());
    }

    public void broadcastDocumentEvent(String entityId, EventType eventType, String companyId) {
        DocumentEventKafka message = DocumentEventKafka.newBuilder()
                .setId(entityId)
                .setEvent(eventType.toString())
                .setCompany(companyId)
                .build();

        documentEventEmitter.send(message);
    }
}

package io.github.project.openubl.xsender.events;

import io.github.project.openubl.xsender.kafka.EntityEventKafkaProducer;
import io.github.project.openubl.xsender.models.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

@ApplicationScoped
public class EntityEventManager {

    @Inject
    EntityEventKafkaProducer kafkaProducer;

    public void onCompanyCreate(@Observes(during = TransactionPhase.AFTER_SUCCESS) CompanyEvent.Created event) {
        kafkaProducer.broadcast(EntityType.COMPANY, event.getId(), EventType.CREATED, event.getOwner());
    }

    public void onCompanyUpdate(@Observes(during = TransactionPhase.AFTER_SUCCESS) CompanyEvent.Updated event) {
        kafkaProducer.broadcast(EntityType.COMPANY, event.getId(), EventType.UPDATED, event.getOwner());
    }

    public void onCompanyDelete(@Observes(during = TransactionPhase.AFTER_SUCCESS) CompanyEvent.Deleted event) {
        kafkaProducer.broadcast(EntityType.COMPANY, event.getId(), EventType.DELETED, event.getOwner());
    }


    public void onDocumentCreate(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEntityEvent.Created event) {
        kafkaProducer.broadcast(EntityType.DOCUMENT, event.getId(), EventType.CREATED, event.getOwner());
    }

    public void onDocumentUpdate(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEntityEvent.Updated event) {
        kafkaProducer.broadcast(EntityType.DOCUMENT, event.getId(), EventType.UPDATED, event.getOwner());
    }

    public void onDocumentDelete(@Observes(during = TransactionPhase.AFTER_SUCCESS) DocumentEntityEvent.Deleted event) {
        kafkaProducer.broadcast(EntityType.DOCUMENT, event.getId(), EventType.DELETED, event.getOwner());
    }

}

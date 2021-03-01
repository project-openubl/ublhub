package io.github.project.openubl.xsender.kafka;

import io.github.project.openubl.xsender.avro.EntityEventKafka;
import io.github.project.openubl.xsender.events.EntityEventProvider;
import io.github.project.openubl.xsender.models.EntityEvent;
import io.github.project.openubl.xsender.models.EntityType;
import io.github.project.openubl.xsender.models.EventType;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class EntityEventKafkaConsumer {

    @Inject
    @EntityEventProvider(EntityType.COMPANY)
    Event<EntityEvent> companyEvent;

    @Inject
    @EntityEventProvider(EntityType.DOCUMENT)
    Event<EntityEvent> documentEvent;

    @Incoming("entity-event-from-kafka")
    public void receive(EntityEventKafka eventKafka) {
        EntityType entityType = EntityType.valueOf(eventKafka.getType());
        String id = eventKafka.getId();
        EventType eventType = EventType.valueOf(eventKafka.getEvent());
        String owner = eventKafka.getOwner();

        EntityEvent event = EntityEvent.Builder.anEntityEvent()
                .withId(id)
                .withType(eventType)
                .withOwner(owner)
                .build();

        switch (entityType) {
            case COMPANY:
                companyEvent.fire(event);
                break;
            case DOCUMENT:
                documentEvent.fire(event);
                break;
            default:
                System.out.println(eventKafka);
        }
    }

}

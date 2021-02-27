package io.github.project.openubl.xsender.kafka;

import io.github.project.openubl.xsender.avro.EntityEventKafka;
import io.github.project.openubl.xsender.models.EntityType;
import io.github.project.openubl.xsender.models.EventType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EntityEventKafkaProducer {

    @Inject
    @Channel("entity-event")
    Emitter<EntityEventKafka> emitter;

    public void broadcast(EntityType entityType, String entityId, EventType eventType, String owner) {
        EntityEventKafka eventKafka = EntityEventKafka.newBuilder()
                .setType(entityType.toString())
                .setId(entityId)
                .setEvent(eventType.toString())
                .setOwner(owner)
                .build();

        emitter.send(eventKafka);
    }

}

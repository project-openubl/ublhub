package io.github.project.openubl.xsender.kafka.producers;

import io.debezium.outbox.quarkus.ExportedEvent;

import java.time.Instant;

public class EntityEventProducer implements ExportedEvent<String, String> {

    private final Instant timestamp;

    private final String entityId;
    private final EntityType entityType;
    private final EventType eventType;
    private final String payload;

    public EntityEventProducer(String entityId, EntityType entityType, EventType eventType, String payload) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.eventType = eventType;
        this.payload = payload;

        this.timestamp = Instant.now();
    }

    @Override
    public String getAggregateId() {
        return entityId;
    }

    @Override
    public String getAggregateType() {
        return entityType.toString();
    }

    @Override
    public String getType() {
        return eventType.toString();
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
}

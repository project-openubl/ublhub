package io.github.project.openubl.xsender.kafka.producers;

import io.debezium.outbox.quarkus.ExportedEvent;

import java.time.Instant;

public class UBLDocumentCreatedEventProducer implements ExportedEvent<String, String> {

    private final static String AGGREGATE = "ubldocument";
    private final static String TYPE = "sunat";

    private final Instant timestamp;

    private final String ublDocumentId;
    private final String payload;

    public UBLDocumentCreatedEventProducer(String ublDocumentId, String payload) {
        this.ublDocumentId = ublDocumentId;
        this.payload = payload;

        this.timestamp = Instant.now();
    }

    @Override
    public String getAggregateId() {
        return ublDocumentId;
    }

    @Override
    public String getAggregateType() {
        return AGGREGATE;
    }

    @Override
    public String getType() {
        return TYPE;
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

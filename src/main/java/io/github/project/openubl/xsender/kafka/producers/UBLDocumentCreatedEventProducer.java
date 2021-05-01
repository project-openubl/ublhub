/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

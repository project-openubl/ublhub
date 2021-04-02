package io.github.project.openubl.xsender.websockets.idm;

import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;

public class EventSpec {

    public String id;
    public EventType event;
    public EntityType entity;

    public static final class Builder {
        public String id;
        public EventType event;
        public EntityType entity;

        private Builder() {
        }

        public static Builder anEventSpec() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withEvent(EventType event) {
            this.event = event;
            return this;
        }

        public Builder withEntity(EntityType entity) {
            this.entity = entity;
            return this;
        }

        public EventSpec build() {
            EventSpec eventSpec = new EventSpec();
            eventSpec.event = this.event;
            eventSpec.entity = this.entity;
            eventSpec.id = this.id;
            return eventSpec;
        }
    }
}

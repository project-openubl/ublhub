package io.github.project.openubl.xsender.websockets.idm;

public class EventMessage {

    public TypeMessage type;
    public EventSpec spec;

    public static final class Builder {
        public TypeMessage type;
        public EventSpec spec;

        private Builder() {
        }

        public static Builder anEventMessage() {
            return new Builder();
        }

        public Builder withType(TypeMessage type) {
            this.type = type;
            return this;
        }

        public Builder withSpec(EventSpec spec) {
            this.spec = spec;
            return this;
        }

        public EventMessage build() {
            EventMessage eventMessage = new EventMessage();
            eventMessage.type = this.type;
            eventMessage.spec = this.spec;
            return eventMessage;
        }
    }
}

package io.github.project.openubl.xsender.models;

public class EntityEvent {
    private String id;
    private EventType type;
    private String owner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public static final class Builder {
        private String id;
        private EventType type;
        private String owner;

        private Builder() {
        }

        public static Builder anEntityEvent() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withType(EventType type) {
            this.type = type;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public EntityEvent build() {
            EntityEvent entityEvent = new EntityEvent();
            entityEvent.setId(id);
            entityEvent.setType(type);
            entityEvent.setOwner(owner);
            return entityEvent;
        }
    }
}

package org.openubl.models;

public class MessageModel {

    private Long entityId;
    private String username;
    private String password;

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "SendFileMessageModel{" +
                "entityId=" + entityId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static final class Builder {
        private Long entityId;
        private String username;
        private String password;

        private Builder() {
        }

        public static Builder aSendFileMessageModel() {
            return new Builder();
        }

        public Builder withEntityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public MessageModel build() {
            MessageModel messageModel = new MessageModel();
            messageModel.setEntityId(entityId);
            messageModel.setUsername(username);
            messageModel.setPassword(password);
            return messageModel;
        }
    }
}

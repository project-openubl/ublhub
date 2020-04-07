package org.openubl.models;

public class SendFileMessageModel {
    private String serverUrl;
    private String fileName;
    private String documentType;
    private String username;
    private String password;
    private String customId;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
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

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public static final class Builder {
        private String serverUrl;
        private String fileName;
        private String documentType;
        private String username;
        private String password;
        private String customId;

        private Builder() {
        }

        public static Builder aSendFileMessageModel() {
            return new Builder();
        }

        public Builder withServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withDocumentType(String documentType) {
            this.documentType = documentType;
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

        public Builder withCustomId(String customId) {
            this.customId = customId;
            return this;
        }

        public SendFileMessageModel build() {
            SendFileMessageModel sendFileMessageModel = new SendFileMessageModel();
            sendFileMessageModel.setServerUrl(serverUrl);
            sendFileMessageModel.setFileName(fileName);
            sendFileMessageModel.setDocumentType(documentType);
            sendFileMessageModel.setUsername(username);
            sendFileMessageModel.setPassword(password);
            sendFileMessageModel.setCustomId(customId);
            return sendFileMessageModel;
        }
    }
}

package org.openubl.jms;

import java.util.Map;

public class SunatJMSMessageModel {
    private String serverUrl;
    private String fileName;
    private String documentType;
    private String username;
    private String password;

    public SunatJMSMessageModel() {
    }

    public SunatJMSMessageModel(Map<String, String> properties) {
        this.serverUrl = properties.get("serverUrl");
        this.fileName = properties.get("fileName");
        this.documentType = properties.get("documentType");
        this.username = properties.get("username");
        this.password = properties.get("password");
    }

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

    public static final class Builder {
        private String serverUrl;
        private String fileName;
        private String documentType;
        private String username;
        private String password;

        private Builder() {
        }

        public static Builder aSunatJMSMessageModel() {
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

        public SunatJMSMessageModel build() {
            SunatJMSMessageModel sunatJMSMessageModel = new SunatJMSMessageModel();
            sunatJMSMessageModel.setServerUrl(serverUrl);
            sunatJMSMessageModel.setFileName(fileName);
            sunatJMSMessageModel.setDocumentType(documentType);
            sunatJMSMessageModel.setUsername(username);
            sunatJMSMessageModel.setPassword(password);
            return sunatJMSMessageModel;
        }
    }
}

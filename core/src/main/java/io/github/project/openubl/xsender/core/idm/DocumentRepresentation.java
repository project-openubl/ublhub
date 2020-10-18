/**
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
package io.github.project.openubl.xsender.core.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DocumentRepresentation {

    private String id;

    private String cdrID;
    private String fileID;
    private String deliveryStatus;

    private String customId;

    private FileInfoRepresentation fileInfo;
    private SunatSecurityCredentialsRepresentation sunatCredentials;
    private SunatStatusRepresentation sunatStatus;

    public String getCdrID() {
        return cdrID;
    }

    public void setCdrID(String cdrID) {
        this.cdrID = cdrID;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public FileInfoRepresentation getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfoRepresentation fileInfo) {
        this.fileInfo = fileInfo;
    }

    public SunatSecurityCredentialsRepresentation getSunatCredentials() {
        return sunatCredentials;
    }

    public void setSunatCredentials(SunatSecurityCredentialsRepresentation sunatCredentials) {
        this.sunatCredentials = sunatCredentials;
    }

    public SunatStatusRepresentation getSunatStatus() {
        return sunatStatus;
    }

    public void setSunatStatus(SunatStatusRepresentation sunatStatus) {
        this.sunatStatus = sunatStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class FileInfoRepresentation {
        public String ruc;
        public String filename;
        public String documentID;
        public String documentType;
        public String deliveryURL;

        public String getRuc() {
            return ruc;
        }

        public void setRuc(String ruc) {
            this.ruc = ruc;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getDocumentID() {
            return documentID;
        }

        public void setDocumentID(String documentID) {
            this.documentID = documentID;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getDeliveryURL() {
            return deliveryURL;
        }

        public void setDeliveryURL(String deliveryURL) {
            this.deliveryURL = deliveryURL;
        }

    }

    public static class SunatSecurityCredentialsRepresentation {
        private String username;
        private String password;

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
    }

    public static class SunatStatusRepresentation {
        private Integer code;
        private String ticket;
        private String status;
        private String description;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getTicket() {
            return ticket;
        }

        public void setTicket(String ticket) {
            this.ticket = ticket;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

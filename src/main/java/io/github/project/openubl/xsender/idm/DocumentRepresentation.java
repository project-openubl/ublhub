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
package io.github.project.openubl.xsender.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;
import java.util.List;

@RegisterForReflection
public class DocumentRepresentation {

    private String id;
    private Long createdOn;

    private int retries;
    private Date willRetryOn;

    private Boolean fileContentValid;
    private String fileContentValidationError;
    private DocumentContentRepresentation fileContent;

    private String sunatDeliveryStatus;
    private DocumentSunatStatusRepresentation sunat;
    private List<DocumentSunatEventRepresentation> sunatEvents;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public Date getWillRetryOn() {
        return willRetryOn;
    }

    public void setWillRetryOn(Date willRetryOn) {
        this.willRetryOn = willRetryOn;
    }

    public Boolean getFileContentValid() {
        return fileContentValid;
    }

    public void setFileContentValid(Boolean fileContentValid) {
        this.fileContentValid = fileContentValid;
    }

    public String getFileContentValidationError() {
        return fileContentValidationError;
    }

    public void setFileContentValidationError(String fileContentValidationError) {
        this.fileContentValidationError = fileContentValidationError;
    }

    public DocumentContentRepresentation getFileContent() {
        return fileContent;
    }

    public void setFileContent(DocumentContentRepresentation fileContent) {
        this.fileContent = fileContent;
    }

    public String getSunatDeliveryStatus() {
        return sunatDeliveryStatus;
    }

    public void setSunatDeliveryStatus(String sunatDeliveryStatus) {
        this.sunatDeliveryStatus = sunatDeliveryStatus;
    }

    public DocumentSunatStatusRepresentation getSunat() {
        return sunat;
    }

    public void setSunat(DocumentSunatStatusRepresentation sunat) {
        this.sunat = sunat;
    }

    public List<DocumentSunatEventRepresentation> getSunatEvents() {
        return sunatEvents;
    }

    public void setSunatEvents(List<DocumentSunatEventRepresentation> sunatEvents) {
        this.sunatEvents = sunatEvents;
    }
}

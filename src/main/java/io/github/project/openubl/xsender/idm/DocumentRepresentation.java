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

@RegisterForReflection
public class DocumentRepresentation {

    private String id;
    private Long createdOn;
    private boolean inProgress;
    private String error;
    private Long scheduledDelivery;
    private int retryCount;

    private Boolean fileContentValid;
    private FileContentRepresentation fileContent;

    private SunatStatusRepresentation sunat;

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

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Boolean getFileContentValid() {
        return fileContentValid;
    }

    public void setFileContentValid(Boolean fileContentValid) {
        this.fileContentValid = fileContentValid;
    }

    public FileContentRepresentation getFileContent() {
        return fileContent;
    }

    public void setFileContent(FileContentRepresentation fileContent) {
        this.fileContent = fileContent;
    }

    public SunatStatusRepresentation getSunat() {
        return sunat;
    }

    public void setSunat(SunatStatusRepresentation sunat) {
        this.sunat = sunat;
    }

    public Long getScheduledDelivery() {
        return scheduledDelivery;
    }

    public void setScheduledDelivery(Long scheduledDelivery) {
        this.scheduledDelivery = scheduledDelivery;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}

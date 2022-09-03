/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DocumentDto {

    private String id;
    private boolean jobInProgress;

    private XMLFileContentRepresentation xmlFileContent;
    private SunatStatusRepresentation sunatResponse;
    private JobErrorRepresentation jobError;

    private Long created;
    private Long updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isJobInProgress() {
        return jobInProgress;
    }

    public void setJobInProgress(boolean jobInProgress) {
        this.jobInProgress = jobInProgress;
    }

    public XMLFileContentRepresentation getXmlFileContent() {
        return xmlFileContent;
    }

    public void setXmlFileContent(XMLFileContentRepresentation xmlFileContent) {
        this.xmlFileContent = xmlFileContent;
    }

    public SunatStatusRepresentation getSunatResponse() {
        return sunatResponse;
    }

    public void setSunatResponse(SunatStatusRepresentation sunatResponse) {
        this.sunatResponse = sunatResponse;
    }

    public JobErrorRepresentation getJobError() {
        return jobError;
    }

    public void setJobError(JobErrorRepresentation jobError) {
        this.jobError = jobError;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }
}

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
package io.github.project.openubl.xsender.scheduler.commons;

import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.sender.XSenderConfig;

import java.util.Date;

public final class DocumentUniSendBuilder {
    protected String namespaceId;
    protected String id;
    protected Integer retries;
    protected ErrorType error;
    protected boolean inProgress;
    protected Date scheduledDelivery;
    protected XmlContentModel xmlContent;
    protected XSenderConfig wsConfig;
    protected BillServiceModel billServiceModel;
    protected String cdrFileId;
    protected String xmlFileId;
    protected byte[] file;
    protected Boolean fileValid;

    private DocumentUniSendBuilder() {
    }

    public static DocumentUniSendBuilder aDocumentUniSend() {
        return new DocumentUniSendBuilder();
    }

    public DocumentUniSendBuilder withNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
        return this;
    }

    public DocumentUniSendBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public DocumentUniSendBuilder withRetries(Integer retries) {
        this.retries = retries;
        return this;
    }

    public DocumentUniSendBuilder withError(ErrorType error) {
        this.error = error;
        return this;
    }

    public DocumentUniSendBuilder withInProgress(boolean inProgress) {
        this.inProgress = inProgress;
        return this;
    }

    public DocumentUniSendBuilder withScheduledDelivery(Date scheduledDelivery) {
        this.scheduledDelivery = scheduledDelivery;
        return this;
    }

    public DocumentUniSendBuilder withXmlContent(XmlContentModel xmlContent) {
        this.xmlContent = xmlContent;
        return this;
    }

    public DocumentUniSendBuilder withWsConfig(XSenderConfig wsConfig) {
        this.wsConfig = wsConfig;
        return this;
    }

    public DocumentUniSendBuilder withBillServiceModel(BillServiceModel billServiceModel) {
        this.billServiceModel = billServiceModel;
        return this;
    }

    public DocumentUniSendBuilder withCdrFileId(String cdrFileId) {
        this.cdrFileId = cdrFileId;
        return this;
    }

    public DocumentUniSendBuilder withXmlFileId(String xmlFileId) {
        this.xmlFileId = xmlFileId;
        return this;
    }

    public DocumentUniSendBuilder withFile(byte[] file) {
        this.file = file;
        return this;
    }

    public DocumentUniSendBuilder withFileValid(Boolean fileValid) {
        this.fileValid = fileValid;
        return this;
    }

    public DocumentUniSend build() {
        DocumentUniSend documentUniSend = new DocumentUniSend();
        documentUniSend.setNamespaceId(namespaceId);
        documentUniSend.setId(id);
        documentUniSend.setRetries(retries);
        documentUniSend.setError(error);
        documentUniSend.setInProgress(inProgress);
        documentUniSend.setScheduledDelivery(scheduledDelivery);
        documentUniSend.setXmlContent(xmlContent);
        documentUniSend.setWsConfig(wsConfig);
        documentUniSend.setBillServiceModel(billServiceModel);
        documentUniSend.setCdrFileId(cdrFileId);
        documentUniSend.setXmlFileId(xmlFileId);
        documentUniSend.setFile(file);
        documentUniSend.setFileValid(fileValid);
        return documentUniSend;
    }
}

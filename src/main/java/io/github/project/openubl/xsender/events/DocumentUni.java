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
package io.github.project.openubl.xsender.events;

import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.sender.XSenderConfig;

import java.util.Date;

public class DocumentUni {
    protected String namespaceId;
    protected String id;

    protected Integer retries;

    protected ErrorType error;
    protected boolean inProgress;
    protected Date scheduledDelivery;

    //

    protected XmlContentModel xmlContent;
    protected XSenderConfig wsConfig;
    protected BillServiceModel billServiceModel;

    protected String cdrFileId;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public ErrorType getError() {
        return error;
    }

    public void setError(ErrorType error) {
        this.error = error;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public Date getScheduledDelivery() {
        return scheduledDelivery;
    }

    public void setScheduledDelivery(Date scheduledDelivery) {
        this.scheduledDelivery = scheduledDelivery;
    }

    public XmlContentModel getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(XmlContentModel xmlContent) {
        this.xmlContent = xmlContent;
    }

    public XSenderConfig getWsConfig() {
        return wsConfig;
    }

    public void setWsConfig(XSenderConfig wsConfig) {
        this.wsConfig = wsConfig;
    }

    public BillServiceModel getBillServiceModel() {
        return billServiceModel;
    }

    public void setBillServiceModel(BillServiceModel billServiceModel) {
        this.billServiceModel = billServiceModel;
    }

    public String getCdrFileId() {
        return cdrFileId;
    }

    public void setCdrFileId(String cdrFileId) {
        this.cdrFileId = cdrFileId;
    }
}

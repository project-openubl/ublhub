package io.github.project.openubl.xsender.events.amqp;

import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xsender.models.ErrorType;

import java.util.Date;

public final class DocumentUniBuilder {
    protected String namespaceId;
    protected String id;
    protected Integer retries;
    protected ErrorType error;
    protected boolean inProgress;
    protected Date scheduledDelivery;
    private XmlContentModel xmlContent;

    private DocumentUniBuilder() {
    }

    public static DocumentUniBuilder aDocumentUni() {
        return new DocumentUniBuilder();
    }

    public DocumentUniBuilder withNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
        return this;
    }

    public DocumentUniBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public DocumentUniBuilder withRetries(Integer retries) {
        this.retries = retries;
        return this;
    }

    public DocumentUniBuilder withError(ErrorType error) {
        this.error = error;
        return this;
    }

    public DocumentUniBuilder withInProgress(boolean inProgress) {
        this.inProgress = inProgress;
        return this;
    }

    public DocumentUniBuilder withScheduledDelivery(Date scheduledDelivery) {
        this.scheduledDelivery = scheduledDelivery;
        return this;
    }

    public DocumentUniBuilder withXmlContent(XmlContentModel xmlContent) {
        this.xmlContent = xmlContent;
        return this;
    }

    public DocumentUni build() {
        DocumentUni documentUni = new DocumentUni();
        documentUni.setNamespaceId(namespaceId);
        documentUni.setId(id);
        documentUni.setRetries(retries);
        documentUni.setError(error);
        documentUni.setInProgress(inProgress);
        documentUni.setScheduledDelivery(scheduledDelivery);
        documentUni.setXmlContent(xmlContent);
        return documentUni;
    }
}

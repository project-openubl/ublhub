package io.github.project.openubl.xsender.events;

public class DocumentEvent {
    private String id;
    private String namespaceId;

    public DocumentEvent() {
    }

    public DocumentEvent(String id, String namespaceId) {
        this.id = id;
        this.namespaceId = namespaceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}

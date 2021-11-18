package io.github.project.openubl.xsender.idm;

public final class NamespaceRepresentationBuilder {
    private String id;
    private String name;
    private String description;
    private SunatUrlsRepresentation webServices;
    private SunatCredentialsRepresentation credentials;

    private NamespaceRepresentationBuilder() {
    }

    public static NamespaceRepresentationBuilder aNamespaceRepresentation() {
        return new NamespaceRepresentationBuilder();
    }

    public NamespaceRepresentationBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public NamespaceRepresentationBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public NamespaceRepresentationBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public NamespaceRepresentationBuilder withWebServices(SunatUrlsRepresentation webServices) {
        this.webServices = webServices;
        return this;
    }

    public NamespaceRepresentationBuilder withCredentials(SunatCredentialsRepresentation credentials) {
        this.credentials = credentials;
        return this;
    }

    public NamespaceRepresentation build() {
        NamespaceRepresentation namespaceRepresentation = new NamespaceRepresentation();
        namespaceRepresentation.setId(id);
        namespaceRepresentation.setName(name);
        namespaceRepresentation.setDescription(description);
        namespaceRepresentation.setWebServices(webServices);
        namespaceRepresentation.setCredentials(credentials);
        return namespaceRepresentation;
    }
}

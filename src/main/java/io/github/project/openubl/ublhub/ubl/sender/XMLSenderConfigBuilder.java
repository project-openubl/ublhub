package io.github.project.openubl.ublhub.ubl.sender;

public final class XMLSenderConfigBuilder {

    private String facturaUrl;
    private String guiaRemisionUrl;
    private String percepcionRetencionUrl;
    private String username;
    private String password;

    private XMLSenderConfigBuilder() {
    }

    public static XMLSenderConfigBuilder aXMLSenderConfig() {
        return new XMLSenderConfigBuilder();
    }

    public XMLSenderConfigBuilder withFacturaUrl(String facturaUrl) {
        this.facturaUrl = facturaUrl;
        return this;
    }

    public XMLSenderConfigBuilder withGuiaRemisionUrl(String guiaRemisionUrl) {
        this.guiaRemisionUrl = guiaRemisionUrl;
        return this;
    }

    public XMLSenderConfigBuilder withPercepcionRetencionUrl(String percepcionRetencionUrl) {
        this.percepcionRetencionUrl = percepcionRetencionUrl;
        return this;
    }

    public XMLSenderConfigBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public XMLSenderConfigBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public XMLSenderConfig build() {
        XMLSenderConfig xMLSenderConfig = new XMLSenderConfig();
        xMLSenderConfig.setFacturaUrl(facturaUrl);
        xMLSenderConfig.setGuiaRemisionUrl(guiaRemisionUrl);
        xMLSenderConfig.setPercepcionRetencionUrl(percepcionRetencionUrl);
        xMLSenderConfig.setUsername(username);
        xMLSenderConfig.setPassword(password);
        return xMLSenderConfig;
    }
}

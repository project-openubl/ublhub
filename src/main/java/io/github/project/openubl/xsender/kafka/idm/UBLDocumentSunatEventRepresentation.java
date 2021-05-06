package io.github.project.openubl.xsender.kafka.idm;

public class UBLDocumentSunatEventRepresentation {

    private String namespace;

    private String id;
    private String storageFile;
    private String ticket;

    private String sunatUsername;
    private String sunatPassword;
    private String sunatUrlFactura;
    private String sunatUrlGuiaRemision;
    private String sunatUrlPercepcionRetencion;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(String storageFile) {
        this.storageFile = storageFile;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getSunatUsername() {
        return sunatUsername;
    }

    public void setSunatUsername(String sunatUsername) {
        this.sunatUsername = sunatUsername;
    }

    public String getSunatPassword() {
        return sunatPassword;
    }

    public void setSunatPassword(String sunatPassword) {
        this.sunatPassword = sunatPassword;
    }

    public String getSunatUrlFactura() {
        return sunatUrlFactura;
    }

    public void setSunatUrlFactura(String sunatUrlFactura) {
        this.sunatUrlFactura = sunatUrlFactura;
    }

    public String getSunatUrlGuiaRemision() {
        return sunatUrlGuiaRemision;
    }

    public void setSunatUrlGuiaRemision(String sunatUrlGuiaRemision) {
        this.sunatUrlGuiaRemision = sunatUrlGuiaRemision;
    }

    public String getSunatUrlPercepcionRetencion() {
        return sunatUrlPercepcionRetencion;
    }

    public void setSunatUrlPercepcionRetencion(String sunatUrlPercepcionRetencion) {
        this.sunatUrlPercepcionRetencion = sunatUrlPercepcionRetencion;
    }
}

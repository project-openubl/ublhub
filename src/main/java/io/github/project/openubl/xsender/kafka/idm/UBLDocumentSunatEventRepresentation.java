package io.github.project.openubl.xsender.kafka.idm;

import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xsender.models.DeliveryStatusType;

public class UBLDocumentSunatEventRepresentation {

    private String id;
    private String storageFile;

    private DeliveryStatusType deliveryStatus;

    private String sunatUsername;
    private String sunatPassword;
    private String sunatUrlFactura;
    private String sunatUrlGuiaRemision;
    private String sunatUrlPercepcionRetencion;

    private XmlContentModel fileContent;
    private Boolean isFileContentValid;
    private String fileContentValidationMessage;

    private UBLRetry retry;
    private BillServiceContentRepresentation billServiceContent;

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

    public XmlContentModel getFileContent() {
        return fileContent;
    }

    public void setFileContent(XmlContentModel fileContent) {
        this.fileContent = fileContent;
    }

    public Boolean getFileContentValid() {
        return isFileContentValid;
    }

    public void setFileContentValid(Boolean fileContentValid) {
        isFileContentValid = fileContentValid;
    }

    public String getFileContentValidationMessage() {
        return fileContentValidationMessage;
    }

    public void setFileContentValidationMessage(String fileContentValidationMessage) {
        this.fileContentValidationMessage = fileContentValidationMessage;
    }

    public BillServiceContentRepresentation getBillServiceContent() {
        return billServiceContent;
    }

    public void setBillServiceContent(BillServiceContentRepresentation billServiceContent) {
        this.billServiceContent = billServiceContent;
    }

    public DeliveryStatusType getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatusType deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public UBLRetry getRetry() {
        return retry;
    }

    public void setRetry(UBLRetry retry) {
        this.retry = retry;
    }
}

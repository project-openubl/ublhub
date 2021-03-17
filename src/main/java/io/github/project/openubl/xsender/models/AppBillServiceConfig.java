package io.github.project.openubl.xsender.models;

import io.github.project.openubl.xmlsenderws.webservices.managers.smart.custom.CustomBillServiceConfig;

public class AppBillServiceConfig implements CustomBillServiceConfig {

    private final String invoiceUrl;
    private final String perception;
    private final String despatchUrl;

    public AppBillServiceConfig(String invoiceUrl, String perception, String despatchUrl) {
        this.invoiceUrl = invoiceUrl;
        this.perception = perception;
        this.despatchUrl = despatchUrl;
    }

    @Override
    public String getInvoiceAndNoteDeliveryURL() {
        return invoiceUrl;
    }

    @Override
    public String getPerceptionAndRetentionDeliveryURL() {
        return perception;
    }

    @Override
    public String getDespatchAdviceDeliveryURL() {
        return despatchUrl;
    }
}

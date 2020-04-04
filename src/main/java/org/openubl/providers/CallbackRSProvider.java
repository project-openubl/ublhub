package org.openubl.providers;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openubl.models.MultipartBody;
import org.openubl.resources.client.CallbackMultipartService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class CallbackRSProvider {

    @ConfigProperty(name = "org.openubl.resources.client.MultiCallbackMultipartService/mp-rest/url")
    String callbackUrl;

    @Inject
    @RestClient
    CallbackMultipartService service;

    public void sendCallback(BillServiceModel billServiceModel) {
        if (callbackUrl == null) {
            return;
        }

        MultipartBody body = new MultipartBody();
        body.ticket = billServiceModel.getTicket();
        body.description = billServiceModel.getDescription();
        body.code = billServiceModel.getCode().toString();
        body.status = billServiceModel.getStatus().toString();

        if (billServiceModel.getCdr() != null) {
            body.crd = new ByteArrayInputStream("HELLO WORLD".getBytes(StandardCharsets.UTF_8));
        }

        service.sendMultipartData(body);
    }

}

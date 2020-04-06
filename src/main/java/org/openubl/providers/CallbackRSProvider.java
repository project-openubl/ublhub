package org.openubl.providers;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.openubl.jms.SendCallbackJMSConsumer;
import org.openubl.models.MultipartBody;
import org.openubl.resources.client.CallbackMultipartService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class CallbackRSProvider {

    private static final Logger LOG = Logger.getLogger(CallbackRSProvider.class);

    @ConfigProperty(name = "org.openubl.enableCallback")
    boolean enableCallback;

    @ConfigProperty(name = "org.openubl.resources.client.CallbackMultipartService/mp-rest/url")
    String callbackUrl;

    @Inject
    @RestClient
    CallbackMultipartService service;

    public boolean sendCallback(BillServiceModel billServiceModel) {
        if (!enableCallback || callbackUrl == null) {
            LOG.info("Not sending message to client");
            return true;
        }

        MultipartBody body = new MultipartBody();
        body.ticket = billServiceModel.getTicket();
        body.description = billServiceModel.getDescription();
        body.code = billServiceModel.getCode().toString();
        body.status = billServiceModel.getStatus().toString();

        if (billServiceModel.getCdr() != null) {
            body.crd = new ByteArrayInputStream(billServiceModel.getCdr());
        }

        try {
            service.sendMultipartData(body);
        } catch (Exception e) {
            LOG.error("Could not send to client");
            return false;
        }

        return true;
    }

}

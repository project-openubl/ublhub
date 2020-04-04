package org.openubl.providers;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CallbackRSProvider {

    @ConfigProperty(name = "org.openubl.resources.client.MultiCallbackMultipartService/mp-rest/url")
    String callbackUrl;

    public void sendCallback(BillServiceModel billServiceModel) {
        if (callbackUrl != null) {
            System.out.println("sending");
        } else {
            System.out.println("nothing to do");
        }
    }

}

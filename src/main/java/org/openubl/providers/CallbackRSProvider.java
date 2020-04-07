package org.openubl.providers;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CallbackRSProvider {

    private static final Logger LOG = Logger.getLogger(CallbackRSProvider.class);

    public boolean sendCallback(BillServiceModel billServiceModel) {
        System.out.println("finish hime");

        return true;
    }

}

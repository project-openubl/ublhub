package io.github.project.openubl.ublhub.ubl.renderer;

import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class InvoiceRenderer {

    @Inject
    @Location("ubl/standard/general/invoice.xml")
    Template invoiceTemplate;

    public Uni<String> renderInvoice(BoletaFactura data) {
        return Uni.createFrom().completionStage(() -> invoiceTemplate.data(data).renderAsync());
    }

}

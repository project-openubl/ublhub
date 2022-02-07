package io.github.project.openubl.ublhub.ubl.content.enricher;

import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.DocumentoDetalle;
import io.github.project.openubl.ublhub.ubl.content.ruleunits.InvoiceLineUnit;
import io.github.project.openubl.ublhub.ubl.content.ruleunits.InvoiceTotalImpuestosUnit;
import io.github.project.openubl.ublhub.ubl.content.ruleunits.InvoiceUnit;
import org.kie.kogito.incubation.application.AppRoot;
import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.rules.QueryId;
import org.kie.kogito.incubation.rules.RuleUnitIds;
import org.kie.kogito.incubation.rules.services.RuleUnitService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Dependent
public class InvoiceEnricher {

    @Inject
    AppRoot appRoot;

    @Inject
    RuleUnitService svc;

    public BoletaFactura enrich(BoletaFactura invoiceDto) {
        // General
        QueryId invoiceQueryId = appRoot.get(RuleUnitIds.class).get(InvoiceUnit.class).queries().get("boletaFactura");
        DataContext invoiceCtx = MapDataContext.of(Map.of("invoice", invoiceDto));
        Stream<BoletaFactura> invoiceStream = svc.evaluate(invoiceQueryId, invoiceCtx).map(dc -> dc.as(MapDataContext.class).get("$i", BoletaFactura.class));

        BoletaFactura enrichedInvoice = invoiceStream.findFirst().orElseThrow(IllegalStateException::new);

        // Lines
        QueryId invoiceLineQueryId = appRoot.get(RuleUnitIds.class).get(InvoiceLineUnit.class).queries().get("detalle");
        MapDataContext invoiceLineCtx = MapDataContext.of(Map.of("invoiceLines", new LinkedList<>(invoiceDto.detalle)));
        Stream<DocumentoDetalle> invoiceLineStream = svc.evaluate(invoiceLineQueryId, invoiceLineCtx).map(dc -> dc.as(MapDataContext.class).get("$il", DocumentoDetalle.class));

        enrichedInvoice.detalle = invoiceLineStream.collect(Collectors.toList());

        // Totals
        invoiceQueryId = appRoot.get(RuleUnitIds.class).get(InvoiceTotalImpuestosUnit.class).queries().get("boletaFacturaTotalImpuestos");
        invoiceCtx = MapDataContext.of(Map.of("invoice", enrichedInvoice));
        invoiceStream = svc.evaluate(invoiceQueryId, invoiceCtx).map(dc -> dc.as(MapDataContext.class).get("$i", BoletaFactura.class));

        enrichedInvoice = invoiceStream.findFirst().orElseThrow(IllegalStateException::new);

        // Result
        return enrichedInvoice;
    }


}

package io.github.project.openubl.ublhub.ubl.content.enricher;

import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BaseDocumento;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.DocumentoDetalle;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.NotaDeCredito;
import io.github.project.openubl.ublhub.ubl.content.ruleunits.*;

import io.github.project.openubl.xmlbuilderlib.config.Config;
import org.kie.kogito.incubation.application.AppRoot;
import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.rules.QueryId;
import org.kie.kogito.incubation.rules.RuleUnitIds;
import org.kie.kogito.incubation.rules.services.RuleUnitService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Dependent
public class ContentEnricher {

    @Inject
    AppRoot appRoot;

    @Inject
    RuleUnitService svc;

    @Inject
    Config config;

    private <T> T baseDocumentEnrich(BaseDocumento dto, Class<T> expectedType) {
        QueryId queryId = appRoot.get(RuleUnitIds.class).get(InitialEnrichBaseDocumentoUnit.class).queries().get("document");
        DataContext dataCtx = MapDataContext.of(Map.of("config", config, "document", dto));
        Stream<T> stream = svc.evaluate(queryId, dataCtx).map(dc -> dc.as(MapDataContext.class).get("$d", expectedType));

        return stream.findFirst().orElseThrow(IllegalStateException::new);
    }

    private List<DocumentoDetalle> documentoDetalleEnrich(List<DocumentoDetalle> dtos) {
        QueryId queryId = appRoot.get(RuleUnitIds.class).get(InitialEnrichDocumentoDetalleUnit.class).queries().get("documentLines");
        DataContext dataCtx = MapDataContext.of(Map.of("config", config, "documentLines", dtos));
        Stream<DocumentoDetalle> stream = svc.evaluate(queryId, dataCtx).map(dc -> dc.as(MapDataContext.class).get("$dl", DocumentoDetalle.class));

        return stream.collect(Collectors.toList());
    }

    public BoletaFactura enrich(BoletaFactura dto) {
        // General
        dto = baseDocumentEnrich(dto, BoletaFactura.class);

        // Invoice
        QueryId queryId = appRoot.get(RuleUnitIds.class).get(InitialEnrichInvoiceUnit.class).queries().get("invoice");
        DataContext dataCtx = MapDataContext.of(Map.of("config", config, "invoice", dto));
        Stream<BoletaFactura> stream = svc.evaluate(queryId, dataCtx).map(dc -> dc.as(MapDataContext.class).get("$i", BoletaFactura.class));

        dto = stream.findFirst().orElseThrow(IllegalStateException::new);

        // Lines
        dto.detalle = documentoDetalleEnrich(dto.detalle);

        // Totales
        queryId = appRoot.get(RuleUnitIds.class).get(FinalizeEnrichInvoiceUnit.class).queries().get("invoiceWithTotals");
        dataCtx = MapDataContext.of(Map.of("config", config, "invoice", dto));
        stream = svc.evaluate(queryId, dataCtx).map(dc -> dc.as(MapDataContext.class).get("$i", BoletaFactura.class));

        dto = stream.findFirst().orElseThrow(IllegalStateException::new);

        // Result
        return dto;
    }

    public NotaDeCredito enrich(NotaDeCredito dto) {
        // General
        dto = baseDocumentEnrich(dto, NotaDeCredito.class);

        // Credit note
        QueryId queryId = appRoot.get(RuleUnitIds.class).get(InitialEnrichCreditNoteUnit.class).queries().get("creditNote");
        DataContext dataCtx = MapDataContext.of(Map.of("config", config, "creditNote", dto));
        Stream<NotaDeCredito> stream = svc.evaluate(queryId, dataCtx).map(dc -> dc.as(MapDataContext.class).get("$cn", NotaDeCredito.class));

        dto = stream.findFirst().orElseThrow(IllegalStateException::new);

        // Lines
        dto.detalle = documentoDetalleEnrich(dto.detalle);

        // Result
        return dto;
    }


}

package io.github.project.openubl.ublhub.ubl;

import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.github.project.openubl.ublhub.ubl.enricher.ContentEnricher;
import io.github.project.openubl.ublhub.ubl.renderer.Renderer;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XMLBuilder {

    @Inject
    ContentEnricher contentEnricher;

    @Inject
    Renderer xmlRenderer;

    public Uni<String> enrichAndRenderAsync(BoletaFactura dto) {
        BoletaFactura data = contentEnricher.enrich(dto);
        return xmlRenderer.renderAsync(data);
    }

}
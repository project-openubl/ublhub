package io.github.project.openubl.ublhub.ubl.renderer.template.extensions;

import io.github.project.openubl.ublhub.ubl.content.catalogs.Catalog;
import io.github.project.openubl.ublhub.ubl.content.catalogs.Catalog7;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.DocumentoDetalle;
import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class DocumentoDetalleTemplateExtension {

    public static Catalog7 igvTipoCatalog(DocumentoDetalle documentoDetalle) {
        return Catalog.valueOfCode(Catalog7.class, documentoDetalle.igvTipo)
                .orElseThrow(Catalog.invalidCatalogValue);
    }

}

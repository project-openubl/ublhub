package io.github.project.openubl.ublhub.ubl.renderer.template.extensions;

import io.github.project.openubl.ublhub.ubl.content.models.standard.general.CuotaDePago;
import io.quarkus.qute.TemplateExtension;

import java.math.BigDecimal;
import java.util.List;

@TemplateExtension
public class CuotaDePagoListTemplateExtension {

    public static BigDecimal importeTotalCuotasDePago(List<CuotaDePago> formaDePagoCuotas) {
        return formaDePagoCuotas.stream().map(f -> f.importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}

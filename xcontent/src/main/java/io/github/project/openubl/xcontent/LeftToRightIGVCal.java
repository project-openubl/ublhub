package io.github.project.openubl.xcontent;

import io.github.project.openubl.xcontent.catalogs.Catalog7;
import io.github.project.openubl.xcontent.catalogs.Catalog7_1;
import io.github.project.openubl.xcontent.common.Config;
import io.github.project.openubl.xcontent.common.ConfigProducer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LeftToRightIGVCal implements IGVCalStrategy {

    private final Catalog7 tipoIgv;
    private final BigDecimal cantidad;
    private final BigDecimal precioSinImpuestos;

    public LeftToRightIGVCal(Catalog7 tipoIgv, BigDecimal cantidad, BigDecimal precioSinImpuestos) {
        this.cantidad = cantidad;
        this.precioSinImpuestos = precioSinImpuestos;
        this.tipoIgv = tipoIgv;
    }

    @Override
    public BigDecimal getBaseImponible() {
        return cantidad
                .multiply(precioSinImpuestos)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal getImporte() {
        TipoIgvWrapper tipoIgvWrapper = new TipoIgvWrapper(tipoIgv);
        return getBaseImponible()
                .multiply(tipoIgvWrapper.getIGVValor())
                .setScale(2, RoundingMode.HALF_EVEN);
    }

}

package io.github.project.openubl.xcontent;

import io.github.project.openubl.xcontent.catalogs.Catalog7;
import io.github.project.openubl.xcontent.catalogs.Catalog7_1;
import io.github.project.openubl.xcontent.common.Config;
import io.github.project.openubl.xcontent.common.ConfigProducer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RightToLeftIGVCal implements IGVCalStrategy {

    private final Catalog7 tipoIgv;
    private final BigDecimal cantidad;
    private final BigDecimal precioConIgv;

    public RightToLeftIGVCal(Catalog7 tipoIgv, BigDecimal cantidad, BigDecimal precioConIgv) {
        this.cantidad = cantidad;
        this.precioConIgv = precioConIgv;
        this.tipoIgv = tipoIgv;
    }

    public BigDecimal getTotal() {
        return cantidad.multiply(precioConIgv).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal getBaseImponible() {
        TipoIgvWrapper tipoIgvWrapper = new TipoIgvWrapper(tipoIgv);
        return getTotal()
                .divide(tipoIgvWrapper.getIGVValor().add(BigDecimal.ONE), 2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal getImporte() {
        return getTotal()
                .subtract(getBaseImponible());
    }

}

package io.github.project.openubl.xcontent;

import java.math.BigDecimal;

public interface DocumentLine {

    String getDescripcion();
    String getUnidadMedida();

    BigDecimal getCantidad();

    BigDecimal getPrecioSinImpuestos();
    BigDecimal getPrecioConImpuestos();

    BigDecimal getPrecioConIgv();
    BigDecimal getPrecioSinIgv();

    BigDecimal getValorDeVentaSinImpuestos();
    BigDecimal getValorDeVentaConImpuestos();

}

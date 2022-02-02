package io.github.project.openubl.xcontent;

import java.math.BigDecimal;

public class DocumentLineImpl implements DocumentLine {

    private final String descripcion;
    private final String unidadMedida;
    private final BigDecimal cantidad;

    public DocumentLineImpl(String descripcion, String unidadMedida, BigDecimal cantidad, BigDecimal precioSinImpuestos) {
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.cantidad = cantidad;
    }

    @Override
    public String getDescripcion() {
        return null;
    }

    @Override
    public String getUnidadMedida() {
        return null;
    }

    @Override
    public BigDecimal getCantidad() {
        return null;
    }

    @Override
    public BigDecimal getPrecioConImpuestos() {
        return null;
    }

    @Override
    public BigDecimal getPrecioSinImpuestos() {
        return null;
    }

    @Override
    public BigDecimal getPrecioConIgv() {
        return null;
    }

    @Override
    public BigDecimal getPrecioSinIgv() {
        return null;
    }

    @Override
    public BigDecimal getValorDeVentaSinImpuestos() {
        return null;
    }

    @Override
    public BigDecimal getValorDeVentaConImpuestos() {
        return null;
    }
}

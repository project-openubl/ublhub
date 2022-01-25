package io.github.project.openubl.ublhub.ubl.builder.idgenerator;

public class ID {
    private final String serie;
    private final int numero;

    public ID(String serie, int numero) {
        this.serie = serie;
        this.numero = numero;
    }

    public String getSerie() {
        return serie;
    }

    public int getNumero() {
        return numero;
    }
}

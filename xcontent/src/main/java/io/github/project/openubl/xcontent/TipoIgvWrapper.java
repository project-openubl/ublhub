package io.github.project.openubl.xcontent;

import io.github.project.openubl.xcontent.catalogs.Catalog7;
import io.github.project.openubl.xcontent.catalogs.Catalog7_1;
import io.github.project.openubl.xcontent.common.Config;
import io.github.project.openubl.xcontent.common.ConfigProducer;

import java.math.BigDecimal;

public class TipoIgvWrapper {

    private final Catalog7 tipoIgv;

    public TipoIgvWrapper(Catalog7 tipoIgv) {
        this.tipoIgv = tipoIgv;
    }

    public BigDecimal getIGVValor() {
        Config config = ConfigProducer.getInstance();

        BigDecimal result;
        if (tipoIgv.getGrupo().equals(Catalog7_1.GRAVADO)) {
            result = tipoIgv.equals(Catalog7.GRAVADO_IVAP) ? config.ivap : config.igv;
        } else {
            result = BigDecimal.ZERO;
        }
        return result;
    }
}

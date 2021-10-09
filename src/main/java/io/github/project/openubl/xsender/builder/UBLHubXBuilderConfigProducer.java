package io.github.project.openubl.xsender.builder;

import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog10;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog7;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog9;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.math.BigDecimal;
import java.util.TimeZone;

@ApplicationScoped
public class UBLHubXBuilderConfigProducer {

    @ConfigProperty(name = "openubl.xbuilder.igv")
    BigDecimal igv;

    @ConfigProperty(name = "openubl.xbuilder.ivap")
    BigDecimal ivap;

    @ConfigProperty(name = "openubl.xbuilder.defaultMoneda")
    String defaultMoneda;

    @ConfigProperty(name = "openubl.xbuilder.defaultUnidadMedida")
    String defaultUnidadMedida;

    @ConfigProperty(name = "openubl.xbuilder.defaultTipoNotaCredito")
    String defaultTipoNotaCredito;

    @ConfigProperty(name = "openubl.xbuilder.defaultTipoNotaDebito")
    String defaultTipoNotaDebito;

    @ConfigProperty(name = "openubl.xbuilder.defaultIcb")
    BigDecimal defaultIcb;

    @ConfigProperty(name = "openubl.xbuilder.defaultTipoIgv")
    String defaultTipoIgv;

    //

    @ConfigProperty(name = "openubl.xbuilder.timezone")
    String timezone;

    @Produces
    public UblHubXBuilderConfig produceConfig() {
        UblHubXBuilderConfig config = new UblHubXBuilderConfig();

        config.setIgv(igv);
        config.setIvap(ivap);
        config.setDefaultMoneda(defaultMoneda);
        config.setDefaultUnidadMedida(defaultUnidadMedida);
        config.setDefaultTipoNotaCredito(Catalog.valueOfCode(Catalog9.class, defaultTipoNotaCredito)
                .orElseThrow(() -> new IllegalStateException("Invalid defaultTipoNotaCredito in config"))
        );
        config.setDefaultTipoNotaDebito(Catalog.valueOfCode(Catalog10.class, defaultTipoNotaDebito)
                .orElseThrow(() -> new IllegalStateException("Invalid defaultTipoNotaDebito in config"))
        );
        config.setDefaultIcb(defaultIcb);
        config.setDefaultTipoIgv(Catalog.valueOfCode(Catalog7.class, defaultTipoIgv)
                .orElseThrow(() -> new IllegalStateException("Invalid defaultTipoIgv in config"))
        );

        return config;
    }

    @Produces
    public UblHubXBuilderClock produceClock() {
        UblHubXBuilderClock clock = new UblHubXBuilderClock();
        clock.setTimeZone(TimeZone.getTimeZone(timezone));

        return clock;
    }
}

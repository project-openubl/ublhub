package io.github.project.openubl.ublhub.flyway;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Optional;

@ApplicationScoped
public class RunFlyway {

    @ConfigProperty(name = "quarkus.datasource.reactive.url", defaultValue = "")
    Optional<String> datasourceUrl;

    @ConfigProperty(name = "quarkus.datasource.username", defaultValue = "")
    Optional<String> datasourceUsername;

    @ConfigProperty(name = "quarkus.datasource.password", defaultValue = "")
    Optional<String> datasourcePassword;

    public void runFlywayMigration(@Observes StartupEvent event) {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        datasourceUrl.orElse("").replace("vertx-reactive", "jdbc"),
                        datasourceUsername.orElse(""),
                        datasourcePassword.orElse("")
                )
                .load();
        flyway.migrate();
    }

}

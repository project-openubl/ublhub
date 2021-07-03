package io.github.project.openubl.xsender.resources.common;

import io.quarkus.test.junit.callback.*;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.flywaydb.core.Flyway;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class FlywayTestCallback implements QuarkusTestBeforeEachCallback, QuarkusTestAfterEachCallback {

    @Override
    public void beforeEach(QuarkusTestMethodContext context) {
        Flyway flyway = flyway(context);
        flyway.clean();
        flyway.migrate();
    }

    @Override
    public void afterEach(QuarkusTestMethodContext context) {
        Flyway flyway = flyway(context);
        flyway.clean();
    }

    private Flyway flyway(final QuarkusTestMethodContext context) {
        Config config = ConfigProvider.getConfig();
        String username = config.getConfigValue("quarkus.datasource.username").getValue();
        String password = config.getConfigValue("quarkus.datasource.password").getValue();
        String reactiveUrl = config.getConfigValue("quarkus.datasource.reactive.url").getValue();
        String jdbUrl = reactiveUrl.replaceFirst("vertx-reactive", "jdbc");

        // Flyway
        final String packageName = context.getTestInstance().getClass().getName();
        final String testDefaultLocation = "db/" + packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        final List<String> locations = new ArrayList<>();
        locations.add(testDefaultLocation);
        locations.add("db" + File.separator + "migration");

        return Flyway.configure()
                .dataSource(jdbUrl, username, password)
                .connectRetries(120)
                .locations(locations.toArray(String[]::new))
                .load();
    }

}

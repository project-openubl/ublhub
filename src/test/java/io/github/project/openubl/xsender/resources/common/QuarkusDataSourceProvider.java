package io.github.project.openubl.xsender.resources.common;

import com.radcortez.flyway.test.junit.DataSourceInfo;
import com.radcortez.flyway.test.junit.DataSourceProvider;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.URL;

public class QuarkusDataSourceProvider implements DataSourceProvider {

    @Override
    public DataSourceInfo getDatasourceInfo(final ExtensionContext extensionContext) {
        // We don't have access to the Quarkus CL here, so we cannot use ConfigProvider.getConfig() to retrieve the same configuration.
        URL properties = Thread.currentThread().getContextClassLoader().getResource("application.properties");
        assert properties != null;

        try {
            SmallRyeConfig config = new SmallRyeConfigBuilder()
                    .withSources(new PropertiesConfigSource(properties))
                    .withProfile("test")
                    .build();

            String reactiveUrl = config.getRawValue("quarkus.datasource.reactive.url");
            String username = config.getRawValue("quarkus.datasource.username");
            String password = config.getRawValue("quarkus.datasource.password");

            return DataSourceInfo.config(reactiveUrl.replaceFirst("vertx-reactive", "jdbc"), username, password);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

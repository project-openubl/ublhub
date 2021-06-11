package io.github.project.openubl.xsender.resources;

import com.radcortez.flyway.test.junit.DataSourceInfo;
import com.radcortez.flyway.test.junit.DataSourceProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class QuarkusDataSourceProvider implements DataSourceProvider {
    public volatile static AtomicInteger DatabasePORT = new AtomicInteger();

    @Override
    public DataSourceInfo getDatasourceInfo(final ExtensionContext extensionContext) {
        String port = "";
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("esteban.txt"));
            port = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DataSourceInfo.config("jdbc:postgresql://localhost:" + port + "/xsender_db", "xsender_username", "xsender_password");

    }
}

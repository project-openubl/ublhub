package io.github.project.openubl.xsender.resources.health.storage.impl;

import io.github.project.openubl.xsender.resources.health.storage.StorageProvider;
import io.github.project.openubl.xsender.resources.health.storage.StorageReadinessCheck;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
@StorageProvider(StorageProvider.Type.S3)
public class S3ReadinessCheck implements StorageReadinessCheck {

    @ConfigProperty(name = "openubl.storage.s3.health.url")
    private String s3HostHealthCheckUrl;

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(s3HostHealthCheckUrl))
                    .GET()
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return false;
        }
    }
}

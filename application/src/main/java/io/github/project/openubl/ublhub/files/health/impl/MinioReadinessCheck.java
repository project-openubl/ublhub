/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.files.health.impl;

import io.github.project.openubl.ublhub.files.health.StorageProvider;
import io.github.project.openubl.ublhub.files.health.StorageReadinessCheck;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@ApplicationScoped
@StorageProvider(StorageProvider.Type.MINIO)
public class MinioReadinessCheck implements StorageReadinessCheck {

    @ConfigProperty(name = "openubl.storage.minio.health.url")
    Optional<String> minioHostHealthCheckUrl;

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public boolean isHealthy() {
        if (minioHostHealthCheckUrl.isPresent()) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(minioHostHealthCheckUrl.get()))
                        .GET()
                        .build();
                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                return response.statusCode() == 200;
            } catch (URISyntaxException | IOException | InterruptedException e) {
                return false;
            }
        } else {
            return true;
        }
    }
}

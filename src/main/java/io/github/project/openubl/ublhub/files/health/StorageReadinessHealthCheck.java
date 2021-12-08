/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.files.health;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;

@Readiness
@ApplicationScoped
public class StorageReadinessHealthCheck implements HealthCheck {

    @ConfigProperty(name = "openubl.storage.type")
    String storageType;

    @Inject
    @Any
    Instance<StorageReadinessCheck> storageReadinessChecks;

    @Override
    public HealthCheckResponse call() {
        StorageProvider.Type providerType = StorageProvider.Type.valueOf(storageType.toUpperCase());
        Annotation annotation = new StorageProviderLiteral(providerType);

        StorageReadinessCheck readinessCheck = storageReadinessChecks.select(annotation).get();
        boolean healthy = readinessCheck.isHealthy();

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Storage connection health check");
        if (healthy) {
            responseBuilder.up();
        } else {
            responseBuilder.down();
        }
        return responseBuilder.build();
    }

}

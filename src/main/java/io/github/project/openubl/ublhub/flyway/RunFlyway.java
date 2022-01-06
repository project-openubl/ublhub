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

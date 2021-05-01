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
package io.github.project.openubl.xsender.basic.resources.config;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PostgreSQLServer implements QuarkusTestResourceLifecycleManager {

    private GenericContainer postgreSQL;

    @Override
    public Map<String, String> start() {
        postgreSQL = new GenericContainer("postgres:" + System.getProperty("postgresql.version", "13.1"))
                .withExposedPorts(5432)
                .withEnv("POSTGRES_USER", "xsender_username")
                .withEnv("POSTGRES_PASSWORD", "xsender_password")
                .withEnv("POSTGRES_DB", "xsender_db");
        postgreSQL.start();

        String host = postgreSQL.getHost();
        Integer port = postgreSQL.getMappedPort(5432);

        return new HashMap<>() {{
            put("quarkus.datasource.jdbc.url", "jdbc:postgresql://" + host + ":" + port + "/xsender_db");
            put("quarkus.datasource.username", "xsender_username");
            put("quarkus.datasource.password", "xsender_password");
        }};
    }

    @Override
    public void stop() {
        postgreSQL.stop();
    }
}

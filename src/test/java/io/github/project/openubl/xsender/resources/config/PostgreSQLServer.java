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
package io.github.project.openubl.xsender.resources.config;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;

public class PostgreSQLServer implements QuarkusTestResourceLifecycleManager {

    public static final String NETWORK_ALIAS = "postgresql";
    public static final int CONTAINER_PORT = 5432;

    public static final String DB_NAME = "xsender_db";
    public static final String DB_USERNAME = "xsender_username";
    public static final String DB_PASSWORD = "xsender_password";

    private GenericContainer<?> postgreSQL;

    @Override
    public Map<String, String> start() {
        postgreSQL = new GenericContainer<>("debezium/postgres:12")
                .withNetwork(KeycloakServer.network)
                .withNetworkAliases(NETWORK_ALIAS)
                .withExposedPorts(CONTAINER_PORT)
                .withEnv("POSTGRES_DB", DB_NAME)
                .withEnv("POSTGRES_USER", DB_USERNAME)
                .withEnv("POSTGRES_PASSWORD", DB_PASSWORD);
        postgreSQL.start();

        String host = postgreSQL.getHost();
        Integer port = postgreSQL.getMappedPort(CONTAINER_PORT);

        return new HashMap<>() {{
            put("quarkus.datasource.jdbc.url", "jdbc:postgresql://" + host + ":" + port + "/" + DB_NAME);
            put("quarkus.datasource.username", DB_USERNAME);
            put("quarkus.datasource.password", DB_PASSWORD);
        }};
    }

    @Override
    public void stop() {
        postgreSQL.stop();
    }

    @Override
    public int order() {
        return 10;
    }
}

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
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class KafkaServer implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> zookeeper;
    private GenericContainer<?> kafka;
    private GenericContainer<?> kafkaConnect;

    @Override
    public Map<String, String> start() {
        zookeeper = new GenericContainer<>("debezium/zookeeper:1.3.1.Final")
                .withNetwork(KeycloakServer.network)
                .withNetworkAliases("zookeeper")
                .withExposedPorts(2181)
                .waitingFor(Wait.forLogMessage(".* Started AdminServer on address .*", 1));
        zookeeper.start();

        kafka = new GenericContainer<>("debezium/kafka:1.3.1.Final")
                .withNetwork(KeycloakServer.network)
                .withNetworkAliases("kafka")
                .withExposedPorts(9092)
                .withEnv("ZOOKEEPER_CONNECT", "zookeeper:2181")
                .waitingFor(Wait.forLogMessage(".* Started socket server acceptors .*", 1));
        kafka.start();

        kafkaConnect = new GenericContainer<>("debezium/connect:1.3.1.Final")
                .withNetwork(KeycloakServer.network)
                .withNetworkAliases("kafka-connect")
                .withExposedPorts(8083)
                .withEnv("BOOTSTRAP_SERVERS", "kafka:9092")
                .withEnv("GROUP_ID", "1")
                .withEnv("CONFIG_STORAGE_TOPIC", "debezium_connect_config")
                .withEnv("OFFSET_STORAGE_TOPIC", "debezium_connect_offsets")
                .withEnv("STATUS_STORAGE_TOPIC", "debezium_connect_status")
                .waitingFor(Wait.forLogMessage(".* Kafka Connect started .*", 1));
        kafkaConnect.start();

        String host = kafka.getHost();
        Integer port = kafka.getMappedPort(9092);

        //

        String payload = "{\n" +
                "  \"name\": \"postgresql-connector\",\n" +
                "  \"config\": {\n" +
                "    \"connector.class\": \"io.debezium.connector.postgresql.PostgresConnector\",\n" +
                "    \"tasks.max\": \"1\",\n" +
                "    \"database.hostname\": \":databaseHost\",\n" +
                "    \"database.port\": \":databasePort\",\n" +
                "    \"database.dbname\": \":databaseName\",\n" +
                "    \"database.user\": \":databaseUsername\",\n" +
                "    \"database.password\": \":databasePassword\",\n" +
                "    \"database.server.name\": \"dbserver1\",\n" +
                "    \"schema.include.list\": \"public\",\n" +
                "    \"table.include.list\": \"public.outboxevent\",\n" +
                "    \"tombstones.on.delete\": \"false\",\n" +
                "    \"transforms\": \"outbox\",\n" +
                "    \"transforms.outbox.type\": \"io.debezium.transforms.outbox.EventRouter\",\n" +
                "    \"transforms.outbox.table.fields.additional.placement\": \"type:header:eventType\",\n" +
                "    \"key.converter\": \"org.apache.kafka.connect.json.JsonConverter\",\n" +
                "    \"key.converter.schemas.enable\": \"false\",\n" +
                "    \"value.converter\": \"org.apache.kafka.connect.json.JsonConverter\",\n" +
                "    \"value.converter.schemas.enable\": \"false\"\n" +
                "  }\n" +
                "}\n";

        payload = payload.replaceFirst(":databaseHost", PostgreSQLServer.NETWORK_ALIAS)
                .replaceFirst(":databasePort", String.valueOf(PostgreSQLServer.CONTAINER_PORT))
                .replaceFirst(":databaseName", PostgreSQLServer.DB_NAME)
                .replaceFirst(":databaseUsername", PostgreSQLServer.DB_USERNAME)
                .replaceFirst(":databasePassword", PostgreSQLServer.DB_PASSWORD);

        String kafkaConnectUrl = "http://" + kafkaConnect.getHost() + ":" + kafkaConnect.getMappedPort(8083);

        int statusCode = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(payload)
                .when()
                .post(kafkaConnectUrl + "/connectors/")
                .statusCode();
        assertEquals(201, statusCode);

        return Collections.singletonMap("kafka.bootstrap.servers", host + ":" + port);
    }

    @Override
    public void stop() {
//        network.close();

        zookeeper.stop();
        kafka.stop();
        kafkaConnect.stop();
    }

    @Override
    public int order() {
        return 20;
    }
}

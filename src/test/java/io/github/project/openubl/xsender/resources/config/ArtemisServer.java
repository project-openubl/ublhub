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
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.HashMap;
import java.util.Map;

public class ArtemisServer implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> artemis;

    @Override
    public Map<String, String> start() {
        artemis = new GenericContainer<>("vromero/activemq-artemis:2.9.0-alpine")
                .withExposedPorts(8161, 61616, 5672)
                .withEnv("ARTEMIS_USERNAME", "openubl")
                .withEnv("ARTEMIS_PASSWORD", "openubl")
                .waitingFor(Wait.forLogMessage(".* Artemis Console available at .*", 1));
        artemis.start();

        String host = artemis.getHost();
        Integer port = artemis.getMappedPort(5672);

        return new HashMap<>() {{
            put("openubl.sender.type", "amqp");
            put("amqp-host", host);
            put("amqp-port", port.toString());
            put("amqp-username", "openubl");
            put("amqp-password", "openubl");
        }};
    }

    @Override
    public void stop() {
        artemis.stop();
    }

}

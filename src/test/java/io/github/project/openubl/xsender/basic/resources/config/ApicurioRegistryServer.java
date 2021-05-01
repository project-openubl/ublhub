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
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Collections;
import java.util.Map;

public class ApicurioRegistryServer implements QuarkusTestResourceLifecycleManager {

    private GenericContainer apicurio;

    @Override
    public Map<String, String> start() {
        apicurio = new GenericContainer("quay.io/apicurio/apicurio-registry-mem:" + System.getProperty("apicurio.version", "1.3.2.Final"))
                .withExposedPorts(8080)
                .withEnv("QUARKUS_PROFILE", "prod")
                .waitingFor(Wait.forHttp("/ui"));
        apicurio.start();

        String host = apicurio.getHost();
        Integer port = apicurio.getMappedPort(8080);

        return Collections.singletonMap(
                "mp.messaging.connector.smallrye-kafka.apicurio.registry.url",
                "http://" + host + ":" + port
        );
    }

    @Override
    public void stop() {
        apicurio.stop();
    }
}

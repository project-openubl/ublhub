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
package io.github.project.openubl.ublhub.containers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.HashMap;
import java.util.Map;

public class MinioServer implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> minio;

    @Override
    public Map<String, String> start() {
        minio = new GenericContainer<>("quay.io/minio/minio:latest")
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", "BQA2GEXO711FVBVXDWKM")
                .withEnv("MINIO_SECRET_KEY", "uvgz3LCwWM3e400cDkQIH/y1Y4xgU4iV91CwFSPC")
                .withCommand("server /data")
                .waitingFor(Wait.forHttp("/minio/health/live"));
        minio.start();

        String host = minio.getHost();
        Integer port = minio.getMappedPort(9000);

        Map<String, String> properties = new HashMap<>();
        properties.put("openubl.storage.minio.host", "http://" + host + ":" + port);
        properties.put("openubl.storage.minio.health.url", "http://" + host + ":" + port + "/minio/health/live");
        properties.put("openubl.storage.minio.access_key_id", "BQA2GEXO711FVBVXDWKM");
        properties.put("openubl.storage.minio.secret_access_key", "uvgz3LCwWM3e400cDkQIH/y1Y4xgU4iV91CwFSPC");

        return properties;
    }

    @Override
    public void stop() {
        minio.stop();
    }

}

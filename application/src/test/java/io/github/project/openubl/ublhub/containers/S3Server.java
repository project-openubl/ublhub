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
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class S3Server implements QuarkusTestResourceLifecycleManager {

    public LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3);

    @Override
    public Map<String, String> start() {
        localstack.start();

        URI host = localstack.getEndpointOverride(LocalStackContainer.Service.S3);

        Map<String, String> properties = new HashMap<>();
        properties.put("openubl.storage.s3.host", host.toString());
        properties.put("openubl.storage.s3.region", localstack.getRegion());
        properties.put("openubl.storage.s3.access_key_id", localstack.getAccessKey());
        properties.put("openubl.storage.s3.secret_access_key", localstack.getSecretKey());

        return properties;
    }

    @Override
    public void stop() {
        localstack.stop();
    }

}

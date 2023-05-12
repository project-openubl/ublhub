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
package io.github.project.openubl.ublhub.messaging;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class SqsMessagesRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.messaging.type")
    String schedulerType;

    @ConfigProperty(name = "openubl.messaging.sqs.queue")
    String sqsTopic;

    @ConfigProperty(name = "openubl.messaging.sqs.access_key_id")
    String sqsAccessKeyID;

    @ConfigProperty(name = "openubl.messaging.sqs.secret_access_key")
    String sqsSecretAccessKey;

    @ConfigProperty(name = "openubl.messaging.sqs.region")
    String sqsRegion;

    @ConfigProperty(name = "openubl.messaging.sqs.host")
    Optional<String> sqsHost;

    @Singleton
    @Produces
    @Named("amazonSQSClient")
    public SqsClient produceSqsClient() {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(sqsAccessKeyID, sqsSecretAccessKey);
        AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(awsCredentials);

        SqsClientBuilder clientBuilder = SqsClient.builder()
                .region(Region.of(sqsRegion))
                .credentialsProvider(awsCredentialsProvider);

        SqsClient client;
        if (sqsHost.isPresent()) {
            client = clientBuilder
                    .endpointOverride(URI.create(sqsHost.get()))
                    .build();
        } else {
            client = clientBuilder
                    .build();
        }

        return client;
    }

    @Override
    public void configure() throws Exception {
        from("aws2-sqs://" + sqsTopic + "?amazonSQSClient=#amazonSQSClient&autoCreateQueue=true")
                .id("sqs-send-xml")
                .precondition(String.valueOf(schedulerType.equalsIgnoreCase("sqs")))
                .to("direct:send-xml");
    }

}

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
package io.github.project.openubl.ublhub.files.camel;

import com.google.cloud.NoCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.google.storage.GoogleCloudStorageConstants;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GoogleCloudStorageFilesRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.storage.link-expiration", defaultValue = "5000")
    String linkExpiration;

    @ConfigProperty(name = "openubl.storage.gcstorage.bucket")
    String gcStorageBucket;

    @ConfigProperty(name = "openubl.storage.gcstorage.project_id")
    String gcStorageProjectId;

    @ConfigProperty(name = "openubl.storage.gcstorage.service_account_id")
    String gcStorageServiceAccountId;

    @ConfigProperty(name = "openubl.storage.gcstorage.host")
    Optional<String> gcStorageHost;

    @Named("gcStorageClient")
    @Produces
    public Storage gcStorageClient() {
        if (gcStorageHost.isPresent()) {
            StorageOptions options = StorageOptions.newBuilder()
                    .setHost(gcStorageHost.get())
                    .setProjectId(gcStorageProjectId)
//                    .setCredentials(NoCredentials.getInstance())
//                    .setRetrySettings(ServiceOptions.getNoRetrySettings())
                    .build();
            return options.getService();
        } else {
            return StorageOptions.getDefaultInstance()
                    .getService();
        }
    }

    @Override
    public void configure() throws Exception {
        from("direct:gcstorage-save-file")
                .id("gcstorage-save-file")
                .choice()
                .when(header("shouldZipFile").isEqualTo(true))
                .marshal().zipFile()
                .endChoice()
                .end()
                .process(exchange -> {
                    exchange.getIn().setHeader(GoogleCloudStorageConstants.OBJECT_NAME, UUID.randomUUID().toString());
                    exchange.getIn().setHeader(GoogleCloudStorageConstants.BUCKET_NAME, gcStorageBucket);
                })
                .toD("google-storage://" + gcStorageBucket + "?autoCreateBucket=true&storageClient=#gcStorageClient")
                .process(exchange -> {
                    String documentID = exchange.getIn().getHeader(GoogleCloudStorageConstants.OBJECT_NAME, String.class);
                    exchange.getIn().setBody(documentID);
                });

        from("direct:gcstorage-get-file")
                .id("gcstorage-get-file")
                .setHeader(GoogleCloudStorageConstants.OPERATION, constant("getObject"))
                .setHeader(GoogleCloudStorageConstants.OBJECT_NAME, body())
                .choice()
                .when(header("shouldUnzip").isEqualTo(true))
                    .pollEnrich().simple("google-storage://" + gcStorageBucket + "?storageClient=#gcStorageClient&deleteAfterRead=false&serviceAccountKey=" + gcStorageServiceAccountId)
                    .setHeader(GoogleCloudStorageConstants.CONTENT_DISPOSITION, simple("$header.CamelAwsS3ContentDisposition"))
                    .setHeader(Exchange.CONTENT_TYPE, simple("$header.CamelAwsS3ContentType"))
                    .unmarshal(RouteUtils.getZipFileDataFormat())
                        .split(bodyAs(Iterator.class), (oldExchange, newExchange) -> newExchange)
                        .streaming()
                        .convertBodyTo(byte[].class)
                    .end()
                .endChoice()
                .otherwise()
                    .pollEnrich().simple("google-storage://" + gcStorageBucket + "?storageClient=#gcStorageClient&deleteAfterRead=false&serviceAccountKey=" + gcStorageServiceAccountId)
                    .setHeader(GoogleCloudStorageConstants.CONTENT_DISPOSITION, simple("$header.CamelAwsS3ContentDisposition"))
                    .setHeader(Exchange.CONTENT_TYPE, simple("$header.CamelAwsS3ContentType"))
                    .endChoice()
                .end();

        from("direct:gcstorage-get-file-link")
                .id("gcstorage-get-file-link")
                .setHeader(GoogleCloudStorageConstants.OPERATION, constant("createDownloadLink"))
                .setHeader(GoogleCloudStorageConstants.OBJECT_NAME, body())
                .setHeader(GoogleCloudStorageConstants.DOWNLOAD_LINK_EXPIRATION_TIME, constant(linkExpiration))
                .toD("google-storage://" + gcStorageBucket + "?storageClient=#gcStorageClient&serviceAccountKey=" + gcStorageServiceAccountId);

        from("direct:gcstorage-delete-file")
                .id("gcstorage-delete-file")
                .setHeader(GoogleCloudStorageConstants.OPERATION, constant("deleteObject"))
                .setHeader(GoogleCloudStorageConstants.OBJECT_NAME, body())
                .toD("google-storage://" + gcStorageBucket + "?storageClient=#gcStorageClient&serviceAccountKey=" + gcStorageServiceAccountId);
    }

}

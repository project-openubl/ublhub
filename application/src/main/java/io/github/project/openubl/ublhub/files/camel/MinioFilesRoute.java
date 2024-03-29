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

import com.github.f4b6a3.tsid.TsidFactory;
import io.github.project.openubl.ublhub.files.FilesManager;
import io.minio.MinioClient;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.minio.MinioConstants;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Optional;

@ApplicationScoped
public class MinioFilesRoute extends RouteBuilder {

    @Inject
    TsidFactory tsidFactory;

    @ConfigProperty(name = "openubl.storage.type")
    String storageType;

    @ConfigProperty(name = "openubl.storage.minio.bucket")
    String s3Bucket;

    @ConfigProperty(name = "openubl.storage.minio.access_key_id")
    String s3AccessKeyID;

    @ConfigProperty(name = "openubl.storage.minio.secret_access_key")
    String s3SecretAccessKey;

    @ConfigProperty(name = "openubl.storage.minio.host")
    Optional<String> s3Host;

    @Produces
    @Singleton
    @Named("minioClient")
    public MinioClient produceS3client() {
        return MinioClient.builder()
                .endpoint(s3Host.orElse(""))
                .credentials(s3AccessKeyID, s3SecretAccessKey)
                .build();
    }

    @Override
    public void configure() throws Exception {
        from("direct:minio-save-file")
                .id("minio-save-file")
                .precondition(String.valueOf(storageType.equalsIgnoreCase("minio")))
                .choice()
                    .when(header("shouldZipFile").isEqualTo(true))
                        .marshal().zipFile()
                    .endChoice()
                .end()
                .process(exchange -> {
                    String filename = FilesManager.generateZipFilename(tsidFactory, exchange);

                    exchange.getIn().setHeader(MinioConstants.OBJECT_NAME, filename);
                    exchange.getIn().setHeader(MinioConstants.DESTINATION_BUCKET_NAME, s3Bucket);
                })
                .toD("minio://" + s3Bucket + "?autoCreateBucket=true&deleteAfterWrite=true&minioClient=#minioClient")
                .process(exchange -> {
                    String documentID = exchange.getIn().getHeader(MinioConstants.OBJECT_NAME, String.class);
                    exchange.getIn().setBody(documentID);
                });

        from("direct:minio-get-file")
                .id("minio-get-file")
                .precondition(String.valueOf(storageType.equalsIgnoreCase("minio")))
                .choice()
                    .when(header("shouldUnzip").isEqualTo(true))
                        .setHeader(MinioConstants.OBJECT_NAME, simple("${body}"))
                        .toD("minio://" + s3Bucket + "?minioClient=#minioClient&operation=getObject")
                        .unmarshal(RouteUtils.getZipFileDataFormat())
                            .split(bodyAs(Iterator.class), (oldExchange, newExchange) -> newExchange)
                            .streaming()
                            .convertBodyTo(byte[].class)
                        .end()
                    .endChoice()
                    .otherwise()
                        .setHeader(MinioConstants.OBJECT_NAME, simple("${body}"))
                        .toD("minio://" + s3Bucket + "?minioClient=#minioClient&operation=getObject")
                    .endChoice()
                .end();

        from("direct:minio-get-file-link")
                .id("minio-get-file-link")
                .precondition(String.valueOf(storageType.equalsIgnoreCase("minio")))
                .log(LoggingLevel.WARN, "Minio does not support link generation.");;

        from("direct:minio-delete-file")
                .id("minio-delete-file")
                .precondition(String.valueOf(storageType.equalsIgnoreCase("minio")))
                .setHeader(MinioConstants.OBJECT_NAME, simple("${body}"))
                .toD("minio://" + s3Bucket + "?minioClient=#minioClient&operation=deleteObject");
    }

}

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
package io.github.project.openubl.xsender.files.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class S3FilesRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.storage.link-expiration", defaultValue = "5000")
    String linkExpiration;

    @ConfigProperty(name = "openubl.storage.s3.bucket")
    String s3Bucket;

    @ConfigProperty(name = "openubl.storage.s3.access_key_id")
    String s3AccessKeyID;

    @ConfigProperty(name = "openubl.storage.s3.secret_access_key")
    String s3SecretAccessKey;

    @ConfigProperty(name = "openubl.storage.s3.region")
    String s3Region;

    @ConfigProperty(name = "openubl.storage.s3.host")
    Optional<String> s3Host;

    @Override
    public void configure() throws Exception {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(s3AccessKeyID, s3SecretAccessKey);
        AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(awsCredentials);

        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(s3Region));

        S3Presigner.Builder s3PreSignerBuilder = S3Presigner.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(s3Region));

        S3Client s3Client;
        S3Presigner s3Presigner;
        if (s3Host.isPresent()) {
            s3Client = s3ClientBuilder
                    .endpointOverride(URI.create(s3Host.get()))
                    .build();
            s3Presigner = s3PreSignerBuilder
                    .endpointOverride(URI.create(s3Host.get()))
                    .build();
        } else {
            s3Client = s3ClientBuilder
                    .build();
            s3Presigner = s3PreSignerBuilder
                    .build();
        }

        bindToRegistry("s3client", s3Client);
        bindToRegistry("s3Presigner", s3Presigner);

        from("direct:s3-save-file")
                .id("s3-save-file")
                .choice()
                    .when(header("shouldZipFile").isEqualTo(true))
                        .marshal().zipFile()
                    .endChoice()
                .end()
                .process(exchange -> {
                    exchange.getIn().setHeader(AWS2S3Constants.KEY, UUID.randomUUID().toString());
                    exchange.getIn().setHeader(AWS2S3Constants.BUCKET_DESTINATION_NAME, s3Bucket);
                })
                .toD("aws2-s3://" + s3Bucket + "?autoCreateBucket=true&deleteAfterWrite=true&amazonS3Client=#s3client")
                .process(exchange -> {
                    String documentID = exchange.getIn().getHeader(AWS2S3Constants.KEY, String.class);
                    exchange.getIn().setBody(documentID);
                });

        from("direct:s3-get-file")
                .id("s3-get-file")
                .choice()
                    .when(header("shouldUnzip").isEqualTo(true))
                        .pollEnrich().simple("aws2-s3://" + s3Bucket + "?amazonS3Client=#s3client&deleteAfterRead=false&fileName=${body}")
                        .setHeader("Content-Disposition", simple("$header.CamelAwsS3ContentDisposition"))
                        .setHeader(Exchange.CONTENT_TYPE, simple("$header.CamelAwsS3ContentType"))
                        .unmarshal().zipFile()
                    .endChoice()
                    .otherwise()
                        .pollEnrich().simple("aws2-s3://" + s3Bucket + "?amazonS3Client=#s3client&deleteAfterRead=false&fileName=${body}")
                        .setHeader("Content-Disposition", simple("$header.CamelAwsS3ContentDisposition"))
                        .setHeader(Exchange.CONTENT_TYPE, simple("$header.CamelAwsS3ContentType"))
                    .endChoice()
                .end();

        from("direct:s3-get-file-link")
                .id("s3-get-file-link")
                .setHeader(AWS2S3Constants.KEY, simple("${body}"))
                .setHeader(AWS2S3Constants.DOWNLOAD_LINK_EXPIRATION_TIME, constant(linkExpiration))
                .toD("aws2-s3://" + s3Bucket + "?amazonS3Client=#s3client&amazonS3Presigner=#s3Presigner&operation=createDownloadLink");

        from("direct:s3-delete-file")
                .id("s3-delete-file")
                .setHeader(AWS2S3Constants.KEY, simple("${body}"))
                .toD("aws2-s3://" + s3Bucket + "?amazonS3Client=#s3client&operation=deleteObject");
    }

}

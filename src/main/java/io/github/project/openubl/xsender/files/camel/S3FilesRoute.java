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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
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
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKeyID, s3SecretAccessKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials));

        AmazonS3 amazonS3;
        if (s3Host.isPresent()) {
            amazonS3 = amazonS3ClientBuilder
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(s3Host.get(), s3Region)
                    )
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(clientConfiguration)
                    .build();
        } else {
            amazonS3 = amazonS3ClientBuilder
                    .withRegion(s3Region)
                    .build();
        }

        bindToRegistry("s3client", amazonS3);

        from("direct:s3-save-file")
                .id("s3-save-file")
                .choice()
                    .when(header("isZipFile").isEqualTo(false))
                        .marshal().zipFile()
                    .endChoice()
                .end()
                .convertBodyTo(byte[].class)
                .process(exchange -> {
                    byte[] body = exchange.getIn().getBody(byte[].class);
                    String filenameWithoutExtension = UUID.randomUUID().toString();

                    exchange.getIn().setHeader(Exchange.FILE_NAME, filenameWithoutExtension + ".zip");
                    exchange.getIn().setHeader(S3Constants.KEY, filenameWithoutExtension);
                    exchange.getIn().setHeader(S3Constants.BUCKET_DESTINATION_NAME, s3Bucket);
                    exchange.getIn().setHeader(S3Constants.CONTENT_LENGTH, body.length);
                    exchange.getIn().setHeader(S3Constants.CONTENT_TYPE, "application/zip");
                    exchange.getIn().setHeader(S3Constants.CONTENT_DISPOSITION, "attachment;filename=\"${header.CamelFileName}\"");
                })
                .toD("aws-s3:" + s3Bucket + "?deleteAfterWrite=true&amazonS3Client=#s3client")
                .process(exchange -> {
                    String documentID = exchange.getIn().getHeader(S3Constants.KEY, String.class);
                    exchange.getIn().setBody(documentID);
                });

        from("direct:s3-get-file")
                .id("s3-get-file")
                .choice()
                    .when(header("shouldUnzip").isEqualTo(true))
                        .pollEnrich().simple("aws-s3:"+ s3Bucket + "?amazonS3Client=#s3client&deleteAfterRead=false&fileName=${body}")
                        .setHeader("Content-Disposition", simple("$header.CamelAwsS3ContentDisposition"))
                        .setHeader(Exchange.CONTENT_TYPE, simple("$header.CamelAwsS3ContentType"))
                        .unmarshal().zipFile()
                    .endChoice()
                    .otherwise()
                        .pollEnrich().simple("aws-s3:"+ s3Bucket + "?amazonS3Client=#s3client&deleteAfterRead=false&fileName=${body}")
                        .setHeader("Content-Disposition", simple("$header.CamelAwsS3ContentDisposition"))
                        .setHeader(Exchange.CONTENT_TYPE, simple("$header.CamelAwsS3ContentType"))
                    .endChoice()
                .end();

        from("direct:s3-get-file-link")
                .id("s3-get-file-link")
                .setHeader(S3Constants.KEY, simple("${body}"))
                .setHeader(S3Constants.S3_OPERATION, constant("downloadLink"))
                .setHeader(S3Constants.DOWNLOAD_LINK_EXPIRATION, constant(linkExpiration))
                .toD("aws-s3:" + s3Bucket + "?amazonS3Client=#s3client")
                .process(exchange -> {
                    String downloadLink = exchange.getIn().getHeader(S3Constants.DOWNLOAD_LINK, String.class);
                    exchange.getIn().setBody(downloadLink);
                });

        from("direct:s3-delete-file")
                .id("s3-delete-file")
                .toD("aws-s3:"+ s3Bucket + "?amazonS3Client=#s3client&operation=deleteObject");
    }

}

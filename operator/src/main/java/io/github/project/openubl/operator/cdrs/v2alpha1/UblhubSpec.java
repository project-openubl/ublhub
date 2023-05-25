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
package io.github.project.openubl.operator.cdrs.v2alpha1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.github.project.openubl.operator.ValueOrSecret;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UblhubSpec {

    @JsonPropertyDescription("Number of instances. Default is 1.")
    private int instances = 1;

    @JsonPropertyDescription("Custom image to be used.")
    private String image;

    @JsonPropertyDescription("Secret(s) that might be used when pulling an image from a private container image registry or repository.")
    private List<LocalObjectReference> imagePullSecrets;

    @JsonPropertyDescription("Configuration of the server.\n" +
            "expressed as a keys and values that can be either direct values or references to secrets.")
    private List<ValueOrSecret> additionalOptions;

    @JsonProperty("http")
    @JsonPropertyDescription("In this section you can configure features related to HTTP and HTTPS")
    private HttpSpec httpSpec;

    @JsonProperty("ingress")
    @JsonPropertyDescription("The deployment is, by default, exposed through a basic ingress.\n" +
            "You can change this behaviour by setting the enabled property to false.")
    private IngressSpec ingressSpec;

    @JsonProperty("db")
    @JsonPropertyDescription("In this section you can find all properties related to connect to a database.")
    private DatabaseSpec databaseSpec;

    @JsonProperty("hostname")
    @JsonPropertyDescription("In this section you can configure hostname and related properties.")
    private HostnameSpec hostnameSpec;

    @JsonProperty("auth")
    @JsonPropertyDescription("In this section you can configure Oidc settings.")
    private AuthSpec authSpec;

    @JsonProperty("storage")
    @JsonPropertyDescription("In this section you can configure the Storage.")
    private StorageSpec storageSpec;

    @JsonProperty("xbuilder")
    @JsonPropertyDescription("XBuilder settings.")
    private XBuilderSpec xBuilderSpec;

    @JsonProperty("xsender")
    @JsonPropertyDescription("XSender settings.")
    private XSenderSpec xSenderSpec;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HttpSpec {
        @JsonPropertyDescription("A secret containing the TLS configuration for HTTPS. Reference: https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets.")
        private String tlsSecret;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IngressSpec {
        @JsonProperty("enabled")
        private boolean enabled = true;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DatabaseSpec {
        @JsonPropertyDescription("The reference to a secret holding the username of the database user.")
        private SecretKeySelector usernameSecret;

        @JsonPropertyDescription("The reference to a secret holding the password of the database user.")
        private SecretKeySelector passwordSecret;

        @JsonPropertyDescription("The full database JDBC URL. For instance, 'jdbc:postgresql://localhost/ublhub'.")
        private String url;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HostnameSpec {
        @JsonPropertyDescription("Hostname for the server.")
        private String hostname;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthSpec {
        @JsonPropertyDescription("Enable Oidc Auth.")
        private boolean enabled;

        @JsonPropertyDescription("Oidc server url.")
        private String serverUrl;

        @JsonPropertyDescription("Oidc client id.")
        private String clientId;

        @JsonPropertyDescription("Oidc client id.")
        private SecretKeySelector credentialsSecret;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StorageSpec {
        public enum Type {
            filesystem,
            s3
        }

        @JsonPropertyDescription("Typo of chosen storage.")
        private Type type;

        @JsonProperty("filesystem")
        @JsonPropertyDescription("Filesystem settings.")
        private StorageFilesystemSpec filesystemSpec;

        @JsonProperty("s3")
        @JsonPropertyDescription("Filesystem settings.")
        private StorageS3Spec s3Spec;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StorageFilesystemSpec {
        @JsonPropertyDescription("Size of the PVC to create.")
        private String size;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StorageS3Spec {
        @JsonPropertyDescription("Only if you are using Minio, otherwise leave it empty")
        private String host;

        @JsonPropertyDescription("Only if you are using Minio, otherwise leave it empty")
        private String healthUrl;

        @JsonPropertyDescription("Region")
        private String region;

        @JsonPropertyDescription("Bucket")
        private String bucket;

        @JsonPropertyDescription("Access key id")
        private String accessKeyId;

        @JsonPropertyDescription("Secret access key")
        private String secretAccessKey;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class XBuilderSpec {
        @JsonPropertyDescription("Default currency")
        private String moneda = "PEN";

        @JsonPropertyDescription("Default IGV")
        private String igvTasa;

        @JsonPropertyDescription("Default ICB")
        private String icbTasa;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class XSenderSpec {
        @JsonPropertyDescription("Enable logging feature")
        private Boolean enableLoggingFeature = false;
    }
}

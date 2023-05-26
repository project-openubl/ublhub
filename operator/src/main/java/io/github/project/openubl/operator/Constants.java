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
package io.github.project.openubl.operator;

import io.github.project.openubl.operator.cdrs.v2alpha1.UblhubSpec;

import java.util.Map;

public class Constants {
    public static final String CRDS_GROUP = "ublhub.openubl.io";
    public static final String CRDS_VERSION = "v1alpha1";

    public static final String CONTEXT_LABELS_KEY = "labels";
    public static final String CONTEXT_CONFIG_KEY = "config";
    public static final String CONTEXT_K8S_CLIENT_KEY = "k8sClient";

    public static final String UBLHUB_NAME = "ublhub";

    public static final String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
    public static final String MANAGED_BY_VALUE = "ublhub-operator";

    public static final Map<String, String> DEFAULT_LABELS = Map.of(
            MANAGED_BY_LABEL, MANAGED_BY_VALUE
    );

    public static final Map<String, String> DEFAULT_DIST_CONFIG = Map.of();

    public static final Integer HTTP_PORT = 8080;
    public static final Integer HTTPS_PORT = 8443;
    public static final String SERVICE_PROTOCOL = "TCP";
    public static final String SERVICE_SUFFIX = "-" + UBLHUB_NAME + "-service";
    public static final String INGRESS_SUFFIX = "-" + UBLHUB_NAME + "-ingress";
    public static final String SECRET_SUFFIX = "-" + UBLHUB_NAME + "-secret";
    public static final String DEPLOYMENT_SUFFIX = "-" + UBLHUB_NAME + "-deployment";
    public static final String PVC_SUFFIX = "-" + UBLHUB_NAME + "-pvc";

    public static final String BASIC_AUTH_SECRET_SUFFIX = "-basic-auth";

    public static final String BASIC_AUTH_SECRET_ENCRYPTIONKEY = "encryption-key";


    public static final String PROFILE_PROD = "prod";


    public static final String CERTIFICATES_FOLDER = "/mnt/certificates";
    public static final String STORAGE_FOLDER = "/mnt/ublhub-storage";

    public static final String STORAGE_MIN_SIZE = "100Mi";

    public static final UblhubSpec.XBuilderSpec defaultXBuilderConfig = UblhubSpec.XBuilderSpec.builder()
            .moneda("PEN")
            .igvTasa("0.18")
            .icbTasa("0.4")
            .build();
    public static final UblhubSpec.XSenderSpec defaultXSenderConfig = UblhubSpec.XSenderSpec.builder()
            .enableLoggingFeature(false)
            .build();
}

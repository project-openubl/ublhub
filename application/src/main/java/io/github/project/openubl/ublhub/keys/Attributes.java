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
package io.github.project.openubl.ublhub.keys;

import io.github.project.openubl.ublhub.keys.provider.ProviderConfigProperty;
import org.keycloak.crypto.Algorithm;

import java.util.Arrays;

public interface Attributes {

    String PRIORITY_KEY = "priority";
    ProviderConfigProperty PRIORITY_PROPERTY = ProviderConfigProperty.builder()
            .name(PRIORITY_KEY)
            .label("Priority")
            .helpText("Priority for the provider")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("0")
            .build();

    String ENABLED_KEY = "enabled";
    ProviderConfigProperty ENABLED_PROPERTY = ProviderConfigProperty.builder()
            .name(ENABLED_KEY)
            .label("Enabled")
            .helpText("Set if the keys are enabled")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue("true")
            .build();

    String ACTIVE_KEY = "active";
    ProviderConfigProperty ACTIVE_PROPERTY = ProviderConfigProperty.builder()
            .name(ACTIVE_KEY)
            .label("Active")
            .helpText("Set if the keys can be used for signing")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue("true")
            .build();

    String PRIVATE_KEY_KEY = "privateKey";
    ProviderConfigProperty PRIVATE_KEY_PROPERTY = ProviderConfigProperty.builder()
            .name(PRIVATE_KEY_KEY)
            .label("Private RSA Key")
            .helpText("Private RSA Key encoded in PEM format")
            .type(ProviderConfigProperty.FILE_TYPE)
            .defaultValue(null)
            .secret(true)
            .build();

    String CERTIFICATE_KEY = "certificate";
    ProviderConfigProperty CERTIFICATE_PROPERTY = ProviderConfigProperty.builder()
            .name(CERTIFICATE_KEY)
            .label("X509 Certificate")
            .helpText("X509 Certificate encoded in PEM format")
            .type(ProviderConfigProperty.FILE_TYPE)
            .defaultValue(null)
            .build();

    String KEY_SIZE_KEY = "keySize";
    ProviderConfigProperty KEY_SIZE_PROPERTY = ProviderConfigProperty.builder()
            .name(KEY_SIZE_KEY)
            .label("Key size")
            .helpText("Size for the generated keys")
            .type(ProviderConfigProperty.LIST_TYPE)
            .defaultValue("2048")
            .options(Arrays.asList("1024", "2048", "4096"))
            .build();

    String KID_KEY = "kid";
    String SECRET_KEY = "secret";

    String SECRET_SIZE_KEY = "secretSize";
    ProviderConfigProperty SECRET_SIZE_PROPERTY = ProviderConfigProperty.builder()
            .name(SECRET_SIZE_KEY)
            .label("Secret size")
            .helpText("Size in bytes for the generated secret")
            .type(ProviderConfigProperty.LIST_TYPE)
            .defaultValue("32")
            .options(Arrays.asList("32", "64", "128", "256", "512"))
            .build();

    String ALGORITHM_KEY = "algorithm";

    ProviderConfigProperty RS_ALGORITHM_PROPERTY = ProviderConfigProperty.builder()
            .name(ALGORITHM_KEY)
            .label("Algorithm")
            .helpText("Intended algorithm for the key")
            .type(ProviderConfigProperty.LIST_TYPE)
            .defaultValue(Algorithm.RS256)
            .options(Arrays.asList(Algorithm.RS256, Algorithm.RS384, Algorithm.RS512, Algorithm.PS256, Algorithm.PS384, Algorithm.PS512))
            .build();

    ProviderConfigProperty HS_ALGORITHM_PROPERTY = ProviderConfigProperty.builder()
            .name(ALGORITHM_KEY)
            .label("Algorithm")
            .helpText("Intended algorithm for the key")
            .type(ProviderConfigProperty.LIST_TYPE)
            .defaultValue(Algorithm.HS256)
            .options(Arrays.asList(Algorithm.HS256, Algorithm.HS384, Algorithm.HS512))
            .build();
}

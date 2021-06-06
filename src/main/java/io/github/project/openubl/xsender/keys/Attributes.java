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
package io.github.project.openubl.xsender.keys;

import io.github.project.openubl.xsender.keys.provider.ProviderConfigProperty;
import org.keycloak.crypto.Algorithm;

public interface Attributes {

    String PRIORITY_KEY = "priority";
    ProviderConfigProperty PRIORITY_PROPERTY = new ProviderConfigProperty(PRIORITY_KEY, "Priority", "Priority for the provider", ProviderConfigProperty.STRING_TYPE, "0");

    String ENABLED_KEY = "enabled";
    ProviderConfigProperty ENABLED_PROPERTY = new ProviderConfigProperty(ENABLED_KEY, "Enabled", "Set if the keys are enabled", ProviderConfigProperty.BOOLEAN_TYPE, "true");

    String ACTIVE_KEY = "active";
    ProviderConfigProperty ACTIVE_PROPERTY = new ProviderConfigProperty(ACTIVE_KEY, "Active", "Set if the keys can be used for signing", ProviderConfigProperty.BOOLEAN_TYPE, "true");

    String PRIVATE_KEY_KEY = "privateKey";
    ProviderConfigProperty PRIVATE_KEY_PROPERTY = new ProviderConfigProperty(PRIVATE_KEY_KEY, "Private RSA Key", "Private RSA Key encoded in PEM format", ProviderConfigProperty.FILE_TYPE, null, true);

    String CERTIFICATE_KEY = "certificate";
    ProviderConfigProperty CERTIFICATE_PROPERTY = new ProviderConfigProperty(CERTIFICATE_KEY, "X509 Certificate", "X509 Certificate encoded in PEM format", ProviderConfigProperty.FILE_TYPE, null);

    String KEY_SIZE_KEY = "keySize";
    ProviderConfigProperty KEY_SIZE_PROPERTY = new ProviderConfigProperty(KEY_SIZE_KEY, "Key size", "Size for the generated keys", ProviderConfigProperty.LIST_TYPE, "2048", "1024", "2048", "4096");

    String KID_KEY = "kid";

    String SECRET_KEY = "secret";

    String SECRET_SIZE_KEY = "secretSize";
    ProviderConfigProperty SECRET_SIZE_PROPERTY = new ProviderConfigProperty(SECRET_SIZE_KEY, "Secret size", "Size in bytes for the generated secret", ProviderConfigProperty.LIST_TYPE, "32", "32", "64", "128", "256", "512");

    String ALGORITHM_KEY = "algorithm";

    ProviderConfigProperty RS_ALGORITHM_PROPERTY = new ProviderConfigProperty(ALGORITHM_KEY, "Algorithm", "Intended algorithm for the key", ProviderConfigProperty.LIST_TYPE,
            Algorithm.RS256,
            Algorithm.RS256, Algorithm.RS384, Algorithm.RS512, Algorithm.PS256, Algorithm.PS384, Algorithm.PS512);

    ProviderConfigProperty HS_ALGORITHM_PROPERTY = new ProviderConfigProperty(ALGORITHM_KEY, "Algorithm", "Intended algorithm for the key", ProviderConfigProperty.LIST_TYPE,
            Algorithm.HS256,
            Algorithm.HS256, Algorithm.HS384, Algorithm.HS512);
}

/**
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
package io.github.project.openubl.xsender.keys.qualifiers;

import io.github.project.openubl.xsender.keys.GeneratedRsaKeyProviderFactory;
import io.github.project.openubl.xsender.keys.ImportedRsaKeyProviderFactory;
import io.github.project.openubl.xsender.keys.JavaKeystoreKeyProviderFactory;

import java.util.Optional;
import java.util.stream.Stream;

public enum RsaKeyType {

    /**
     * rsa-generated
     */
    GENERATED(GeneratedRsaKeyProviderFactory.ID),

    /**
     * java-keystore
     */
    JAVA_KEYSTORE(JavaKeystoreKeyProviderFactory.ID),

    /**
     * rsa
     */
    IMPORTED(ImportedRsaKeyProviderFactory.ID);

    private final String providerId;

    RsaKeyType(String providerId) {
        this.providerId = providerId;
    }

    public static Optional<RsaKeyType> findByProviderId(String providerId) {
        return Stream.of(RsaKeyType.values())
                .filter(p -> p.getProviderId().equals(providerId))
                .findFirst();
    }

    public String getProviderId() {
        return providerId;
    }
}

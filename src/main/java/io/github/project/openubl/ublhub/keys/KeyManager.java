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

import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.smallrye.mutiny.Uni;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

public interface KeyManager {

    Uni<KeyWrapper> getActiveKey(ProjectEntity namespace, KeyUse use, String algorithm);

    Uni<KeyWrapper> getKey(ProjectEntity namespace, String kid, KeyUse use, String algorithm);

    Uni<List<KeyWrapper>> getKeys(ProjectEntity namespace);

    Uni<List<KeyWrapper>> getKeys(ProjectEntity namespace, KeyUse use, String algorithm);

    class ActiveRsaKey {
        private final String kid;
        private final PrivateKey privateKey;
        private final PublicKey publicKey;
        private final X509Certificate certificate;

        public ActiveRsaKey(String kid, PrivateKey privateKey, PublicKey publicKey, X509Certificate certificate) {
            this.kid = kid;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.certificate = certificate;
        }

        public String getKid() {
            return kid;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }
    }

}

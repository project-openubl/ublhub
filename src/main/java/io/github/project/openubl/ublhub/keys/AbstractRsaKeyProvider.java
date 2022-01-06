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

import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.crypto.*;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRsaKeyProvider implements KeyProvider {

    private final KeyStatus status;

    private final ComponentModel model;

    private final KeyWrapper key;

    private final String algorithm;

    public AbstractRsaKeyProvider(NamespaceEntity namespace, ComponentModel model) {
        this.model = model;
        this.status = KeyStatus.from(model.get(Attributes.ACTIVE_KEY, true), model.get(Attributes.ENABLED_KEY, true));
        this.algorithm = model.get(Attributes.ALGORITHM_KEY, Algorithm.RS256);

        if (model.hasNote(KeyWrapper.class.getName())) {
            key = model.getNote(KeyWrapper.class.getName());
        } else {
            key = loadKey(namespace, model);
            model.setNote(KeyWrapper.class.getName(), key);
        }
    }

    protected abstract KeyWrapper loadKey(NamespaceEntity namespace, ComponentModel model);

    @Override
    public List<KeyWrapper> getKeys() {
        return Collections.singletonList(key);
    }

    protected KeyWrapper createKeyWrapper(KeyPair keyPair, X509Certificate certificate) {
        KeyWrapper key = new KeyWrapper();

        key.setProviderId(model.getId());
        key.setProviderPriority(model.get("priority", 0L));

        key.setKid(KeyUtils.createKeyId(keyPair.getPublic()));
        key.setUse(KeyUse.SIG);
        key.setType(KeyType.RSA);
        key.setAlgorithm(algorithm);
        key.setStatus(status);
        key.setPrivateKey(keyPair.getPrivate());
        key.setPublicKey(keyPair.getPublic());
        key.setCertificate(certificate);

        return key;
    }

}

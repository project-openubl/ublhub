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

import io.github.project.openubl.ublhub.keys.component.ComponentFactory;
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.smallrye.mutiny.Uni;
import org.keycloak.crypto.KeyUse;

public interface KeyProviderFactory<T extends KeyProvider> extends ComponentFactory<T, KeyProvider> {

    T create(NamespaceEntity namespace, ComponentModel model);

    default Uni<Boolean> createFallbackKeys(NamespaceEntity namespace, KeyUse keyUse, String algorithm) {
        return Uni.createFrom().item(false);
    }
}

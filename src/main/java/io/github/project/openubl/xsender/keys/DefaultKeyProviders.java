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
package io.github.project.openubl.xsender.keys;

import io.github.project.openubl.xsender.keys.component.ComponentModel;
import org.keycloak.common.util.MultivaluedHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
@ApplicationScoped
public class DefaultKeyProviders {

    @Inject
    ComponentProvider componentProvider;

    public void createProviders(String entityId) {
        if (!hasProvider(entityId, "rsa-generated")) {
            ComponentModel generated = new ComponentModel();
            generated.setName("rsa-generated");
            generated.setParentId(entityId);
            generated.setProviderId("rsa-generated");
            generated.setProviderType(KeyProvider.class.getName());

            MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
            config.putSingle("priority", "100");
            generated.setConfig(config);
            componentProvider.addComponentModel(entityId, generated);
        }
    }

    protected boolean hasProvider(String entityId, String providerId) {
        List<ComponentModel> currentComponents = componentProvider.getComponents(entityId, KeyProvider.class.getName());
        for (ComponentModel current : currentComponents) {
            if (current.getProviderId().equals(providerId)) {
                return true;
            }
        }
        return false;
    }
}

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
package io.github.project.openubl.xsender.keys.component.utils;

import io.github.project.openubl.xsender.keys.KeyProviderFactory;
import io.github.project.openubl.xsender.keys.component.ComponentFactory;
import io.github.project.openubl.xsender.keys.qualifiers.RsaKeyType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Optional;

@ApplicationScoped
public class DefaultComponentUtil implements ComponentUtil {

    @Inject
    @Any
    Instance<KeyProviderFactory> componentFactories;

    @Override
    public ComponentFactory getComponentFactory(String providerType, String providerId) {
        try {
            Class<?> aClass = Class.forName(providerType);

            Optional<RsaKeyType> op = RsaKeyType.findByProviderId(providerId);
            if (!op.isPresent()) {
                return null;
            }

            Annotation componentProviderLiteral = new ComponentProviderLiteral(aClass);
            Annotation rsaKeyProviderLiteral = new RsaKeyProviderLiteral(op.get());

            return componentFactories.select(KeyProviderFactory.class, componentProviderLiteral, rsaKeyProviderLiteral).get();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Invalid factory", e);
        }
    }

}

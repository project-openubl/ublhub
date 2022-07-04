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
package io.github.project.openubl.ublhub.keys.component;

import io.github.project.openubl.ublhub.keys.provider.ConfiguredProvider;
import io.github.project.openubl.ublhub.keys.provider.ProviderConfigProperty;
import io.github.project.openubl.ublhub.keys.provider.ProviderFactory;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ComponentFactory<CreatedType, ProviderType> extends ProviderFactory<ProviderType>, ConfiguredProvider {

    CreatedType create(ProjectEntity project, ComponentModel model);

    @Override
    default ProviderType create() {
        return null;
    }

    default void validateConfiguration(ProjectEntity project, ComponentModel model) throws ComponentValidationException {
    }

    default void onCreate(ProjectEntity project, ComponentModel model) {
    }


    default void onUpdate(ProjectEntity project, ComponentModel model) {
    }

    default void preRemove(ProjectEntity project, ComponentModel model) {
    }

    /**
     * These are config properties that are common across all implementation of this component type
     *
     * @return
     */
    default List<ProviderConfigProperty> getCommonProviderConfigProperties() {
        return Collections.emptyList();
    }

    /**
     * This is metadata about this component type.  Its really configuration information about the component type and not
     * an individual instance
     *
     * @return
     */
    default Map<String, Object> getTypeMetadata() {
        return Collections.emptyMap();
    }


}

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
package io.github.project.openubl.xsender.models.jpa;

import io.github.project.openubl.xsender.keys.component.ComponentModel;
import io.github.project.openubl.xsender.keys.component.utils.ComponentUtil;
import io.github.project.openubl.xsender.models.jpa.entities.ComponentConfigEntity;
import io.github.project.openubl.xsender.models.jpa.entities.ComponentEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import org.keycloak.common.util.MultivaluedHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ComponentRepository implements PanacheRepositoryBase<ComponentEntity, String> {

    @Inject
    ComponentUtil componentUtil;

    public Uni<ComponentModel> addComponentModel(NamespaceEntity namespace, ComponentModel model) {
        return importComponentModel(namespace, model)
                .invoke(componentModel -> componentUtil.notifyCreated(namespace, model));
    }

    public Uni<ComponentModel> importComponentModel(NamespaceEntity namespace, ComponentModel model) {
        return Uni.createFrom().item(() -> componentUtil.getComponentFactory(model))
                .onItem().ifNull().failWith(() -> new IllegalArgumentException("Invalid component type"))
                .onItem().ifNotNull().invoke(componentFactory -> componentFactory.validateConfiguration(namespace, model))
                .chain(componentFactory -> {
                    ComponentEntity c = new ComponentEntity();
                    if (model.getId() == null) {
                        c.id = UUID.randomUUID().toString();
                    } else {
                        c.id = model.getId();
                    }
                    c.name = model.getName();
                    c.parentId = model.getParentId();
                    if (model.getParentId() == null) {
                        c.parentId = namespace.id;
                        model.setParentId(namespace.id);
                    }
                    c.providerType = model.getProviderType();
                    c.providerId = model.getProviderId();
                    c.subType = model.getSubType();
                    c.namespace = namespace;

                    return c.<ComponentEntity>persist();
                })
                .map(c -> {
                    setConfig(model, c);
                    model.setId(c.id);
                    return model;
                });
    }

    protected void setConfig(ComponentModel model, ComponentEntity c) {
        c.componentConfigs.clear();
        for (String key : model.getConfig().keySet()) {
            List<String> vals = model.getConfig().get(key);
            if (vals == null) {
                continue;
            }
            for (String val : vals) {
                ComponentConfigEntity config = new ComponentConfigEntity();
                config.id = UUID.randomUUID().toString();
                config.name = key;
                config.value = val;
                config.component = c;
                c.componentConfigs.add(config);
            }
        }
    }

    public Uni<Void> updateComponent(NamespaceEntity namespace, ComponentModel component) {
        return Uni.createFrom()
                .<Void>emitter(uniEmitter -> {
                    try {
                        componentUtil.getComponentFactory(component).validateConfiguration(namespace, component);
                        uniEmitter.complete(null);
                    } catch (Throwable e) {
                        uniEmitter.fail(e);
                    }
                })
                .chain(() -> ComponentEntity.<ComponentEntity>findById(component.getId()))
                .chain(c -> {
                    c.name = component.getName();
                    c.providerId = component.getProviderId();
                    c.providerType = component.getProviderType();
                    c.parentId = component.getParentId();
                    c.subType = component.getSubType();
                    setConfig(component, c);
                    return c.<ComponentEntity>persist();
                })
                .invoke(c -> componentUtil.notifyUpdated(namespace, component))
                .chain(() -> Uni.createFrom().voidItem());
    }

    public Uni<Boolean> removeComponent(ComponentModel component) {
        return removeComponents(component.getId())
                .chain(() -> ComponentEntity.deleteById(component.getId()));
    }

    public Uni<Long> removeComponents(String parentId) {
        return ComponentEntity.delete("parentId = :parentId", Parameters.with("parentId", parentId));
    }

    public Uni<List<ComponentModel>> getComponents(NamespaceEntity namespace) {
        return ComponentEntity
                .find("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.componentConfigs WHERE c.namespace.id = :namespaceId",
                        Parameters.with("namespaceId", namespace.id)
                )
                .<ComponentEntity>list()
                .map(entities -> entities.stream()
                        .map(this::entityToModel)
                        .collect(Collectors.toList())
                );
    }

    public Uni<List<ComponentModel>> getComponents(String parentId, final String providerType) {
        return ComponentEntity
                .find("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.componentConfigs WHERE c.parentId = :parentId AND c.providerType = :providerType",
                        Parameters.with("parentId", parentId).and("providerType", providerType)
                )
                .<ComponentEntity>list()
                .map(entities -> entities.stream()
                        .map(this::entityToModel)
                        .collect(Collectors.toList())
                );
    }

    public Uni<List<ComponentModel>> getComponents(NamespaceEntity namespace, String parentId) {
        return ComponentEntity
                .find("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.componentConfigs WHERE c.namespace.id = :namespaceId and c.parentId = :parentId",
                        Parameters.with("namespaceId", namespace.id).and("parentId", parentId)
                ).<ComponentEntity>list()
                .map(entities -> entities.stream()
                        .map(this::entityToModel)
                        .collect(Collectors.toList())
                );
    }

    public Uni<ComponentModel> getComponent(String id) {
        return ComponentEntity.<ComponentEntity>findById(id)
                .map(entity -> entity != null ? entityToModel(entity) : null);
    }

    private ComponentModel entityToModel(ComponentEntity c) {
        ComponentModel model = new ComponentModel();
        model.setId(c.id);
        model.setName(c.name);
        model.setProviderType(c.providerType);
        model.setProviderId(c.providerId);
        model.setSubType(c.subType);
        model.setParentId(c.parentId);
        MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
        for (ComponentConfigEntity configEntity : c.componentConfigs) {
            config.add(configEntity.name, configEntity.value);
        }
        model.setConfig(config);
        return model;
    }

}

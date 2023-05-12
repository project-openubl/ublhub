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
package io.github.project.openubl.ublhub.models.jpa;

import com.github.f4b6a3.tsid.TsidFactory;
import io.github.project.openubl.ublhub.keys.component.ComponentFactory;
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.keys.component.utils.ComponentUtil;
import io.github.project.openubl.ublhub.models.jpa.entities.ComponentConfigEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ComponentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.keycloak.common.util.MultivaluedHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
public class ComponentRepository implements PanacheRepositoryBase<ComponentEntity, String> {

    @Inject
    ComponentUtil componentUtil;

    @Inject
    TsidFactory tsidFactory;

    public ComponentModel addComponentModel(ComponentOwner owner, ComponentModel model) {
        return importComponentModel(owner, model);
    }

    public ComponentModel importComponentModel(ComponentOwner owner, ComponentModel model) {
        ComponentFactory componentFactory = componentUtil.getComponentFactory(model);
        if (componentFactory == null) {
            throw new IllegalArgumentException("Invalid component type");
        }

        componentFactory.validateConfiguration(owner, model);

        ComponentEntity c = new ComponentEntity();
        if (model.getId() == null) {
            c.setId(UUID.randomUUID().toString());
        } else {
            c.setId(model.getId());
        }
        c.setName(model.getName());
        c.setParentId(model.getParentId());
        if (model.getParentId() == null) {
            c.setParentId(owner.getProject());
            model.setParentId(owner.getProject());
        }
        c.setProviderType(model.getProviderType());
        c.setProviderId(model.getProviderId());
        c.setSubType(model.getSubType());

        c.setProject(owner.getProject());
        c.setRuc(owner.getRuc());

        c.persist();

        setConfig(model, c);
        model.setId(c.getId());
        return model;
    }

    protected void setConfig(ComponentModel model, ComponentEntity c) {
        c.getComponentConfigs().clear();
        for (String key : model.getConfig().keySet()) {
            List<String> vals = model.getConfig().get(key);
            if (vals == null) {
                continue;
            }
            for (String val : vals) {
                ComponentConfigEntity config = new ComponentConfigEntity();
                config.setId(tsidFactory.create().toLong());
                config.setName(key);
                config.setValue(val);
                config.setComponent(c);
                c.getComponentConfigs().add(config);
            }
        }
    }

    public void updateComponent(ComponentOwner owner, ComponentModel component) {
        componentUtil.getComponentFactory(component).validateConfiguration(owner, component);

        ComponentEntity c = ComponentEntity.<ComponentEntity>findById(component.getId());

        c.setName(component.getName());
        c.setProviderId(component.getProviderId());
        c.setProviderType(component.getProviderType());
        c.setParentId(component.getParentId());
        c.setSubType(component.getSubType());
        setConfig(component, c);
        c.persist();
    }

    public boolean removeComponent(ComponentOwner owner, ComponentModel component) {
        removeComponents(owner, component.getId());
        return ComponentEntity.deleteById(component.getId());
    }

    public long removeComponents(ComponentOwner owner, String parentId) {
        String query = new StringBuilder("project = :project")
                .append(owner.getRuc() != null ? " and ruc = :ruc" : "")
                .append(" and parentId = :parentId")
                .toString();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("project", owner.getProject());
        if (owner.getRuc() != null) {
            parameters.put("ruc", owner.getRuc());
        }
        parameters.put("parentId", parentId);

        return ComponentEntity.delete(query, parameters);
    }

    public List<ComponentModel> getComponents(ComponentOwner owner) {
        String query = new StringBuilder("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.componentConfigs")
                .append(" WHERE c.project = :project")
                .append(owner.getRuc() != null ? " and c.ruc = :ruc" : "")
                .toString();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("project", owner.getProject());
        if (owner.getRuc() != null) {
            parameters.put("ruc", owner.getRuc());
        }

        return ComponentEntity
                .find(query, parameters)
                .<ComponentEntity>list()
                .stream()
                .map(this::entityToModel)
                .collect(Collectors.toList());
    }

    public List<ComponentModel> getComponents(ComponentOwner owner, String parentId) {
        String query = new StringBuilder("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.componentConfigs")
                .append(" WHERE c.project = :project")
                .append(owner.getRuc() != null ? " and c.ruc = :ruc" : "")
                .append(" and c.parentId = :parentId")
                .toString();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("project", owner.getProject());
        if (owner.getRuc() != null) {
            parameters.put("ruc", owner.getRuc());
        }
        parameters.put("parentId", parentId);

        return ComponentEntity
                .find(query, parameters)
                .<ComponentEntity>list()
                .stream()
                .map(this::entityToModel)
                .collect(Collectors.toList());
    }

    public List<ComponentModel> getComponents(ComponentOwner owner, String parentId, String providerType) {
        String query = new StringBuilder("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.componentConfigs")
                .append(" WHERE c.project = :project")
                .append(owner.getRuc() != null ? " and c.ruc = :ruc" : "")
                .append(" and c.parentId = :parentId")
                .append(" and c.providerType = :providerType")
                .toString();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("project", owner.getProject());
        if (owner.getRuc() != null) {
            parameters.put("ruc", owner.getRuc());
        }
        parameters.put("parentId", parentId);
        parameters.put("providerType", providerType);

        return ComponentEntity
                .find(query, parameters)
                .<ComponentEntity>list()
                .stream()
                .map(this::entityToModel)
                .collect(Collectors.toList());
    }

    public ComponentModel getComponent(ComponentOwner owner, String id) {
        String query = new StringBuilder("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.componentConfigs")
                .append(" WHERE c.project = :project")
                .append(owner.getRuc() != null ? " and c.ruc = :ruc" : "")
                .append(" and c.id = :id")
                .toString();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("project", owner.getProject());
        if (owner.getRuc() != null) {
            parameters.put("ruc", owner.getRuc());
        }
        parameters.put("id", id);

        return ComponentEntity
                .<ComponentEntity>find(query, parameters)
                .firstResultOptional()
                .map(this::entityToModel)
                .orElse(null);
    }

    private ComponentModel entityToModel(ComponentEntity c) {
        ComponentModel model = new ComponentModel();
        model.setId(c.getId());
        model.setName(c.getName());
        model.setProviderType(c.getProviderType());
        model.setProviderId(c.getProviderId());
        model.setSubType(c.getSubType());
        model.setParentId(c.getParentId());
        MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
        for (ComponentConfigEntity configEntity : c.getComponentConfigs()) {
            config.add(configEntity.getName(), configEntity.getValue());
        }
        model.setConfig(config);
        return model;
    }

}

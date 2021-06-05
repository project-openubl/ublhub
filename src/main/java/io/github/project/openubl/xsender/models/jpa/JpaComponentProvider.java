/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * <p>
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.eclipse.org/legal/epl-2.0/
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.models.jpa;

import io.github.project.openubl.xsender.keys.ComponentProvider;
import io.github.project.openubl.xsender.keys.component.ComponentFactory;
import io.github.project.openubl.xsender.keys.component.ComponentModel;
import io.github.project.openubl.xsender.keys.component.utils.ComponentUtil;
import io.github.project.openubl.xsender.models.jpa.entities.ComponentConfigEntity;
import io.github.project.openubl.xsender.models.jpa.entities.ComponentEntity;
import org.keycloak.common.util.MultivaluedHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
public class JpaComponentProvider implements ComponentProvider {

    /**
     * This just exists for testing purposes
     */
    public static final String COMPONENT_PROVIDER_EXISTS_DISABLED = "component.provider.exists.disabled";

    @Inject
    EntityManager em;

    @Inject
    ComponentUtil componentUtil;

    @Override
    public ComponentModel addComponentModel(String entityId, ComponentModel model) {
        model = importComponentModel(entityId, model);
        componentUtil.notifyCreated(entityId, model);

        return model;
    }

    @Override
    public ComponentModel importComponentModel(String entityId, ComponentModel model) {
        ComponentFactory componentFactory;
        try {
            componentFactory = componentUtil.getComponentFactory(model);
            if (componentFactory == null && System.getProperty(COMPONENT_PROVIDER_EXISTS_DISABLED) == null) {
                throw new IllegalArgumentException("Invalid component type");
            }
            if (componentFactory != null) {
                componentFactory.validateConfiguration(entityId, model);
            }
        } catch (Exception e) {
            if (System.getProperty(COMPONENT_PROVIDER_EXISTS_DISABLED) == null) {
                throw e;
            }
        }

        ComponentEntity c = new ComponentEntity();
        if (model.getId() == null) {
            c.setId(UUID.randomUUID().toString());
        } else {
            c.setId(model.getId());
        }
        c.setName(model.getName());
        c.setParentId(model.getParentId());
        if (model.getParentId() == null) {
            c.setParentId(entityId);
            model.setParentId(entityId);
        }
        c.setProviderType(model.getProviderType());
        c.setProviderId(model.getProviderId());
        c.setSubType(model.getSubType());
        c.setEntityId(entityId);
        em.persist(c);

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
                config.setId(UUID.randomUUID().toString());
                config.setName(key);
                config.setValue(val);
                config.setComponent(c);
                c.getComponentConfigs().add(config);
            }
        }
    }

    @Override
    public void updateComponent(String entityId, ComponentModel component) {
        componentUtil.getComponentFactory(component).validateConfiguration(entityId, component);

        ComponentEntity c = em.find(ComponentEntity.class, component.getId());
        if (c == null) return;
        c.setName(component.getName());
        c.setProviderId(component.getProviderId());
        c.setProviderType(component.getProviderType());
        c.setParentId(component.getParentId());
        c.setSubType(component.getSubType());
        setConfig(component, c);
        componentUtil.notifyUpdated(entityId, component);
    }

    @Override
    public void removeComponent(String entityId, ComponentModel component) {
        ComponentEntity c = em.find(ComponentEntity.class, component.getId());
        if (c == null) return;
        removeComponents(entityId, component.getId());
        ComponentEntity.deleteById(component.getId());
    }

    @Override
    public void removeComponents(String entityId, String parentId) {
        Predicate<ComponentEntity> sameParent = c -> Objects.equals(parentId, c.getParentId());

        List<ComponentEntity> components = ComponentEntity.find("entityId", entityId).list();
        components.stream().filter(sameParent).forEach(f -> {
            em.remove(f);
        });
    }

    @Override
    public List<ComponentModel> getComponents(String entityId, String parentId, String providerType) {
        if (parentId == null) parentId = entityId;
        final String parent = parentId;

        List<ComponentEntity> components = ComponentEntity.find("entityId", entityId).list();
        return components.stream()
                .filter(c -> parent.equals(c.getParentId()) && providerType.equals(c.getProviderType()))
                .map(this::entityToModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComponentModel> getComponents(String entityId, String parentId) {
        List<ComponentEntity> components = ComponentEntity.find("entityId", entityId).list();
        return components.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .map(this::entityToModel)
                .collect(Collectors.toList());
    }

    protected ComponentModel entityToModel(ComponentEntity c) {
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

    @Override
    public List<ComponentModel> getComponents(String entityId) {
        List<ComponentEntity> components = ComponentEntity.find("entityId", entityId).list();
        return components.stream()
                .map(this::entityToModel)
                .collect(Collectors.toList());
    }

    @Override
    public ComponentModel getComponent(String entityId, String id) {
        ComponentEntity c = em.find(ComponentEntity.class, id);
        if (c == null) return null;
        return entityToModel(c);
    }

}

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
package io.github.project.openubl.ublhub.resources;

import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.github.project.openubl.ublhub.keys.KeyProviderFactory;
import io.github.project.openubl.ublhub.keys.component.ComponentFactory;
import io.github.project.openubl.ublhub.keys.provider.ProviderConfigProperty;
import io.github.project.openubl.ublhub.models.ServerInfoDto;
import org.keycloak.representations.idm.ComponentTypeRepresentation;
import org.keycloak.representations.idm.ConfigPropertyRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Path("/server-info")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class ServerInfoResource {

    @Inject
    @Any
    Instance<KeyProviderFactory<?>> componentFactories;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ServerInfoDto getInfo() {
        ServerInfoDto info = new ServerInfoDto();
        setProviders(info);
        return info;
    }

    private void setProviders(ServerInfoDto info) {
        info.setComponentTypes(new HashMap<>());

        List<ComponentTypeRepresentation> types = new ArrayList<>();

        for (ComponentFactory componentFactory : componentFactories) {
            ComponentTypeRepresentation rep = new ComponentTypeRepresentation();
            rep.setId(componentFactory.getId());
            rep.setHelpText(componentFactory.getHelpText());
            List<ProviderConfigProperty> configProperties = componentFactory.getConfigProperties();
            if (configProperties == null) {
                configProperties = Collections.emptyList();
            }
            rep.setProperties(mapToRepresentation(configProperties));

            types.add(rep);
        }

        info.getComponentTypes().put(KeyProvider.class.getName(), types);
        info.getComponentTypes().put("keyProviders", types);
    }

    public List<ConfigPropertyRepresentation> mapToRepresentation(List<ProviderConfigProperty> configProperties) {
        List<ConfigPropertyRepresentation> propertiesRep = new LinkedList<>();
        for (ProviderConfigProperty prop : configProperties) {
            ConfigPropertyRepresentation propRep = mapToRepresentation(prop);
            propertiesRep.add(propRep);
        }
        return propertiesRep;
    }

    public static ConfigPropertyRepresentation mapToRepresentation(ProviderConfigProperty prop) {
        ConfigPropertyRepresentation propRep = new ConfigPropertyRepresentation();
        propRep.setName(prop.getName());
        propRep.setLabel(prop.getLabel());
        propRep.setType(prop.getType());
        propRep.setDefaultValue(prop.getDefaultValue());
        propRep.setOptions(prop.getOptions());
        propRep.setHelpText(prop.getHelpText());
        propRep.setSecret(prop.isSecret());
        return propRep;
    }
}

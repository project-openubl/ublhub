package io.github.project.openubl.ublhub.models;

import org.keycloak.representations.idm.ComponentTypeRepresentation;

import java.util.List;
import java.util.Map;

public class ServerInfoDto {
    private Map<String, List<ComponentTypeRepresentation>> componentTypes;

    public Map<String, List<ComponentTypeRepresentation>> getComponentTypes() {
        return componentTypes;
    }

    public void setComponentTypes(Map<String, List<ComponentTypeRepresentation>> componentTypes) {
        this.componentTypes = componentTypes;
    }
}

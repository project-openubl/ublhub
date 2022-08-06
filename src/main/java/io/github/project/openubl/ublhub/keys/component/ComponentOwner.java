package io.github.project.openubl.ublhub.keys.component;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentOwner {

    private String id;
    private OwnerType type;

    public enum OwnerType {
        project,
        company
    }

    public String getPrettyName() {
        return type + ":" + id;
    }

}

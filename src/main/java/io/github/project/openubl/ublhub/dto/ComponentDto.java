package io.github.project.openubl.ublhub.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class ComponentDto {

    private String id;
    private String name;
    private String providerId;
    private String providerType;
    private String parentId;
    private String subType;
    private Map<String, List<String>> config;

}

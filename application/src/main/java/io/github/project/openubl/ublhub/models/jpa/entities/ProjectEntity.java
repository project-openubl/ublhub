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
package io.github.project.openubl.ublhub.models.jpa.entities;

import io.github.project.openubl.ublhub.security.Role;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "project")
public class ProjectEntity extends PanacheEntityBase {

    public static final String NAME_PATTERN = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";

    @EqualsAndHashCode.Include
    @Id
    @Pattern(regexp = NAME_PATTERN)
    @Size(max = 255)
    @Column(name = "name")
    @Access(AccessType.PROPERTY)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Valid
    @Embedded
    private SunatEntity sunat;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<ProjectUserEntity> users;

    @Version
    @Column(name = "version")
    private int version;

    public void setProjectOwner(String username) {
        ProjectUserEntity projectUserEntity = new ProjectUserEntity();
        projectUserEntity.setId(new ProjectUserEntity.ProjectUserId(name, username));
        projectUserEntity.setRoles(String.join(",",
                Role.owner
        ));
        projectUserEntity.persist();
    }

    public boolean hasAnyRole(String username) {
        return ProjectUserEntity
                .findByIdOptional(new ProjectUserEntity.ProjectUserId(name, username))
                .isPresent();
    }

    public boolean hasAnyRole(String username, String... roles) {
        return ProjectUserEntity
                .<ProjectUserEntity>findByIdOptional(new ProjectUserEntity.ProjectUserId(name, username))
                .map(projectUserEntity -> {
                    Set<String> collect = Stream.of(roles).collect(Collectors.toSet());
                    return Arrays.stream(projectUserEntity.getRoles().split(","))
                            .anyMatch(collect::contains);
                })
                .orElse(false);
    }

}

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

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "component")
public class ComponentEntity extends PanacheEntityBase {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id", length = 36)
    @Access(AccessType.PROPERTY)
    public String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "project_id")
    public ProjectEntity project;

    @NotNull
    @Size(max = 255)
    @Column(name = "name")
    public String name;

    @Size(max = 255)
    @Column(name = "provider_type")
    public String providerType;

    @Size(max = 255)
    @Column(name = "provider_id")
    public String providerId;

    @Size(max = 255)
    @Column(name = "parent_id")
    public String parentId;

    @Size(max = 255)
    @Column(name = "sub_type")
    public String subType;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "component")
    public Set<ComponentConfigEntity> componentConfigs = new HashSet<>();

}

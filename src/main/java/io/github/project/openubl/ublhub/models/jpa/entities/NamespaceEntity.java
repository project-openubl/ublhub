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

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "namespace", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
public class NamespaceEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    public String id;

    @NotNull
    @Size(max = 255)
    @Column(name = "name")
    public String name;

    @Size(max = 255)
    public String description;

    @NotNull
    @Valid
    @Embedded
    public SunatEntity sunat;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "namespace", orphanRemoval = true, cascade = CascadeType.REMOVE)
    public List<CompanyEntity> companies = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "namespace", orphanRemoval = true, cascade = CascadeType.REMOVE)
    public List<ComponentEntity> components = new ArrayList<>();

}

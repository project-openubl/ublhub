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
package io.github.project.openubl.xsender.models.jpa.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "namespace", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
public class NamespaceEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    public String id;

    @NotNull
    @Column(name = "owner")
    public String owner;

    @NotNull
    @Column(name = "name")
    public String name;

    @Size(max = 250)
    public String description;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    public Date createdOn;

    @Version
    @Column(name = "version")
    public int version;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "namespace", orphanRemoval = true)
    public List<CompanyEntity> companies = new ArrayList<>();

//    @OnDelete(action = OnDeleteAction.CASCADE)
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "namespace", orphanRemoval = true)
//    public List<UBLDocumentEntity> documents = new ArrayList<>();

}

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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
    private String id;

    @NotNull
    @Column(name = "owner")
    private String owner;

    @NotNull
    @Column(name = "name")
    private String name;

    @Size(max = 250)
    private String description;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Version
    @Column(name = "version")
    private int version;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "namespace", orphanRemoval = true)
    private List<CompanyEntity> companies = new ArrayList<>();

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "namespace", orphanRemoval = true)
    private List<UBLDocumentEntity> documents = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<CompanyEntity> getCompanies() {
        return companies;
    }

    public void setCompanies(List<CompanyEntity> companies) {
        this.companies = companies;
    }

    public List<UBLDocumentEntity> getDocuments() {
        return documents;
    }

    public void setDocuments(List<UBLDocumentEntity> documents) {
        this.documents = documents;
    }

    public static final class NamespaceEntityBuilder {
        private String id;
        private String owner;
        private String name;
        private String description;
        private Date createdOn;
        private int version;

        private NamespaceEntityBuilder() {
        }

        public static NamespaceEntityBuilder aNamespaceEntity() {
            return new NamespaceEntityBuilder();
        }

        public NamespaceEntityBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public NamespaceEntityBuilder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public NamespaceEntityBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public NamespaceEntityBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public NamespaceEntityBuilder withCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public NamespaceEntityBuilder withVersion(int version) {
            this.version = version;
            return this;
        }

        public NamespaceEntity build() {
            NamespaceEntity namespaceEntity = new NamespaceEntity();
            namespaceEntity.setId(id);
            namespaceEntity.setOwner(owner);
            namespaceEntity.setName(name);
            namespaceEntity.setDescription(description);
            namespaceEntity.setCreatedOn(createdOn);
            namespaceEntity.setVersion(version);
            return namespaceEntity;
        }
    }
}

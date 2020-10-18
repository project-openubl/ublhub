/**
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
package io.github.project.openubl.xsender.core.models.jpa.entities;

import io.github.project.openubl.xsender.core.models.OrganizationType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "ORGANIZATION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"NAME"})
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ORG_TYPE", discriminatorType = DiscriminatorType.STRING)
public class OrganizationEntity extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @Column(name = "OWNER")
    private String owner;

    @NotNull
    @Column(name = "NAME")
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private OrganizationType type;

    @Embedded
    private SunatCredentialsEntity sunatCredentials;

    @Embedded
    private SunatUrlsEntity sunatUrls;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "corporate")
    private Set<RepositoryEntity> companies = new HashSet<>();

    @Version
    @Column(name = "VERSION")
    private int version;

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

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public SunatCredentialsEntity getSunatCredentials() {
        return sunatCredentials;
    }

    public void setSunatCredentials(SunatCredentialsEntity sunatCredentials) {
        this.sunatCredentials = sunatCredentials;
    }

    public SunatUrlsEntity getSunatUrls() {
        return sunatUrls;
    }

    public void setSunatUrls(SunatUrlsEntity sunatUrls) {
        this.sunatUrls = sunatUrls;
    }

    public Set<RepositoryEntity> getCompanies() {
        return companies;
    }

    public void setCompanies(Set<RepositoryEntity> companies) {
        this.companies = companies;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public static final class Builder {
        private String id;
        private String owner;
        private String name;
        private OrganizationType type;
        private SunatCredentialsEntity sunatCredentials;
        private SunatUrlsEntity sunatUrls;
        private Set<RepositoryEntity> companies = new HashSet<>();
        private int version;

        private Builder() {
        }

        public static Builder anOrganizationEntity() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(OrganizationType type) {
            this.type = type;
            return this;
        }

        public Builder withSunatCredentials(SunatCredentialsEntity sunatCredentials) {
            this.sunatCredentials = sunatCredentials;
            return this;
        }

        public Builder withSunatUrls(SunatUrlsEntity sunatUrls) {
            this.sunatUrls = sunatUrls;
            return this;
        }

        public Builder withCompanies(Set<RepositoryEntity> companies) {
            this.companies = companies;
            return this;
        }

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public OrganizationEntity build() {
            OrganizationEntity organizationEntity = new OrganizationEntity();
            organizationEntity.setId(id);
            organizationEntity.setOwner(owner);
            organizationEntity.setName(name);
            organizationEntity.setType(type);
            organizationEntity.setSunatCredentials(sunatCredentials);
            organizationEntity.setSunatUrls(sunatUrls);
            organizationEntity.setCompanies(companies);
            organizationEntity.setVersion(version);
            return organizationEntity;
        }
    }
}

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

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "company", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"namespace_id", "ruc"})
})
public class CompanyEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @Column(name = "ruc")
    private String ruc;

    @NotNull
    @Column(name = "name")
    private String name;

    private String description;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @NotNull
    @Valid
    @Embedded
    private SunatCredentialsEntity sunatCredentials;

    @NotNull
    @Valid
    @Embedded
    private SunatUrlsEntity sunatUrls;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "namespace_id")
    private NamespaceEntity namespace;

    @Version
    @Column(name = "version")
    private int version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
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

    public NamespaceEntity getNamespace() {
        return namespace;
    }

    public void setNamespace(NamespaceEntity namespace) {
        this.namespace = namespace;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public static final class CompanyEntityBuilder {
        private String id;
        private String ruc;
        private String name;
        private String description;
        private Date createdOn;
        private SunatCredentialsEntity sunatCredentials;
        private SunatUrlsEntity sunatUrls;
        private NamespaceEntity namespace;
        private int version;

        private CompanyEntityBuilder() {
        }

        public static CompanyEntityBuilder aCompanyEntity() {
            return new CompanyEntityBuilder();
        }

        public CompanyEntityBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public CompanyEntityBuilder withRuc(String ruc) {
            this.ruc = ruc;
            return this;
        }

        public CompanyEntityBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public CompanyEntityBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CompanyEntityBuilder withCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public CompanyEntityBuilder withSunatCredentials(SunatCredentialsEntity sunatCredentials) {
            this.sunatCredentials = sunatCredentials;
            return this;
        }

        public CompanyEntityBuilder withSunatUrls(SunatUrlsEntity sunatUrls) {
            this.sunatUrls = sunatUrls;
            return this;
        }

        public CompanyEntityBuilder withNamespace(NamespaceEntity namespace) {
            this.namespace = namespace;
            return this;
        }

        public CompanyEntityBuilder withVersion(int version) {
            this.version = version;
            return this;
        }

        public CompanyEntity build() {
            CompanyEntity companyEntity = new CompanyEntity();
            companyEntity.setId(id);
            companyEntity.setRuc(ruc);
            companyEntity.setName(name);
            companyEntity.setDescription(description);
            companyEntity.setCreatedOn(createdOn);
            companyEntity.setSunatCredentials(sunatCredentials);
            companyEntity.setSunatUrls(sunatUrls);
            companyEntity.setNamespace(namespace);
            companyEntity.setVersion(version);
            return companyEntity;
        }
    }
}

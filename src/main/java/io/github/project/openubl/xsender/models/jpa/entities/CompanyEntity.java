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
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "COMPANY", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"NAME"})
})
public class CompanyEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @Column(name = "owner")
    private String owner;

    @Pattern(regexp = "[a-z0-9]([-a-z0-9]*[a-z0-9])?", message = "label must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character (e.g. 'my-name', or '123-abc')")
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

    @Version
    @Column(name = "version")
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyEntity that = (CompanyEntity) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static final class Builder {
        private String id;
        private String owner;
        private String name;
        private String description;
        private Date createdOn;
        private SunatCredentialsEntity sunatCredentials;
        private SunatUrlsEntity sunatUrls;
        private int version;

        private Builder() {
        }

        public static Builder aCompanyEntity() {
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

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
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

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public CompanyEntity build() {
            CompanyEntity companyEntity = new CompanyEntity();
            companyEntity.setId(id);
            companyEntity.setOwner(owner);
            companyEntity.setName(name);
            companyEntity.setDescription(description);
            companyEntity.setCreatedOn(createdOn);
            companyEntity.setSunatCredentials(sunatCredentials);
            companyEntity.setSunatUrls(sunatUrls);
            companyEntity.setVersion(version);
            return companyEntity;
        }
    }
}

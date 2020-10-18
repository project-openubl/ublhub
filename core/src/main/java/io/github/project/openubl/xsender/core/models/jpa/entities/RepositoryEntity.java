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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "ORGANIZATION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"NAME", "CORPORATE_ID"})
})
@NamedQueries(value = {
        @NamedQuery(name = "FindByName", query = "select o from RepositoryEntity o where o.name = :name"),
        @NamedQuery(name = "ListOrganizations", query = "select o from RepositoryEntity o"),
        @NamedQuery(name = "FilterOrganizations", query = "select o from RepositoryEntity o where lower(o.name) like :filterText")
})
public class RepositoryEntity extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    @Access(AccessType.PROPERTY)
    private String id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "CORPORATE_ID")
    private OrganizationEntity corporate;

    @NotNull
    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @NotNull
    @Column(name = "USING_CORPORATE_URLS")
    private boolean usingCorporateUrls;

    @NotNull
    @Column(name = "USING_CORPORATE_CREDENTIALS")
    private boolean usingCorporateCredentials;

    @Embedded
    private SunatCredentialsEntity sunatCredentials;

    @Embedded
    private SunatUrlsEntity sunatUrls;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "company")
    private List<UBLDocumentEntity> documents = new ArrayList<>();

    @Version
    @Column(name = "VERSION")
    private int version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OrganizationEntity getCorporate() {
        return corporate;
    }

    public void setCorporate(OrganizationEntity corporate) {
        this.corporate = corporate;
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

    public boolean isUsingCorporateUrls() {
        return usingCorporateUrls;
    }

    public void setUsingCorporateUrls(boolean usingCorporateUrls) {
        this.usingCorporateUrls = usingCorporateUrls;
    }

    public boolean isUsingCorporateCredentials() {
        return usingCorporateCredentials;
    }

    public void setUsingCorporateCredentials(boolean usingCorporateCredentials) {
        this.usingCorporateCredentials = usingCorporateCredentials;
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

    public List<UBLDocumentEntity> getDocuments() {
        return documents;
    }

    public void setDocuments(List<UBLDocumentEntity> documents) {
        this.documents = documents;
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
        RepositoryEntity that = (RepositoryEntity) o;
        return corporate.equals(that.corporate) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(corporate, name);
    }

}

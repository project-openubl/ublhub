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
    public String id;

    @NotNull
    @Column(name = "ruc")
    public String ruc;

    @NotNull
    @Column(name = "name")
    public String name;

    public String description;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    public Date createdOn;

    @NotNull
    @Valid
    @Embedded
    public SunatCredentialsEntity sunatCredentials;

    @NotNull
    @Valid
    @Embedded
    public SunatUrlsEntity sunatUrls;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey, name = "namespace_id")
    public NamespaceEntity namespace;

    @Version
    @Column(name = "version")
    public int version;

}

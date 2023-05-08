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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "company")
public class CompanyEntity extends PanacheEntityBase {

    public static final String RUC_PATTERN = "[0-9]+";

    @Embeddable
    @EqualsAndHashCode
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompanyId implements Serializable {
        @Pattern(regexp = ProjectEntity.NAME_PATTERN)
        @NotNull
        @Column(name = "project")
        private String project;

        @Size(min = 11, max = 11)
        @Column(name = "ruc")
        @Access(AccessType.PROPERTY)
        private String ruc;
    }

    @EmbeddedId
    private CompanyId id;

    @NotNull
    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    private String description;

    @Size(max = 255)
    @Column(name = "logo_file_id")
    private String logoFileId;

    @Valid
    @Embedded
    private SunatEntity sunat;

    @Version
    @Column(name = "version")
    private int version;
}

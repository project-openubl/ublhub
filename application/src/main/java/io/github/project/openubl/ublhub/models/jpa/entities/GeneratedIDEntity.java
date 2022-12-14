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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "generated_id")
public class GeneratedIDEntity extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    private Long id;

    @NotNull
    @Size(max = 11)
    @Column(name = "ruc")
    private String ruc;

    @NotNull
    @Size(max = 50)
    @Column(name = "document_type")
    private String documentType;

    @NotNull
    @Min(1)
    @Column(name = "serie")
    private int serie;

    @NotNull
    @Min(1)
    @Column(name = "numero")
    private int numero;

    @NotNull
    @Column(name = "project_id")
    private Long projectId;

    @Version
    @Column(name = "version")
    private int version;

}

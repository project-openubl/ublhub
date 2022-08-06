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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sunat_response")
public class SUNATResponseEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    public String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    public UBLDocumentEntity document;

    @Size(max = 50)
    @Column(name = "ticket")
    public String ticket;

    @Size(max = 50)
    @Column(name = "status")
    public String status;

    @Column(name = "code")
    public Integer code;

    @Size(max = 255)
    @Column(name = "description")
    public String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "value")
    @CollectionTable(name = "sunat_response_notes", joinColumns = {@JoinColumn(name = "sunat_response_id")})
    public Set<@NotNull @Size(max = 255) String> notes = new HashSet<>();

}

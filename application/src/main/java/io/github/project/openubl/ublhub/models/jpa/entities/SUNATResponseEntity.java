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

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SUNATResponseEntity {

    @Size(max = 50)
    @Column(name = "sunat_ticket")
    private String ticket;

    @Size(max = 50)
    @Column(name = "sunat_status")
    private String status;

    @Column(name = "sunat_code")
    private Integer code;

    @Setter(AccessLevel.NONE)
    @Size(max = 255)
    @Column(name = "sunat_description")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "val")
    @CollectionTable(name = "sunat_note", joinColumns = {@JoinColumn(name = "sunat_note_id")})
    private Set<@NotNull @Size(max = 255) String> notes = new HashSet<>();

    public void setDescription(String description) {
        if (description == null) {
            this.description = description;
        } else {
            this.description = StringUtils.truncate(description, 255);
        }
    }
}

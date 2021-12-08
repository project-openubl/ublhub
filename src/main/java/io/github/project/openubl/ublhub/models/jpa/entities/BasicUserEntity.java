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

import io.github.project.openubl.ublhub.idm.BasicUserRepresentation;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "basic_user", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
@UserDefinition
public class BasicUserEntity extends PanacheEntity {

    @Column(name = "full_name")
    public String fullName;

    @NotNull
    @Username
    public String username;

    @NotNull
    @Password
    public String password;

    @Roles
    public String permissions;

    @Version
    public int version;

    public BasicUserRepresentation toRepresentation() {
        BasicUserRepresentation result = new BasicUserRepresentation();
        result.setId(id);
        result.setFullName(fullName);
        result.setUsername(username);
        result.setPermissions(Arrays.stream(permissions.split(",")).sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
        return result;
    }

    public static BasicUserEntity add(BasicUserRepresentation rep) {
        BasicUserEntity user = new BasicUserEntity();
        user.fullName = rep.getFullName();
        user.username = rep.getUsername();
        user.password = BcryptUtil.bcryptHash(rep.getPassword());
        user.permissions = String.join(",", rep.getPermissions());
        user.persist();
        return user;
    }

    public static BasicUserEntity update(BasicUserEntity user, BasicUserRepresentation rep) {
        user.fullName = rep.getFullName();
        user.username = rep.getUsername();
        if (rep.getPassword() != null) {
            user.password = BcryptUtil.bcryptHash(rep.getPassword());
        }
        user.permissions = String.join(",", rep.getPermissions());
        user.persist();
        return user;
    }

}
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
package io.github.project.openubl.ublhub.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Set;

@RegisterForReflection
public final class BasicUserRepresentationBuilder {
    private Long id;
    private String fullName;
    private String username;
    private String password;
    private Set<String> permissions;

    private BasicUserRepresentationBuilder() {
    }

    public static BasicUserRepresentationBuilder aBasicUserRepresentation() {
        return new BasicUserRepresentationBuilder();
    }

    public BasicUserRepresentationBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public BasicUserRepresentationBuilder withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public BasicUserRepresentationBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public BasicUserRepresentationBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public BasicUserRepresentationBuilder withPermissions(Set<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public BasicUserRepresentation build() {
        BasicUserRepresentation basicUserRepresentation = new BasicUserRepresentation();
        basicUserRepresentation.setId(id);
        basicUserRepresentation.setFullName(fullName);
        basicUserRepresentation.setUsername(username);
        basicUserRepresentation.setPassword(password);
        basicUserRepresentation.setPermissions(permissions);
        return basicUserRepresentation;
    }
}

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
package io.github.project.openubl.ublhub.models.jpa;

import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectUserEntity;
import io.github.project.openubl.ublhub.security.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@Transactional
@ApplicationScoped
public class ProjectUserRepository implements PanacheRepositoryBase<ProjectUserEntity, ProjectUserEntity.ProjectUserId> {

    public ProjectUserEntity createOwner(ProjectEntity project, String username) {
        ProjectUserEntity projectUserEntity = new ProjectUserEntity();
        projectUserEntity.setId(new ProjectUserEntity.ProjectUserId(project.getName(), username));
        projectUserEntity.setRoles(Role.owner);
        projectUserEntity.persist();
        return projectUserEntity;
    }
}

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

import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@Transactional
@ApplicationScoped
public class CompanyRepository implements PanacheRepositoryBase<CompanyEntity, CompanyEntity.CompanyId> {

    public enum SortByField {
        name
    }

    public List<CompanyEntity> listAll(ProjectEntity project) {
        Sort sort = Sort.by(SortByField.name.toString(), Sort.Direction.Descending);
        return listAll(project, sort);
    }

    public List<CompanyEntity> listAll(ProjectEntity project, Sort sort) {
        Parameters params = Parameters.with("project", project.getName());

        PanacheQuery<CompanyEntity> query = CompanyEntity
                .find("From CompanyEntity as c where c.id.project = :project", sort, params);

        return query.list();
    }
}

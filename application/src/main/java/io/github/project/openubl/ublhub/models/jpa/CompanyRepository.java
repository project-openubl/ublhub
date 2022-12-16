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

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
@ApplicationScoped
public class CompanyRepository implements PanacheRepositoryBase<CompanyEntity, Long> {

    public enum SortByField {
        name,
        created
    }

    public CompanyEntity findById(ProjectEntity project, Long companyId) {
        return findById(project.getId(), companyId);
    }

    public CompanyEntity findById(Long projectId, Long companyId) {
        Parameters params = Parameters.with("projectId", projectId).and("companyId", companyId);
        return find("id = :companyId and projectId = :projectId", params).firstResult();
    }

    public CompanyEntity findByRuc(ProjectEntity project, String ruc) {
        return findByRuc(project.getId(), ruc);
    }

    public CompanyEntity findByRuc(Long projectId, String ruc) {
        Parameters params = Parameters.with("projectId", projectId).and("ruc", ruc);
        return find("ruc = :ruc and projectId = :projectId", params).firstResult();
    }

    public List<CompanyEntity> listAll(ProjectEntity project) {
        Sort sort = Sort.by(CompanyRepository.SortByField.created.toString(), Sort.Direction.Descending);
        return listAll(project, sort);
    }

    public List<CompanyEntity> listAll(ProjectEntity project, Sort sort) {
        Parameters params = Parameters.with("projectId", project.getId());

        PanacheQuery<CompanyEntity> query = CompanyEntity
                .find("From CompanyEntity as c where c.projectId = :projectId", sort, params);

        return query.list();
    }

    public boolean deleteByProjectIdAndId(Long projectId, Long id) {
        Parameters params = Parameters.with("projectId", projectId).and("id", id);
        long rows = CompanyEntity
                .delete("projectId = :projectId and id = :id", params);
        return rows > 0;
    }
}

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

import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CompanyRepository implements PanacheRepositoryBase<CompanyEntity, String> {

    public static final String[] SORT_BY_FIELDS = {"name", "created"};

    public Uni<CompanyEntity> findById(ProjectEntity project, String companyId) {
        return findById(project.id, companyId);
    }

    public Uni<CompanyEntity> findById(String projectId, String companyId) {
        Parameters params = Parameters.with("projectId", projectId).and("companyId", companyId);
        return find("id = :companyId and project.id = :projectId", params).firstResult();
    }

    public Uni<CompanyEntity> findByRuc(ProjectEntity project, String ruc) {
        return findByRuc(project.id, ruc);
    }

    public Uni<CompanyEntity> findByRuc(String projectId, String ruc) {
        Parameters params = Parameters.with("projectId", projectId).and("ruc", ruc);
        return find("ruc = :ruc and project.id = :projectId", params).firstResult();
    }

    public UniAndGroup2<List<CompanyEntity>, Long> list(ProjectEntity project, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        Parameters params = Parameters.with("projectId", project.id);

        PanacheQuery<CompanyEntity> query = CompanyEntity
                .find("From CompanyEntity as c where c.project.id = :projectId", sort, params)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return Uni.combine().all().unis(query.list(), query.count());
    }

    public UniAndGroup2<List<CompanyEntity>, Long> list(ProjectEntity project, String filterText, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        Parameters params = Parameters.with("projectId", project.id).and("filterText", "%" + filterText.toLowerCase() + "%");

        PanacheQuery<CompanyEntity> query = CompanyEntity
                .find("From CompanyEntity as c where c.project.id = :projectId and (lower(c.name) like :filterText or c.ruc like :filterText)", sort, params)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return Uni.combine().all().unis(query.list(), query.count());
    }

    public Uni<Boolean> deleteByNamespaceIdAndId(String projectId, String id) {
        Parameters params = Parameters.with("projectId", projectId).and("id", id);
        return CompanyEntity
                .delete("project.id = :projectId and id = :id", params)
                .map(rowCount -> rowCount > 0);
    }
}

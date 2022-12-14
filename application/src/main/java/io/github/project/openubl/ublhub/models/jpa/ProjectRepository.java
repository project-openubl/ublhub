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
import io.github.project.openubl.ublhub.models.SearchBean;
import io.github.project.openubl.ublhub.models.SortBean;
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
public class ProjectRepository implements PanacheRepositoryBase<ProjectEntity, String> {

    public enum SortByField {
        name,
        created
    }

    public static final String[] SORT_BY_FIELDS = {SortByField.name.toString(), SortByField.created.toString()};

    public ProjectEntity findByName(String name) {
        return find("name", name).firstResult();
    }

    public SearchBean<ProjectEntity> list(PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<ProjectEntity> query = ProjectEntity
                .find("From ProjectEntity as n", sort)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);
        return new SearchBean<>(query.count(), query.list());
    }

    public SearchBean<ProjectEntity> list(String filterText, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<ProjectEntity> query = ProjectEntity
                .find(
                        "From ProjectEntity as o where lower(o.name) like :filterText",
                        sort,
                        Parameters.with("filterText", "%" + filterText.toLowerCase() + "%")
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return new SearchBean<>(query.count(), query.list());
    }

    @Override
    public boolean deleteById(String id) {
        long rows = ProjectEntity.delete("id", id);
        return rows > 0;
    }
}

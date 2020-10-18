/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.core.models.jpa;

import io.github.project.openubl.xsender.core.models.PageBean;
import io.github.project.openubl.xsender.core.models.PageModel;
import io.github.project.openubl.xsender.core.models.SortBean;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
@ApplicationScoped
public class OrganizationRepository implements PanacheRepositoryBase<OrganizationEntity, String> {

    public static final String[] SORT_BY_FIELDS = {"name"};

    public Optional<OrganizationEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public static PageModel<OrganizationEntity> listAll(PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<OrganizationEntity> query = OrganizationEntity
                .findAll(sort)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<OrganizationEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }

    public static PageModel<OrganizationEntity> listAll(String filterText, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<OrganizationEntity> query = OrganizationEntity
                .find(
                        "From OrganizationEntity as o where lower(o.name) like :filterText",
                        sort,
                        Parameters.with("filterText", "%" + filterText.toLowerCase() + "%")
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<OrganizationEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }

    public static PageModel<OrganizationEntity> list(String owner, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<OrganizationEntity> query = OrganizationEntity
                .find(
                        "From OrganizationEntity as o where o.owner =:owner",
                        sort,
                        Parameters.with("owner", owner)
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<OrganizationEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }

    public static PageModel<OrganizationEntity> list(String owner, String filterText, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<OrganizationEntity> query = OrganizationEntity
                .find(
                        "From OrganizationEntity as o where o.owner =:owner and lower(o.name) like :filterText",
                        sort,
                        Parameters.with("owner", owner).and("filterText", "%" + filterText.toLowerCase() + "%")
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<OrganizationEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }

}

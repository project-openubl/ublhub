/*
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
package io.github.project.openubl.xsender.models.jpa;

import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
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
public class CompanyRepository implements PanacheRepositoryBase<CompanyEntity, String> {

    public static final String[] SORT_BY_FIELDS = {"name", "createdOn"};

    public Optional<CompanyEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public Optional<CompanyEntity> findByNameAndOwner(String name, String owner) {
        return find("name = ?1 and owner = ?2", name, owner).firstResultOptional();
    }

    public static PageModel<CompanyEntity> list(String owner, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<CompanyEntity> query = CompanyEntity
                .find(
                        "From CompanyEntity as o where o.owner =:owner",
                        sort,
                        Parameters.with("owner", owner)
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<CompanyEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }

    public static PageModel<CompanyEntity> list(String owner, String filterText, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<CompanyEntity> query = CompanyEntity
                .find(
                        "From CompanyEntity as o where o.owner =:owner and lower(o.name) like :filterText",
                        sort,
                        Parameters.with("owner", owner).and("filterText", "%" + filterText.toLowerCase() + "%")
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<CompanyEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }

}

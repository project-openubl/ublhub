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
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class NamespaceRepository implements PanacheRepositoryBase<NamespaceEntity, String> {

    public static final String[] SORT_BY_FIELDS = {"name", "createdOn"};

    public Uni<NamespaceEntity> findByName(String name) {
        return find("name", name).firstResult();
    }

    public Uni<NamespaceEntity> findByIdAndOwner(String id, String owner) {
        return find("id = ?1 and owner = ?2", id, owner).firstResult();
    }

    public UniAndGroup2<List<NamespaceEntity>, Long> list(String owner, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<NamespaceEntity> query = NamespaceEntity
                .find(
                        "From NamespaceEntity as o where o.owner =:owner",
                        sort,
                        Parameters.with("owner", owner)
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return Uni.combine().all().unis(query.list(), query.count());
    }

    public UniAndGroup2<List<NamespaceEntity>, Long> list(String owner, String filterText, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<NamespaceEntity> query = NamespaceEntity
                .find(
                        "From NamespaceEntity as o where o.owner =:owner and lower(o.name) like :filterText",
                        sort,
                        Parameters.with("owner", owner).and("filterText", "%" + filterText.toLowerCase() + "%")
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return Uni.combine().all().unis(query.list(), query.count());
    }

}

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

import io.github.project.openubl.xsender.models.DocumentFilterModel;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UBLDocumentRepository implements PanacheRepositoryBase<UBLDocumentEntity, String> {

    public static final String[] SORT_BY_FIELDS = {"createdOn"};

    public Uni<UBLDocumentEntity> findById(NamespaceEntity namespace, String id) {
        Parameters queryParameters = Parameters.with("namespaceId", namespace.id)
                .and("id", id);
        return UBLDocumentEntity
                .find("From UBLDocumentEntity as d where d.namespace.id = :namespaceId and d.id = :id", queryParameters)
                .firstResult();
    }

    public UniAndGroup2<List<UBLDocumentEntity>, Long> list(NamespaceEntity namespace, DocumentFilterModel filters, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        StringBuilder queryBuilder = new StringBuilder("From UBLDocumentEntity as c where c.namespace.id = :namespaceId");
        Parameters queryParameters = Parameters.with("namespaceId", namespace.id);

        if (filters.getRuc() != null && !filters.getRuc().isEmpty()) {
            queryBuilder.append(" and c.ruc in :ruc");
            queryParameters = queryParameters.and("ruc", filters.getRuc());
        }
        if (filters.getDocumentType() != null && !filters.getDocumentType().isEmpty()) {
            queryBuilder.append(" and c.documentType in :documentType");
            queryParameters = queryParameters.and("documentType", filters.getDocumentType());
        }

        PanacheQuery<UBLDocumentEntity> query = UBLDocumentEntity
                .find(queryBuilder.toString(), sort, queryParameters)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return Uni.combine().all().unis(query.list(), query.count());
    }

    public UniAndGroup2<List<UBLDocumentEntity>, Long> list(NamespaceEntity namespace, String filterText, DocumentFilterModel filters, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        StringBuilder queryBuilder = new StringBuilder("From UBLDocumentEntity as c where c.namespace.id = :namespaceId and lower(c.documentID) like :filterText");
        Parameters queryParameters = Parameters.with("namespaceId", namespace.id).and("filterText", "%" + filterText.toLowerCase());

        if (filters.getRuc() != null && !filters.getRuc().isEmpty()) {
            queryBuilder.append(" and c.ruc in :ruc");
            queryParameters = queryParameters.and("ruc", filters.getRuc());
        }
        if (filters.getDocumentType() != null && !filters.getDocumentType().isEmpty()) {
            queryBuilder.append(" and c.documentType in :documentType");
            queryParameters = queryParameters.and("documentType", filters.getDocumentType());
        }

        PanacheQuery<UBLDocumentEntity> query = UBLDocumentEntity
                .find(queryBuilder.toString(), sort, queryParameters)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return Uni.combine().all().unis(query.list(), query.count());
    }
}

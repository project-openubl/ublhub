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

import io.github.project.openubl.ublhub.models.DocumentFilterModel;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
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

    public static final String[] SORT_BY_FIELDS = {"created"};

    public Uni<UBLDocumentEntity> findById(NamespaceEntity namespace, String id) {
        return findById(namespace.id, id);
    }

    public Uni<UBLDocumentEntity> findById(String namespaceId, String id) {
        Parameters params = Parameters.with("namespaceId", namespaceId).and("id", id);
        return UBLDocumentEntity
                .find("From UBLDocumentEntity as d where d.namespace.id = :namespaceId and d.id = :id", params)
                .firstResult();
    }

    public UniAndGroup2<List<UBLDocumentEntity>, Long> list(NamespaceEntity namespace, DocumentFilterModel filters, PageBean pageBean, List<SortBean> sortBy) {
        return list(namespace, null, filters, pageBean, sortBy);
    }

    public UniAndGroup2<List<UBLDocumentEntity>, Long> list(NamespaceEntity namespace, String filterText, DocumentFilterModel filters, PageBean pageBean, List<SortBean> sortBy) {
        StringBuilder queryBuilder = new StringBuilder("select distinct d from UBLDocumentEntity d left join d.xmlFileContent x where d.namespace.id = :namespaceId");
        Parameters params = Parameters.with("namespaceId", namespace.id);

        if (filterText != null && !filterText.trim().isEmpty()) {
            queryBuilder.append(" and lower(x.serieNumero) like :filterText");
            params = params.and("filterText", "%" + filterText.toLowerCase());
        }
        if (filters.getRuc() != null && !filters.getRuc().isEmpty()) {
            queryBuilder.append(" and x.ruc in :ruc");
            params = params.and("ruc", filters.getRuc());
        }
        if (filters.getDocumentType() != null && !filters.getDocumentType().isEmpty()) {
            queryBuilder.append(" and x.documentType in :documentType");
            params = params.and("documentType", filters.getDocumentType());
        }

        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and("d." + f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<UBLDocumentEntity> query = UBLDocumentEntity
                .find(queryBuilder.toString(), sort, params)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return Uni.combine().all().unis(query.list(), query.count());
    }
}

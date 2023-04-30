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

import io.github.project.openubl.ublhub.models.FilterDocumentBean;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SearchBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
@ApplicationScoped
public class UBLDocumentRepository implements PanacheRepositoryBase<UBLDocumentEntity, Long> {

    public static final String[] SORT_BY_FIELDS = {"created"};

    public UBLDocumentEntity findById(ProjectEntity project, Long id) {
        return findById(project.getId(), id);
    }

    public UBLDocumentEntity findById(Long projectId, Long id) {
        Parameters params = Parameters.with("projectId", projectId).and("id", id);
        return UBLDocumentEntity
                .find("From UBLDocumentEntity as d where d.projectId = :projectId and d.id = :id", params)
                .firstResult();
    }

    public SearchBean<UBLDocumentEntity> list(ProjectEntity project, FilterDocumentBean filters, PageBean pageBean, List<SortBean> sortBy) {
        return list(project, null, filters, pageBean, sortBy);
    }

    public SearchBean<UBLDocumentEntity> list(ProjectEntity project, String filterText, FilterDocumentBean filters, PageBean pageBean, List<SortBean> sortBy) {
        StringBuilder queryBuilder = new StringBuilder("select d from UBLDocumentEntity d where d.projectId = :projectId");
        Parameters params = Parameters.with("projectId", project.getId());

        if (filterText != null && !filterText.trim().isEmpty()) {
            queryBuilder.append(" and lower(d.xmlData.serieNumero) like :filterText");
            params = params.and("filterText", "%" + filterText.toLowerCase());
        }
        if (filters.getRuc() != null && !filters.getRuc().isEmpty()) {
            queryBuilder.append(" and d.xmlData.ruc in :ruc");
            params = params.and("ruc", filters.getRuc());
        }
        if (filters.getDocumentType() != null && !filters.getDocumentType().isEmpty()) {
            queryBuilder.append(" and d.xmlData.tipoDocumento in :documentType");
            params = params.and("documentType", filters.getDocumentType());
        }

        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and("d." + f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<UBLDocumentEntity> query = UBLDocumentEntity
                .find(queryBuilder.toString(), sort, params)
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        return new SearchBean<>(query.count(), query.list());
    }
}

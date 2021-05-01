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

import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UBLDocumentRepository implements PanacheRepositoryBase<UBLDocumentEntity, String> {

    public static final String[] SORT_BY_FIELDS = {"createdOn"};

//    public List<UBLDocumentEntity> findAllThatCouldNotBeDelivered() {
//        return list("deliveryStatus", DeliveryStatusType.COULD_NOT_BE_DELIVERED);
//    }

    public PageModel<UBLDocumentEntity> list(CompanyEntity company, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<UBLDocumentEntity> query = CompanyEntity
                .find(
                        "From UBLDocumentEntity as d where d.company =:company",
                        sort,
                        Parameters.with("company", company)
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<UBLDocumentEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }

    public PageModel<UBLDocumentEntity> list(CompanyEntity company, String filterText, PageBean pageBean, List<SortBean> sortBy) {
        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(f.getFieldName(), f.isAsc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<UBLDocumentEntity> query = CompanyEntity
                .find(
                        "From UBLDocumentEntity as d where d.company =:company and lower(d.documentID) like :filterText",
                        sort,
                        Parameters.with("company", company)
                                .and("filterText", "%" + filterText.toLowerCase() + "%")
                )
                .range(pageBean.getOffset(), pageBean.getOffset() + pageBean.getLimit() - 1);

        long count = query.count();
        List<UBLDocumentEntity> list = query.list();
        return new PageModel<>(pageBean, count, list);
    }
}

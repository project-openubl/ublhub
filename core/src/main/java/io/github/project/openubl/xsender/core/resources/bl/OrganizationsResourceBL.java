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
package io.github.project.openubl.xsender.core.resources.bl;

import io.github.project.openubl.xsender.core.idm.OrganizationRepresentation;
import io.github.project.openubl.xsender.core.idm.PageRepresentation;
import io.github.project.openubl.xsender.core.models.ContextBean;
import io.github.project.openubl.xsender.core.models.PageBean;
import io.github.project.openubl.xsender.core.models.PageModel;
import io.github.project.openubl.xsender.core.models.SortBean;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.github.project.openubl.xsender.core.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.core.resources.utils.ResourceUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.InternalServerErrorException;
import java.net.URISyntaxException;
import java.util.List;

@ApplicationScoped
public class OrganizationsResourceBL {

    public PageRepresentation<OrganizationRepresentation> listAllOrganizations(
            ContextBean contextBean,
            String name,
            Integer offset,
            Integer limit,
            List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, OrganizationRepository.SORT_BY_FIELDS);

        PageModel<OrganizationEntity> pageModel;
        if (name != null && !name.trim().isEmpty()) {
            pageModel = OrganizationRepository.listAll(name, pageBean, sortBeans);
        } else {
            pageModel = OrganizationRepository.listAll(pageBean, sortBeans);
        }

        List<NameValuePair> queryParameters = ResourceUtils.buildNameValuePairs(offset, limit, sortBeans);
        if (name != null) {
            queryParameters.add(new BasicNameValuePair("name", name));
        }

        try {
            return EntityToRepresentation.toRepresentation(
                    pageModel,
                    EntityToRepresentation::toRepresentation,
                    contextBean.getUriInfo(),
                    queryParameters
            );
        } catch (URISyntaxException e) {
            throw new InternalServerErrorException();
        }
    }

    public PageRepresentation<OrganizationRepresentation> listOrganizations(
            ContextBean contextBean,
            String name,
            Integer offset,
            Integer limit,
            List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, OrganizationRepository.SORT_BY_FIELDS);

        PageModel<OrganizationEntity> pageModel;
        if (name != null && !name.trim().isEmpty()) {
            pageModel = OrganizationRepository.list(contextBean.getUsername(), name, pageBean, sortBeans);
        } else {
            pageModel = OrganizationRepository.list(contextBean.getUsername(), pageBean, sortBeans);
        }

        List<NameValuePair> queryParameters = ResourceUtils.buildNameValuePairs(offset, limit, sortBeans);
        if (name != null) {
            queryParameters.add(new BasicNameValuePair("name", name));
        }

        try {
            return EntityToRepresentation.toRepresentation(
                    pageModel,
                    EntityToRepresentation::toRepresentation,
                    contextBean.getUriInfo(),
                    queryParameters
            );
        } catch (URISyntaxException e) {
            throw new InternalServerErrorException();
        }
    }

}

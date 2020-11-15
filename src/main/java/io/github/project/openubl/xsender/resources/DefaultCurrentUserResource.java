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
package io.github.project.openubl.xsender.resources;

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.managers.CompanyManager;
import io.github.project.openubl.xsender.models.ContextBean;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.List;

@Transactional
@ApplicationScoped
public class DefaultCurrentUserResource implements CurrentUserResource {

    @Context
    UriInfo uriInfo;

    @Inject
    UserIdentity userIdentity;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyManager companyManager;

    @Override
    public CompanyRepresentation createCompany(CompanyRepresentation rep) {
        if (companyRepository.findByName(rep.getName()).isPresent()) {
            throw new BadRequestException("Name already taken");
        }

        CompanyEntity company = companyManager.createCompany(userIdentity.getUsername(), rep);
        return EntityToRepresentation.toRepresentation(company);
    }

    public PageRepresentation<CompanyRepresentation> getCompanies(
            String name,
            Integer offset,
            Integer limit,
            List<String> sortBy
    ) {
        ContextBean contextBean = ContextBean.Builder.aContextBean()
                .withUsername(userIdentity.getUsername())
                .withUriInfo(uriInfo)
                .build();

        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, CompanyRepository.SORT_BY_FIELDS);

        PageModel<CompanyEntity> pageModel;
        if (name != null && !name.trim().isEmpty()) {
            pageModel = CompanyRepository.list(contextBean.getUsername(), name, pageBean, sortBeans);
        } else {
            pageModel = CompanyRepository.list(contextBean.getUsername(), pageBean, sortBeans);
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


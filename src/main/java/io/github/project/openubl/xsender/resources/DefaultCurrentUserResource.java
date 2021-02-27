/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * <p>
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.eclipse.org/legal/epl-2.0/
 * <p>
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
import io.github.project.openubl.xsender.models.*;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
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

    @Inject
    Event<CompanyEvent.Created> companyCreatedEvent;

    @Override
    public Response createCompany(CompanyRepresentation rep) {
        if (companyRepository.findByName(rep.getName()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Name already taken")
                    .build();
        }

        CompanyEntity companyEntity = companyManager.createCompany(userIdentity.getUsername(), rep);

        companyCreatedEvent.fire(new CompanyEvent.Created() {
            @Override
            public String getId() {
                return companyEntity.getId();
            }

            @Override
            public String getOwner() {
                return companyEntity.getOwner();
            }
        });

        return Response.ok()
                .entity(EntityToRepresentation.toRepresentation(companyEntity))
                .build();
    }

    public PageRepresentation<CompanyRepresentation> getCompanies(
            String filterText,
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
        if (filterText != null && !filterText.trim().isEmpty()) {
            pageModel = CompanyRepository.list(contextBean.getUsername(), filterText, pageBean, sortBeans);
        } else {
            pageModel = CompanyRepository.list(contextBean.getUsername(), pageBean, sortBeans);
        }

        List<NameValuePair> queryParameters = ResourceUtils.buildNameValuePairs(offset, limit, sortBeans);
        if (filterText != null) {
            queryParameters.add(new BasicNameValuePair("name", filterText));
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


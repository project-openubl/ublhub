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
package io.github.project.openubl.xsender.basic.resources;

import io.github.project.openubl.xsender.basic.Constants;
import io.github.project.openubl.xsender.core.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.core.idm.PageRepresentation;
import io.github.project.openubl.xsender.core.managers.CompanyManager;
import io.github.project.openubl.xsender.core.models.ContextBean;
import io.github.project.openubl.xsender.core.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.core.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.core.resources.CurrentUserResource;
import io.github.project.openubl.xsender.core.resources.bl.CompanyResourceBL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Transactional
@ApplicationScoped
public class BasicCurrentUserResource implements CurrentUserResource {

    @Context
    UriInfo uriInfo;

    @Inject
    CompanyResourceBL companyResourceBL;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyManager companyManager;

    public PageRepresentation<CompanyRepresentation> getCompanies(
            String name,
            Integer offset,
            Integer limit,
            List<String> sortBy
    ) {
        ContextBean contextBean = ContextBean.Builder.aContextBean()
                .withUsername(Constants.DEFAULT_USERNAME)
                .withUriInfo(uriInfo)
                .build();

        return companyResourceBL.listOrganizations(contextBean, name, offset, limit, sortBy);
    }

    @Override
    public CompanyRepresentation createCompany(CompanyRepresentation rep) {
        if (companyRepository.findByName(rep.getName()).isPresent()) {
            throw new BadRequestException("Name already taken");
        }

        CompanyEntity company = companyManager.createCompany(Constants.DEFAULT_USERNAME, rep);
        return EntityToRepresentation.toRepresentation(company);
    }

}


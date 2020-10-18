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
package io.github.project.openubl.xsender.authz.resources;

import io.github.project.openubl.xsender.core.idm.OrganizationRepresentation;
import io.github.project.openubl.xsender.core.idm.PageRepresentation;
import io.github.project.openubl.xsender.core.idm.RepositoryRepresentation;
import io.github.project.openubl.xsender.core.models.ContextBean;
import io.github.project.openubl.xsender.core.resources.AuthenticatedUserResource;
import io.github.project.openubl.xsender.core.resources.bl.OrganizationsResourceBL;
import io.quarkus.security.identity.SecurityIdentity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Transactional
@ApplicationScoped
public class AuthzAuthenticatedUserResource implements AuthenticatedUserResource {

    @Inject
    SecurityIdentity keycloakSecurityContext;

    @Context
    UriInfo uriInfo;

    @Inject
    OrganizationsResourceBL organizationsResourceBL;

    @Override
    public String initUser() {
        String username = keycloakSecurityContext.getPrincipal().getName();
        return username;
    }

    public PageRepresentation<OrganizationRepresentation> getCurrentUserOrganizations(
            String name,
            Integer offset,
            Integer limit,
            List<String> sortBy
    ) {
        String username = keycloakSecurityContext.getPrincipal().getName();
        ContextBean contextBean = ContextBean.Builder.aContextBean()
                .withUsername(username)
                .withUriInfo(uriInfo)
                .build();

        return organizationsResourceBL.listOrganizations(contextBean, name, offset, limit, sortBy);
    }

    public List<RepositoryRepresentation> getRepositories() {
        return new ArrayList<>();
    }

    public RepositoryRepresentation createRepository() {
        return new RepositoryRepresentation();
    }
}


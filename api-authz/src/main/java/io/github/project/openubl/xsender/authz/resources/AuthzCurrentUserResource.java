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

import io.github.project.openubl.xsender.core.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.core.idm.PageRepresentation;
import io.github.project.openubl.xsender.core.managers.CompanyManager;
import io.github.project.openubl.xsender.core.models.ContextBean;
import io.github.project.openubl.xsender.core.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.core.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.core.resources.CurrentUserResource;
import io.github.project.openubl.xsender.core.resources.bl.CompanyResourceBL;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.AuthorizationContext;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.ClientAuthorizationContext;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.*;

@Transactional
@ApplicationScoped
public class AuthzCurrentUserResource implements CurrentUserResource {

    @Context
    UriInfo uriInfo;

//    @Context
//    HttpServletRequest request;

    @Inject
    SecurityIdentity identity;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyManager companyManager;

    @Inject
    CompanyResourceBL companyResourceBL;

    private ContextBean getContextBean() {
        String username = identity.getPrincipal().getName();
        return ContextBean.Builder.aContextBean()
                .withUsername(username)
                .withUriInfo(uriInfo)
                .build();
    }

    @Override
    public PageRepresentation<CompanyRepresentation> getCompanies(String name, Integer offset, Integer limit, List<String> sortBy) {
        return companyResourceBL.listOrganizations(getContextBean(), name, offset, limit, sortBy);
    }

    public static final String SCOPE_COMPANY_READ = "company:read";
    public static final String SCOPE_COMPANY_WRITE = "company:write";
    public static final String SCOPE_COMPANY_ADMIN = "company:admin";

    @Override
    public CompanyRepresentation createCompany(CompanyRepresentation rep) {
        if (companyRepository.findByName(rep.getName()).isPresent()) {
            throw new BadRequestException("Name already taken");
        }

        ContextBean contextBean = getContextBean();
        CompanyEntity company = companyManager.createCompany(contextBean.getUsername(), rep);

        try {
            Set<ScopeRepresentation> scopes = new HashSet<>();

            scopes.add(new ScopeRepresentation(SCOPE_COMPANY_READ));
            scopes.add(new ScopeRepresentation(SCOPE_COMPANY_WRITE));
            scopes.add(new ScopeRepresentation(SCOPE_COMPANY_ADMIN));

            ResourceRepresentation resource = new ResourceRepresentation(company.getId(), scopes, "/companies/" + company.getId(), "xsender:company");

            resource.setOwner(contextBean.getUsername());
            resource.setOwnerManagedAccess(true);

            ResourceRepresentation response = getAuthzClient().protection().resource().create(resource);

//            album.setExternalId(response.getId());
        } catch (Exception e) {
            throw new RuntimeException("Could not register protected resource.", e);
        }

        return EntityToRepresentation.toRepresentation(company);
    }

    private AuthzClient getAuthzClient() {
        return getAuthorizationContext().getClient();
    }

    private ClientAuthorizationContext getAuthorizationContext() {
        AuthorizationContext authorizationContext = getKeycloakSecurityContext().getAuthorizationContext();
        return ClientAuthorizationContext.class.cast(authorizationContext);
    }

    private KeycloakSecurityContext getKeycloakSecurityContext() {
        Object attribute = identity.getAttribute(KeycloakSecurityContext.class.getName());
        Map<String, Object> attributes = identity.getAttributes();
        return KeycloakSecurityContext.class.cast(attribute);
    }

}


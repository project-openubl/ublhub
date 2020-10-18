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
package io.github.project.openubl.xsender.core.resources;

import io.github.project.openubl.xsender.core.idm.OrganizationRepresentation;
import io.github.project.openubl.xsender.core.idm.RepositoryRepresentation;
import io.github.project.openubl.xsender.core.idm.SunatCredentialsRepresentation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.util.List;

@Path(Paths.ORGS)
@Produces("application/json")
@Consumes("application/json")
public interface OrgResource {

    /**
     * Get an organization
     */
    @GET
    @Path("/{org}")
    OrganizationRepresentation getOrganization(@PathParam("org") @NotNull String org);

    /**
     * Update an org
     */
    @PATCH
    @Path("/{org}")
    OrganizationRepresentation updateOrganization(
            @PathParam("org") @NotNull String org,
            @NotNull @Valid OrganizationRepresentation rep
    );


    /**
     * Change SUNAT credentials or an organization
     */
    @PATCH
    @Path("/{org}/sunat-credentials")
    void updateCorporateSUNATCredentials(
            @PathParam("org") @NotNull String org,
            @NotNull @Valid SunatCredentialsRepresentation rep
    );

    /**
     * List organization repositories
     */
    @GET
    @Path("/{org}/repos")
    List<RepositoryRepresentation> listRepositories();

    @POST
    @Path("/{org}/repos")
    RepositoryRepresentation createRepositorie();

}


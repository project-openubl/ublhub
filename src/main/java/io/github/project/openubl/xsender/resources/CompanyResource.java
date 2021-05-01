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
package io.github.project.openubl.xsender.resources;

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/companies")
@Produces("application/json")
@Consumes("application/json")
public interface CompanyResource {

    /**
     * Get an organization
     */
    @GET
    @Path("/{company}")
    CompanyRepresentation getCompany(@PathParam("company") @NotNull String company);

    /**
     * Update an org
     */
    @PUT
    @Path("/{company}")
    CompanyRepresentation updateCompany(
            @PathParam("company") @NotNull String company,
            @NotNull CompanyRepresentation rep
    );

    @DELETE
    @Path("/{company}")
    void deleteCompany(@PathParam("company") @NotNull String company);

    /**
     * Change SUNAT credentials of a company
     */
    @PUT
    @Path("/{company}/sunat-credentials")
    void updateCompanySUNATCredentials(
            @PathParam("company") @NotNull String company,
            @NotNull @Valid SunatCredentialsRepresentation rep
    );

    /**
     * List documents
     */
    @GET
    @Path("/{company}/documents")
    PageRepresentation<DocumentRepresentation> listDocuments(
            @PathParam("company") @NotNull String company,
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("createdOn:desc") List<String> sortBy
    );

    /**
     * Create document
     */
    @POST
    @Path("/{company}/documents")
    @Consumes("multipart/form-data")
    Response createDocument(
            @PathParam("company") @NotNull String company,
            MultipartFormDataInput input
    );

    @GET
    @Path("/{company}/documents/{documentId}")
    DocumentRepresentation getDocument(
            @PathParam("company") @NotNull String company,
            @PathParam("documentId") @NotNull String documentId
    );

    @GET
    @Path("/{company}/documents/{documentId}/file")
    Response getDocumentFile(
            @PathParam("company") @NotNull String company,
            @PathParam("documentId") @NotNull String documentId
    );

    @GET
    @Path("/{company}/documents/{documentId}/file-link")
    String getDocumentFileLink(
            @PathParam("company") @NotNull String company,
            @PathParam("documentId") @NotNull String documentId
    );

    @GET
    @Path("/{company}/documents/{documentId}/cdr")
    Response getDocumentCDR(
            @PathParam("company") @NotNull String company,
            @PathParam("documentId") @NotNull String documentId
    );

    @GET
    @Path("/{company}/documents/{documentId}/cdr-link")
    String getDocumentCDRLink(
            @PathParam("company") @NotNull String company,
            @PathParam("documentId") @NotNull String documentId
    );

}


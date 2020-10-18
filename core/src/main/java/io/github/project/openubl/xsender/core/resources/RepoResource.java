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

import io.github.project.openubl.xsender.core.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.core.idm.RepositoryRepresentation;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/repos")
@Produces("application/json")
@Consumes("application/json")
public interface RepoResource {

    /**
     * Get a repository
     */
    @GET
    @Path("/{owner}/{repo}")
    RepositoryRepresentation getRepository(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo
    );

    /**
     * Update a repository
     */
    @PATCH
    @Path("/{owner}/{repo}")
    RepositoryRepresentation updateRepository(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo,
            @NotNull @Valid RepositoryRepresentation rep
    );


    /**
     * Delete a repository
     */
    @DELETE
    @Path("/{owner}/{repo}")
    void deleteRepository(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo
    );

    /**
     * Create document
     */
    @POST
    @Path("/{owner}/{repo}/docs")
    @Consumes("multipart/form-data")
    Response createDocument(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo,
            MultipartFormDataInput input
    );

    @GET
    @Path("/{owner}/{repo}/docs")
    List<DocumentRepresentation> listDocuments(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo
    );

    @GET
    @Path("/{owner}/{repo}/docs/{docId}")
    DocumentRepresentation getDocument(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo
    );

    @GET
    @Path("/{owner}/{repo}/docs/{docId}/file")
    Response getDocumentFile(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo,
            @PathParam("docId") @NotNull String docId
    );

    @GET
    @Path("/{owner}/{repo}/docs/{docId}/file-link")
    String getDocumentFileLink(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo,
            @PathParam("docId") @NotNull String docId
    );

    @GET
    @Path("/{owner}/{repo}/docs/{docId}/cdr")
    Response getDocumentCDR(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo,
            @PathParam("docId") @NotNull String docId
    );

    @GET
    @Path("/{owner}/{repo}/docs/{docId}/cdr-link")
    String getDocumentCDRLink(
            @PathParam("owner") @NotNull String owner,
            @PathParam("repo") @NotNull String repo,
            @PathParam("docId") @NotNull String docId
    );
}


/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.resources;

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

import io.github.project.openubl.ublhub.files.FilesMutiny;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class DocumentFileResource {

    @Inject
    FilesMutiny filesMutiny;

    @Inject
    UBLDocumentRepository documentRepository;

    @GET
    @Path("/{namespaceId}/document-files/{documentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Uni<Response> getDocumentFile(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("documentId") @NotNull String documentId,
            @QueryParam("requestedFile") @DefaultValue("ubl") String requestedFile,
            @QueryParam("requestedFormat") @DefaultValue("zip") String requestedFormat
    ) {
        return Panache
                .withTransaction(() -> documentRepository.findById(namespaceId, documentId))
                .onItem().ifNotNull().transformToUni(documentEntity -> Uni.createFrom().item(documentEntity.getXmlFileId())
                        .onItem().ifNotNull().transformToUni(xmlFileId -> {
                            String fileId;
                            if (requestedFile.equals("ubl")) {
                                fileId = documentEntity.getXmlFileId();
                            } else {
                                fileId = documentEntity.getCdrFileId();
                            }

                            boolean isZipFormatRequested = requestedFormat.equals("zip");

                            Uni<byte[]> bytesUni;
                            if (isZipFormatRequested) {
                                bytesUni = filesMutiny.getFileAsBytesWithoutUnzipping(fileId);
                            } else {
                                bytesUni = filesMutiny.getFileAsBytesAfterUnzip(fileId);
                            }

                            String filename = documentEntity.getXmlData().getSerieNumero() + (isZipFormatRequested ? ".zip" : ".xml");
                            String mediaType = isZipFormatRequested ? "application/zip" : MediaType.APPLICATION_XML;

                            return bytesUni.map(bytes -> Response.ok(bytes, mediaType)
                                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                    .build()
                            );
                        })
                        .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build())
                )
                .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

}



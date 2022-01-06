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
import io.github.project.openubl.ublhub.models.jpa.NamespaceRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
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
    NamespaceRepository namespaceRepository;

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
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> documentRepository.findById(namespaceEntity, documentId))
                )
                .onItem().ifNotNull().transformToUni(documentEntity -> {
                            String fileId;
                            if (requestedFile.equals("ubl")) {
                                fileId = documentEntity.storageFile;
                            } else {
                                fileId = documentEntity.storageCdr;
                            }

                            boolean isZipFormatRequested = requestedFormat.equals("zip");

                            Uni<byte[]> bytesUni;
                            if (isZipFormatRequested) {
                                bytesUni = filesMutiny.getFileAsBytesWithoutUnzipping(fileId);
                            } else {
                                bytesUni = filesMutiny.getFileAsBytesAfterUnzip(fileId);
                            }

                            return bytesUni.map(bytes -> Response.ok(bytes, isZipFormatRequested ? "application/zip" : MediaType.APPLICATION_XML)
                                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentEntity.documentID + (isZipFormatRequested ? ".zip" : ".xml") + "\"")
                                    .build()
                            );
                        }
                )
                .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

}



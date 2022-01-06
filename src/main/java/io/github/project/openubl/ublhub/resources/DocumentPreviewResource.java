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

import io.github.project.openubl.ublhub.builder.XMLBuilderManager;
import io.github.project.openubl.ublhub.idm.input.InputTemplateRepresentation;
import io.github.project.openubl.ublhub.models.jpa.NamespaceRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class DocumentPreviewResource {

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    XMLBuilderManager xmlBuilderManager;

    @POST
    @Path("/{namespaceId}/document-preview")
    public Uni<Response> createDocumentPreview(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull JsonObject jsonObject
    ) {
        InputTemplateRepresentation inputTemplate = jsonObject.mapTo(InputTemplateRepresentation.class);
        JsonObject documentJsonObject = jsonObject.getJsonObject("spec").getJsonObject("document");
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> xmlBuilderManager.createXMLString(namespaceEntity, inputTemplate, documentJsonObject, true)
                                // Response
                                .map(xmlString -> Response
                                        .status(Response.Status.OK)
                                        .entity(xmlString)
                                        .build()
                                )
                        )

                        .onItem().ifNull().continueWith(Response.ok()
                                .status(Response.Status.NOT_FOUND)::build
                        )

                        .onFailure(throwable -> throwable instanceof ConstraintViolationException).recoverWithItem(throwable -> Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity(throwable.getMessage()).build()
                        )
                );
    }

}



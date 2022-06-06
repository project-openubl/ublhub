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

import io.github.project.openubl.ublhub.idm.input.IDGeneratorRepresentation;
import io.github.project.openubl.ublhub.models.jpa.NamespaceRepository;
import io.github.project.openubl.ublhub.resources.validation.JSONValidatorManager;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import io.github.project.openubl.ublhub.ubl.builder.xmlgenerator.XMLGeneratorManager;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
    XMLGeneratorManager xmlGeneratorManager;

    @Inject
    JSONValidatorManager jsonManager;

    @POST
    @Path("/{namespaceId}/document-preview")
    public Uni<Response> createDocumentPreview(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull JsonObject json
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository
                        .findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> jsonManager
                                .getUniInputTemplateFromJSON(json)
                                .onItem().ifNotNull().transformToUni(input -> Uni.createFrom()
                                        .item(() -> {
                                            // Always set generator to NONE in case of a Preview
                                            IDGeneratorRepresentation idGeneratorRepresentation = new IDGeneratorRepresentation();
                                            idGeneratorRepresentation.setName(IDGeneratorType.none);
                                            return idGeneratorRepresentation;
                                        })
                                        .invoke(idGeneratorRepresentation -> input.getSpec().setIdGenerator(idGeneratorRepresentation))
                                        .chain(() -> xmlGeneratorManager.createXMLString(namespaceEntity, input))
                                        .map(xmlString -> Response
                                                .status(Response.Status.OK)
                                                .entity(xmlString)
                                                .build()
                                        )
                                )
                                .onItem().ifNull().continueWith(Response.ok().status(Response.Status.BAD_REQUEST)::build)
                        )
                        .onItem().ifNull().continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build)
                );
    }

}



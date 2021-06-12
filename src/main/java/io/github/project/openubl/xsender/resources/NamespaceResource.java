package io.github.project.openubl.xsender.resources;

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

import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.security.UserIdentity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class NamespaceResource {

    private static final Logger LOG = Logger.getLogger(NamespaceResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @GET
    @Path("/{namespaceId}")
    public Uni<Response> getNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache.withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()))
                .onItem().ifNotNull().transform(entity -> Response.ok().entity(EntityToRepresentation.toRepresentation(entity)).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @PUT
    @Path("/{namespaceId}")
    public Uni<Response> updateNamespace(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull NamespaceRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().invoke(namespaceEntity -> {
                            namespaceEntity.name = rep.getName();
                            namespaceEntity.description = rep.getDescription();
                        })
                )
                .onItem().ifNotNull().transform(entity -> Response.ok().entity(EntityToRepresentation.toRepresentation(entity)).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @DELETE
    @Path("/{namespaceId}")
    public Uni<Response> deleteNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().call(PanacheEntityBase::delete)
                )
                .onItem().ifNotNull().transform(entity -> Response.status(Response.Status.NO_CONTENT).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

}



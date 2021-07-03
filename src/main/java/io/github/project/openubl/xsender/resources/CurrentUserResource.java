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

import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CurrentUserResource {

    private static final Logger LOG = Logger.getLogger(CurrentUserResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @POST
    @Path("/namespaces")
    public Uni<Response> createNameSpace(@NotNull @Valid NamespaceRepresentation rep) {
        return namespaceRepository.findByName(rep.getName())
                .onItem().ifNotNull().transform(entity -> Response.status(Response.Status.CONFLICT).build())
                .onItem().ifNull().switchTo(() -> Panache
                        .withTransaction(() -> {
                            final NamespaceEntity namespaceEntity = new NamespaceEntity();
                            namespaceEntity.id = UUID.randomUUID().toString();
                            namespaceEntity.name = rep.getName();
                            namespaceEntity.description = rep.getDescription();
                            namespaceEntity.createdOn = new Date();
                            namespaceEntity.owner = userIdentity.getUsername();

                            return namespaceRepository.persist(namespaceEntity).map(unused -> namespaceEntity);
                        })
                        .map(namespaceEntity -> Response.ok()
                                .entity(EntityToRepresentation.toRepresentation(namespaceEntity))
                                .build()
                        )
                );
    }

    @GET
    @Path("/namespaces")
    public Uni<PageRepresentation<NamespaceRepresentation>> getNamespaces(
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("createdOn:desc") List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, NamespaceRepository.SORT_BY_FIELDS);

        Uni<Tuple2<List<NamespaceEntity>, Long>> searchResult;
        if (filterText != null && !filterText.trim().isEmpty()) {
            searchResult = Panache.withTransaction(namespaceRepository.list(userIdentity.getUsername(), filterText, pageBean, sortBeans)::asTuple);
        } else {
            searchResult = Panache.withTransaction(namespaceRepository.list(userIdentity.getUsername(), pageBean, sortBeans)::asTuple);
        }

        return searchResult.map(
                tuple2 -> EntityToRepresentation.toRepresentation(tuple2.getItem1(), tuple2.getItem2(), EntityToRepresentation::toRepresentation)
        );
    }

}


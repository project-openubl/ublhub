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

import io.github.project.openubl.ublhub.idm.CompanyRepresentation;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.NamespaceRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.utils.EntityToRepresentation;
import io.github.project.openubl.ublhub.models.utils.RepresentationToEntity;
import io.github.project.openubl.ublhub.resources.utils.ResourceUtils;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class CompanyResource {

    @Context
    UriInfo uriInfo;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

    @GET
    @Path("/{namespaceId}/companies/{companyId}")
    public Uni<Response> getCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        return Panache
                .withTransaction(() -> companyRepository.findById(namespaceId, companyId))
                .onItem().ifNotNull().transform(EntityToRepresentation::toRepresentation)
                .map(result -> {
                    if (result != null) {
                        return Response.ok().entity(result).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });
    }

    @POST
    @Path("/{namespaceId}/companies")
    public Uni<Response> createCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull @Valid CompanyRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> companyRepository
                                .findByRuc(namespaceEntity, rep.getRuc())
                                .onItem().ifNotNull().transform(companyEntity -> Response.status(Response.Status.CONFLICT).build())
                                .onItem().ifNull().switchTo(() -> {
                                    CompanyEntity companyEntity = new CompanyEntity();
                                    companyEntity.id = UUID.randomUUID().toString();
                                    companyEntity.namespace = namespaceEntity;

                                    RepresentationToEntity.assign(companyEntity, rep);
                                    return companyEntity.<CompanyEntity>persist()
                                            .map(EntityToRepresentation::toRepresentation)
                                            .map(result -> Response.ok().entity(result).build());
                                })
                        )
                        .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build)
                );
    }

    @PUT
    @Path("/{namespaceId}/companies/{companyId}")
    public Uni<Response> updateCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId,
            @NotNull CompanyRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> companyRepository.findById(namespaceId, companyId)
                        .onItem().ifNotNull().invoke(companyEntity -> RepresentationToEntity.assign(companyEntity, rep))
                )
                .onItem().ifNotNull().transform(EntityToRepresentation::toRepresentation)
                .map(result -> {
                    if (result != null) {
                        return Response.status(Response.Status.OK).entity(result).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });
    }

    @DELETE
    @Path("/{namespaceId}/companies/{companyId}")
    public Uni<Response> deleteCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        return Panache.withTransaction(() -> companyRepository.deleteByNamespaceIdAndId(namespaceId, companyId))
                .map(result -> Response
                        .status(result ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                        .build()
                );
    }

    @GET
    @Path("/{namespaceId}/companies")
    public Uni<Response> getCompanies(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("created:desc") List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, CompanyRepository.SORT_BY_FIELDS);

        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> {
                            UniAndGroup2<List<CompanyEntity>, Long> searchResult;
                            if (filterText != null && !filterText.trim().isEmpty()) {
                                searchResult = companyRepository.list(namespaceEntity, filterText, pageBean, sortBeans);
                            } else {
                                searchResult = companyRepository.list(namespaceEntity, pageBean, sortBeans);
                            }
                            return searchResult.asTuple();
                        })
                )
                .onItem().ifNotNull().transform(tuple2 -> Response.ok()
                        .entity(EntityToRepresentation.toRepresentation(
                                tuple2.getItem1(),
                                tuple2.getItem2(),
                                EntityToRepresentation::toRepresentation
                        ))
                        .build()
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

}



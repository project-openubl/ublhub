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
package io.github.project.openubl.ublhub.resources;

import io.github.project.openubl.ublhub.idm.NamespaceRepresentation;
import io.github.project.openubl.ublhub.idm.PageRepresentation;
import io.github.project.openubl.ublhub.keys.DefaultKeyProviders;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.NamespaceRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;
import io.github.project.openubl.ublhub.models.utils.EntityToRepresentation;
import io.github.project.openubl.ublhub.resources.utils.ResourceUtils;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
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
    NamespaceRepository namespaceRepository;

    @Inject
    DefaultKeyProviders defaultKeyProviders;

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

                            namespaceEntity.sunat = new SunatEntity();
                            namespaceEntity.sunat.sunatUsername = rep.getCredentials().getUsername();
                            namespaceEntity.sunat.sunatPassword = rep.getCredentials().getPassword();
                            namespaceEntity.sunat.sunatUrlFactura = rep.getWebServices().getFactura();
                            namespaceEntity.sunat.sunatUrlGuiaRemision = rep.getWebServices().getGuia();
                            namespaceEntity.sunat.sunatUrlPercepcionRetencion = rep.getWebServices().getRetenciones();

                            return namespaceRepository.persist(namespaceEntity)
                                    .chain(namespace -> defaultKeyProviders.createProviders(namespace))
                                    .map(unused -> namespaceEntity);
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

        if (filterText != null && !filterText.trim().isEmpty()) {
            return Panache.withTransaction(() -> namespaceRepository
                    .list(filterText, pageBean, sortBeans)
                    .asTuple()
                    .map(tuple2 -> EntityToRepresentation.toRepresentation(tuple2.getItem1(), tuple2.getItem2(), EntityToRepresentation::toRepresentation))
            );
        } else {
            return Panache.withTransaction(() -> namespaceRepository
                    .list(pageBean, sortBeans)
                    .asTuple()
                    .map(tuple2 -> EntityToRepresentation.toRepresentation(tuple2.getItem1(), tuple2.getItem2(), EntityToRepresentation::toRepresentation))
            );
        }
    }

}

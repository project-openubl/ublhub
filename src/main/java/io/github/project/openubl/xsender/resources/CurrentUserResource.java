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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.outbox.quarkus.ExportedEvent;
import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.kafka.idm.NamespaceCrudEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityEventProducer;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.kafka.utils.EventEntityToRepresentation;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/user")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class CurrentUserResource {

    private static final Logger LOG = Logger.getLogger(CurrentUserResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/namespaces")
    public Response createNameSpace(@NotNull @Valid NamespaceRepresentation rep) {
        if (namespaceRepository.findByName(rep.getName()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Name already taken")
                    .build();
        }

        NamespaceEntity namespaceEntity = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName(rep.getName())
                .withDescription(rep.getDescription())
                .withCreatedOn(new Date())
                .withOwner(userIdentity.getUsername())
                .build();

        namespaceRepository.persist(namespaceEntity);

        try {
            NamespaceCrudEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(namespaceEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new EntityEventProducer(namespaceEntity.getId(), EntityType.namespace, EventType.CREATED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }

        return Response.ok()
                .entity(EntityToRepresentation.toRepresentation(namespaceEntity))
                .build();
    }

    @GET
    @Path("/namespaces")
    public PageRepresentation<NamespaceRepresentation> getNamespaces(
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("createdOn:desc") List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, NamespaceRepository.SORT_BY_FIELDS);

        PageModel<NamespaceEntity> pageModel;
        if (filterText != null && !filterText.trim().isEmpty()) {
            pageModel = namespaceRepository.list(userIdentity.getUsername(), filterText, pageBean, sortBeans);
        } else {
            pageModel = namespaceRepository.list(userIdentity.getUsername(), pageBean, sortBeans);
        }

        return EntityToRepresentation.toRepresentation(pageModel, EntityToRepresentation::toRepresentation);
    }

}


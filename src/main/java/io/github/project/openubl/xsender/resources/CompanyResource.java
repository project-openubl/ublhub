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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.outbox.quarkus.ExportedEvent;
import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.kafka.idm.CompanyCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityEventProducer;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.kafka.utils.EventEntityToRepresentation;
import io.github.project.openubl.xsender.managers.CompanyManager;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
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

@Path("/namespaces/{namespace}/companies")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class CompanyResource {

    private static final Logger LOG = Logger.getLogger(CompanyResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyManager companyManager;

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/")
    public Response createCompany(
            @PathParam("namespace") @NotNull String namespace,
            @NotNull @Valid CompanyRepresentation rep
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByNameAndOwner(namespace, userIdentity.getUsername()).orElseThrow(NotFoundException::new);

        if (companyRepository.findByRuc(namespaceEntity, rep.getRuc()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("RUC already taken")
                    .build();
        }

        CompanyEntity companyEntity = companyManager.createCompany(namespaceEntity, rep);

        try {
            CompanyCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(companyEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            this.event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.company, EventType.CREATED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }

        return Response.ok()
                .entity(EntityToRepresentation.toRepresentation(companyEntity))
                .build();
    }

    @GET
    @Path("/{ruc}")
    public CompanyRepresentation getCompany(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("ruc") @NotNull String ruc
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByNameAndOwner(namespace, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findByRuc(namespaceEntity, ruc).orElseThrow(NotFoundException::new);
        return EntityToRepresentation.toRepresentation(companyEntity);
    }

    @PUT
    @Path("/{ruc}")
    public CompanyRepresentation updateCompany(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("ruc") @NotNull String ruc,
            @NotNull CompanyRepresentation rep
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByNameAndOwner(namespace, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findByRuc(namespaceEntity, ruc).orElseThrow(NotFoundException::new);

        companyEntity = companyManager.updateCompany(rep, companyEntity);

        try {
            CompanyCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(companyEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.company, EventType.UPDATED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }

        return EntityToRepresentation.toRepresentation(companyEntity);
    }

    @DELETE
    @Path("/{ruc}")
    public void deleteNamespace(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("ruc") @NotNull String ruc
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByNameAndOwner(namespace, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findByRuc(namespaceEntity, ruc).orElseThrow(NotFoundException::new);

        companyRepository.delete(companyEntity);

        try {
            CompanyCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(companyEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.company, EventType.DELETED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }
    }

}



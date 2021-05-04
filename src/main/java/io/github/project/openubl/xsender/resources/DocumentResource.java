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
import io.github.project.openubl.xsender.exceptions.StorageException;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.idm.ErrorRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.kafka.idm.UBLDocumentCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityEventProducer;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.kafka.utils.EventEntityToRepresentation;
import io.github.project.openubl.xsender.managers.CompanyManager;
import io.github.project.openubl.xsender.managers.DocumentsManager;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/namespaces/{namespace}/documents")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class DocumentResource {

    private static final Logger LOG = Logger.getLogger(DocumentResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    DocumentsManager documentsManager;

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/upload")
    public Response uploadXML(
            @PathParam("namespace") @NotNull String namespace,
            MultipartFormDataInput input
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByNameAndOwner(namespace, userIdentity.getUsername()).orElseThrow(NotFoundException::new);

        // Extract file

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<InputPart> fileInputParts = uploadForm.get("file");

        if (fileInputParts == null) {
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        byte[] xmlFile = null;

        try {
            for (InputPart inputPart : fileInputParts) {
                InputStream fileInputStream = inputPart.getBody(InputStream.class, null);
                xmlFile = IOUtils.toByteArray(fileInputStream);
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not extract required data from upload/form");
        }

        if (xmlFile == null || xmlFile.length == 0) {
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is empty");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        UBLDocumentEntity documentEntity;
        try {
            documentEntity = documentsManager.createDocumentAndScheduleDelivery(namespaceEntity, xmlFile);

            try {
                UBLDocumentCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(documentEntity);
                String eventPayload = objectMapper.writeValueAsString(eventRep);
                event.fire(new EntityEventProducer(documentEntity.getId(), EntityType.document, EventType.CREATED, eventPayload));
            } catch (JsonProcessingException e) {
                LOG.error(e);
            }
        } catch (StorageException e) {
            LOG.error(e);
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

        return Response.status(Response.Status.OK)
                .entity(EntityToRepresentation.toRepresentation(documentEntity))
                .build();
    }

    @GET
    @Path("/")
    public PageRepresentation<DocumentRepresentation> getDocuments(
            @PathParam("namespace") @NotNull String namespace,
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("createdOn:desc") List<String> sortBy
    ) {
        return null;
    }


    @GET
    @Path("/{documentId}")
    public DocumentRepresentation getDocument(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("documentId") @NotNull String documentId
    ) {
        return null;
    }

    @GET
    @Path("/{documentId}/file")
    public Response getDocumentFile(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("documentId") @NotNull String documentId
    ) {
        return null;
    }

    @GET
    @Path("/{documentId}/file-link")
    public String getDocumentFileLink(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("documentId") @NotNull String documentId
    ) {
        return null;
    }

    @GET
    @Path("/{documentId}/cdr")
    public Response getDocumentCdr(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("documentId") @NotNull String documentId
    ) {
        return null;
    }

    @GET
    @Path("/{documentId}/cdr-link")
    public String getDocumentCdrLink(
            @PathParam("namespace") @NotNull String namespace,
            @PathParam("documentId") @NotNull String documentId
    ) {
        return null;
    }

}



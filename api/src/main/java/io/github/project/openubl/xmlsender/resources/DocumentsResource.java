/**
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
package io.github.project.openubl.xmlsender.resources;

import io.github.project.openubl.xmlsender.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xmlsender.exceptions.StorageException;
import io.github.project.openubl.xmlsender.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xmlsender.idm.DocumentRepresentation;
import io.github.project.openubl.xmlsender.idm.ErrorRepresentation;
import io.github.project.openubl.xmlsender.managers.DocumentsManager;
import io.github.project.openubl.xmlsender.files.FilesManager;
import io.github.project.openubl.xmlsender.models.jpa.DocumentRepository;
import io.github.project.openubl.xmlsender.models.jpa.entities.DocumentEntity;
import io.github.project.openubl.xmlsender.models.utils.EntityToRepresentation;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/documents")
@Produces("application/json")
@Transactional
@ApplicationScoped
public class DocumentsResource {

    private static final Logger LOG = Logger.getLogger(DocumentsResource.class);

    @Inject
    DocumentRepository documentRepository;

    @Inject
    DocumentsManager documentsManager;

    @Inject
    FilesManager filesManager;

    @POST
    @Consumes("multipart/form-data")
    public Response createDocument(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<InputPart> fileInputParts = uploadForm.get("file");
        List<InputPart> usernameInputParts = uploadForm.get("username");
        List<InputPart> passwordInputParts = uploadForm.get("password");
        List<InputPart> customIdInputParts = uploadForm.get("customId");

        if (fileInputParts == null) {
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        byte[] xmlFile = null;
        String username = null;
        String password = null;
        String customId = null;

        try {
            for (InputPart inputPart : fileInputParts) {
                InputStream fileInputStream = inputPart.getBody(InputStream.class, null);
                xmlFile = IOUtils.toByteArray(fileInputStream);
            }

            if (usernameInputParts != null) {
                for (InputPart inputPart : usernameInputParts) {
                    username = inputPart.getBodyAsString();
                }
            }

            if (passwordInputParts != null) {
                for (InputPart inputPart : passwordInputParts) {
                    password = inputPart.getBodyAsString();
                }
            }

            if (customIdInputParts != null) {
                for (InputPart inputPart : customIdInputParts) {
                    customId = inputPart.getBodyAsString();
                }
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not extract required data from upload/form");
        }

        if (xmlFile == null || xmlFile.length == 0) {
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is empty");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        DocumentEntity documentEntity;
        try {
            documentEntity = documentsManager.createDocumentAndScheduleDelivery(xmlFile, username, password, customId);
        } catch (InvalidXMLFileException e) {
            LOG.error(e);
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is not a valid XML file or is corrupted");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (UnsupportedDocumentTypeException e) {
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
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
    @Path("/{id}")
    @Produces("application/json")
    public DocumentRepresentation getDocument(@PathParam("id") Long id) {
        DocumentEntity documentEntity = documentRepository.findById(id);
        if (documentEntity == null) {
            throw new NotFoundException();
        }

        return EntityToRepresentation.toRepresentation(documentEntity);
    }

    @GET
    @Path("/{id}/file")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentFile(@PathParam("id") Long id) {
        DocumentEntity documentEntity = documentRepository.findById(id);
        if (documentEntity == null) {
            throw new NotFoundException();
        }

        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.fileID);
        return Response.ok(file)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentEntity.filenameWithoutExtension + ".xml" + "\""
                )
                .build();
    }

    @GET
    @Path("/{id}/file-link")
    @Produces("application/json")
    public String getDocumentFileLink(@PathParam("id") Long id) {
        DocumentEntity documentEntity = documentRepository.findById(id);
        if (documentEntity == null) {
            throw new NotFoundException();
        }

        return filesManager.getFileLink(documentEntity.fileID);
    }

    @GET
    @Path("/{id}/cdr")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDocumentCdr(@PathParam("id") Long id) {
        DocumentEntity documentEntity = documentRepository.findById(id);
        if (documentEntity == null) {
            throw new NotFoundException();
        }
        if (documentEntity.cdrID == null) {
            throw new NotFoundException();
        }

        byte[] file = filesManager.getFileAsBytesWithoutUnzipping(documentEntity.cdrID);
        return Response.ok(file)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentEntity.filenameWithoutExtension + ".zip" + "\""
                )
                .build();
    }

    @GET
    @Path("/{id}/cdr-link")
    @Produces("application/json")
    public String getDocumentCdrLink(@PathParam("id") Long id) {
        DocumentEntity documentEntity = documentRepository.findById(id);
        if (documentEntity == null) {
            throw new NotFoundException();
        }
        if (documentEntity.cdrID == null) {
            throw new NotFoundException();
        }

        return filesManager.getFileLink(documentEntity.cdrID);
    }
}


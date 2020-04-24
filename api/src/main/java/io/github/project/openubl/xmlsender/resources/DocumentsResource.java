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
import io.github.project.openubl.xmlsender.managers.DocumentsManager;
import io.github.project.openubl.xmlsender.models.FileDeliveryStatusType;
import io.github.project.openubl.xmlsender.models.imd.ErrorRepresentation;
import io.github.project.openubl.xmlsender.models.jpa.entities.FileDeliveryEntity;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/documents")
@Produces("application/json")
public class DocumentsResource {

    private static final Logger LOG = Logger.getLogger(DocumentsResource.class);

    @Inject
    DocumentsManager documentsManager;

    @POST
    @Path("/xml/send")
    @Consumes("multipart/form-data")
    public Response sendXML(MultipartFormDataInput input) {
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

        FileDeliveryEntity fileDeliveryEntity;
        try {
            fileDeliveryEntity = documentsManager.createFileDeliveryAndSchedule(xmlFile, username, password, customId);
        } catch (InvalidXMLFileException e) {
            LOG.error(e);
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is not a valid XML file or is corrupted");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (UnsupportedDocumentTypeException e) {
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (StorageException e) {
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

        Response.Status status = fileDeliveryEntity.deliveryStatus.equals(FileDeliveryStatusType.SCHEDULED_TO_DELIVER)
                ? Response.Status.OK
                : Response.Status.INTERNAL_SERVER_ERROR;
        return Response.status(status).entity(fileDeliveryEntity).build();
    }

}


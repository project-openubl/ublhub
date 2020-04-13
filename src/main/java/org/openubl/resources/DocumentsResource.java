package org.openubl.resources;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.openubl.exceptions.InvalidXMLFileException;
import org.openubl.exceptions.UnsupportedDocumentTypeException;
import org.openubl.models.FileDeliveryStatusType;
import org.openubl.models.imd.ErrorRepresentation;
import org.openubl.models.jpa.entities.FileDeliveryEntity;
import org.openubl.providers.DocumentsManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

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

        if (fileInputParts == null || usernameInputParts == null || passwordInputParts == null) {
            ErrorRepresentation error = new ErrorRepresentation("Invalid request. Request must include file, username, and password");
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

            for (InputPart inputPart : usernameInputParts) {
                username = inputPart.getBodyAsString();
            }

            for (InputPart inputPart : passwordInputParts) {
                password = inputPart.getBodyAsString();
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
            ErrorRepresentation error = new ErrorRepresentation("Invalid file or corrupted");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        FileDeliveryEntity fileDeliveryEntity;
        try {
            fileDeliveryEntity = documentsManager.sendFile(xmlFile, username, password, customId);
        } catch (InvalidXMLFileException e) {
            LOG.error(e);
            ErrorRepresentation error = new ErrorRepresentation("File is not an XML or is corrupted");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (UnsupportedDocumentTypeException e) {
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        Response.Status status = fileDeliveryEntity.deliveryStatus.equals(FileDeliveryStatusType.SCHEDULED_TO_DELIVER)
                ? Response.Status.OK
                : Response.Status.INTERNAL_SERVER_ERROR;
        return Response.status(status).entity(fileDeliveryEntity).build();
    }

}


package org.openubl.resources;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.openubl.exceptions.InvalidXMLFileException;
import org.openubl.exceptions.StorageException;
import org.openubl.exceptions.UnsupportedDocumentTypeException;
import org.openubl.managers.DocumentsManager;
import org.openubl.models.FileDeliveryStatusType;
import org.openubl.models.imd.ErrorRepresentation;
import org.openubl.models.jpa.entities.FileDeliveryEntity;

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
        List<InputPart> customIdInputParts = uploadForm.get("customId");

        if (fileInputParts == null) {
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        byte[] xmlFile = null;
        String customId = null;

        try {
            for (InputPart inputPart : fileInputParts) {
                InputStream fileInputStream = inputPart.getBody(InputStream.class, null);
                xmlFile = IOUtils.toByteArray(fileInputStream);
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
            fileDeliveryEntity = documentsManager.createFileDeliveryAndSchedule(xmlFile, customId);
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


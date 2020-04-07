package org.openubl.resources;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.openubl.exceptions.InvalidXMLFileException;
import org.openubl.models.jpa.entities.FileDeliveryEntity;
import org.openubl.providers.SendFileMessageProvider;

import javax.inject.Inject;
import javax.jms.JMSException;
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
    SendFileMessageProvider sendFileMessageProvider;

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
            return Response.status(Response.Status.BAD_REQUEST).entity("You should send: file, username, password").build();
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

            for (InputPart inputPart : customIdInputParts) {
                customId = inputPart.getBodyAsString();
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not extract required data from upload/form");
        }

        if (xmlFile == null || xmlFile.length == 0) {
            throw new BadRequestException("Invalid file");
        }

        FileDeliveryEntity fileDeliveryEntity;
        try {
            fileDeliveryEntity = sendFileMessageProvider.sendFile(xmlFile, username, password, customId);
        } catch (InvalidXMLFileException e) {
            LOG.error("File is not an XML or is corrupted");
            throw new BadRequestException("File is not an XML or is corrupted");
        } catch (JMSException e) {
            LOG.error(e);
            throw new InternalServerErrorException("Error saving message in Broker");
        }

        return Response.status(201).entity(fileDeliveryEntity).build();
    }

}


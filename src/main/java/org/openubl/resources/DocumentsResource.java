package org.openubl.resources;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.openubl.providers.SendFileProvider;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/documents")
@Produces("application/json")
public class DocumentsResource {

    @Inject
    SendFileProvider sendFileProvider;

    @POST
    @Path("/xml/send")
    @Consumes("multipart/form-data")
    public Response sendXML(MultipartFormDataInput input) throws SAXException, ParserConfigurationException, IOException {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<InputPart> fileInputParts = uploadForm.get("file");
        List<InputPart> usernameInputParts = uploadForm.get("username");
        List<InputPart> passwordInputParts = uploadForm.get("password");

        byte[] xmlFile = null;
        String username = null;
        String password = null;

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
        } catch (IOException e) {
            throw new BadRequestException("Could not extract required data from upload/form");
        }

        sendFileProvider.sendFile(xmlFile, username, password);
        return Response.status(201).build();
    }

}


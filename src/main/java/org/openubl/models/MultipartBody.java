package org.openubl.models;

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class MultipartBody {

    @FormParam("cdr")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream crd;

    @FormParam("status")
    @PartType(MediaType.TEXT_PLAIN)
    public String status;

    @FormParam("code")
    @PartType(MediaType.TEXT_PLAIN)
    public String code;

    @FormParam("description")
    @PartType(MediaType.TEXT_PLAIN)
    public String description;

    @FormParam("ticket")
    @PartType(MediaType.TEXT_PLAIN)
    public String ticket;
}
package org.openubl.managers;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.models.FileType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class FilesManager {

    private static final Logger LOG = Logger.getLogger(FilesManager.class);

    @Inject
    CamelContext camelContext;

    @ConfigProperty(name = "openubl.storage.type")
    String storageType;

    public String upload(byte[] file, String filename, FileType fileType) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.FILE_NAME, filename);
        headers.put("fileType", fileType);

        LOG.debug("Using storageType=" + storageType);

        return camelContext
                .createProducerTemplate()
                .requestBodyAndHeaders("direct:" + storageType + "-save-file", file, headers, String.class);
    }

    public byte[] getFileAsBytes(String fileID) {
        LOG.debug("Using storageType=" + storageType);

        return camelContext
                .createProducerTemplate()
                .requestBody("direct:" + storageType + "-get-file", fileID, byte[].class);
    }

    public void delete(String fileID) {
        camelContext
                .createProducerTemplate()
                .requestBody("direct:" + storageType + "delete-file", fileID);
    }

}

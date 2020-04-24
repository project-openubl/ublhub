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

    /**
     * Uploads a file and zip it if necessary
     */
    public String uploadFile(byte[] file, String fileName, FileType fileType) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("isZipFile", fileType.equals(FileType.ZIP));
        headers.put(Exchange.FILE_NAME, fileName);

        return camelContext
                .createProducerTemplate()
                .requestBodyAndHeaders("direct:" + storageType + "-save-file", file, headers, String.class);
    }

    public byte[] getFileAsBytesAfterUnzip(String fileID) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("shouldUnzip", true);

        return camelContext
                .createProducerTemplate()
                .requestBodyAndHeaders("direct:" + storageType + "-get-file", fileID, headers, byte[].class);
    }

    public byte[] getFileAsBytesWithoutUnzipping(String fileID) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("shouldUnzip", true);

        return camelContext
                .createProducerTemplate()
                .requestBodyAndHeaders("direct:" + storageType + "-get-file", fileID, headers, byte[].class);
    }

    public void delete(String fileID) {
        camelContext
                .createProducerTemplate()
                .requestBody("direct:" + storageType + "delete-file", fileID);
    }

}

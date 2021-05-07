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
package io.github.project.openubl.xsender.files;

import io.github.project.openubl.xsender.models.FileType;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class FilesManager {

    @Inject
    CamelContext camelContext;

    @ConfigProperty(name = "openubl.storage.type")
    String storageType;

    /**
     * Uploads a file and zip it if necessary
     */
    public String createFile(byte[] file, String fileName, FileType fileType) {
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
        headers.put("shouldUnzip", false);

        return camelContext
                .createProducerTemplate()
                .requestBodyAndHeaders("direct:" + storageType + "-get-file", fileID, headers, byte[].class);
    }

    public String getFileLink(String fileID) {
        return camelContext
                .createProducerTemplate()
                .requestBody("direct:" + storageType + "-get-file-link", fileID, String.class);
    }

    public void delete(String fileID) {
        camelContext
                .createProducerTemplate()
                .requestBody("direct:" + storageType + "delete-file", fileID);
    }

}

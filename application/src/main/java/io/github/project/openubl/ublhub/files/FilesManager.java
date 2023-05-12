/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.files;

import org.apache.camel.CamelContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class FilesManager {

    @Inject
    CamelContext camelContext;

    @ConfigProperty(name = "openubl.storage.type")
    String storageType;

    public String createFile(byte[] file, boolean shouldZipFile) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("shouldZipFile", shouldZipFile);

        return camelContext
                .createProducerTemplate()
                .requestBodyAndHeaders("direct:" + storageType + "-save-file", file, headers, String.class);
    }

    public String createFile(File file, boolean shouldZipFile) throws FileNotFoundException {
        Map<String, Object> headers = new HashMap<>();
        headers.put("shouldZipFile", shouldZipFile);

        InputStream is = new FileInputStream(file);
        return camelContext
                .createProducerTemplate()
                .requestBodyAndHeaders("direct:" + storageType + "-save-file", is, headers, String.class);
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
                .requestBody("direct:" + storageType + "-delete-file", fileID);
    }

}

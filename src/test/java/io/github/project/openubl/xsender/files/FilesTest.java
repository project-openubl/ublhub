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

import io.github.project.openubl.xsender.resources.config.BaseKeycloakTest;
import io.github.project.openubl.xsender.resources.config.KeycloakServer;
import io.github.project.openubl.xsender.resources.config.StorageServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(StorageServer.class)
public class FilesTest extends BaseKeycloakTest {

    @Inject
    FilesManager filesManager;

    @Test
    public void uploadXMLFile() {
        // Given
        String filename = "myfile.xml";
        byte[] file = new byte[]{1,2,3};

        // When
        String result = filesManager.createFile(file, filename, FileType.XML);

        // Then
        assertNotNull(result);
    }

    @Test
    public void uploadZIPFile() {
        // Given
        String filename = "myfile.zip";
        byte[] file = new byte[]{1,2,3};

        // When
        String result = filesManager.createFile(file, filename, FileType.XML);

        // Then
        assertNotNull(result);
    }

    @Test
    public void uploadThenGetFile() {
        // Given
        String filename = "myfile.xml";
        byte[] file = new byte[]{1,2,3};
        String fileId = filesManager.createFile(file, filename, FileType.XML);

        // When
        byte[] fileAsBytesWithoutUnzipping = filesManager.getFileAsBytesWithoutUnzipping(fileId);
        byte[] fileAsBytesAfterUnzip = filesManager.getFileAsBytesAfterUnzip(fileId);

        // Then
        assertNotNull(fileAsBytesWithoutUnzipping);
        assertNotNull(fileAsBytesAfterUnzip);
    }

    @Test
    public void uploadFileThenDeleteIt() {
        // Given
        String filename = "myfile.xml";
        byte[] file = new byte[]{1,2,3};
        String fileId = filesManager.createFile(file, filename, FileType.XML);

        // When
        filesManager.delete(fileId);

        // Then
    }

    @Test
    public void uploadFileThenGetLink() {
        // Given
        String filename = "myfile.xml";
        byte[] file = new byte[]{1,2,3};
        String fileId = filesManager.createFile(file, filename, FileType.XML);

        // When
        String fileLink = filesManager.getFileLink(fileId);

        // Then
        assertNotNull(fileLink);
    }
}

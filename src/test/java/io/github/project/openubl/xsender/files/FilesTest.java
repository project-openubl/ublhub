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

import com.google.common.io.Files;
import io.github.project.openubl.xsender.resources.config.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(MinioServer.class)
@QuarkusTestResource(ArtemisServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
public class FilesTest extends BaseKeycloakTest {

    @Inject
    FilesManager filesManager;

    @Test
    public void uploadBytes() {
        // Given
        byte[] file = new byte[]{1,2,3};

        // When
        String result1 = filesManager.createFile(file, false);
        String result2 = filesManager.createFile(file, true);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    public void uploadFile(@TempDir Path tempPath) throws IOException {
        // Given
        String filename = "myfile.xml";
        byte[] fileContent = new byte[]{1,2,3};
        File file = tempPath.resolve(filename).toFile();
        Files.write(fileContent, file);

        // When
        String result = filesManager.createFile(file, false);
        String result2 = filesManager.createFile(file, true);

        // Then
        assertNotNull(result);
        assertNotNull(result2);
    }

    @Test
    public void uploadThenGetFile() {
        // Given
        byte[] fileContent = new byte[]{1,2,3};
        String fileId = filesManager.createFile(fileContent, true);

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
        byte[] fileContent = new byte[]{1,2,3};
        String fileId = filesManager.createFile(fileContent, true);

        // When
        filesManager.delete(fileId);

        // Then
    }

    @Test
    public void uploadFileThenGetLink() {
        // Given
        byte[] fileContent = new byte[]{1,2,3};
        String fileId = filesManager.createFile(fileContent, true);

        // When
        String fileLink = filesManager.getFileLink(fileId);

        // Then
        assertNotNull(fileLink);
    }
}

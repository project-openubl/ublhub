package io.github.project.openubl.xsender.files;

import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.resources.config.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@QuarkusTestResource(StorageServer.class)
@QuarkusTestResource(SenderServer.class)
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

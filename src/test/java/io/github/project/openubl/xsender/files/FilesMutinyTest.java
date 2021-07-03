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
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(MinioServer.class)
@QuarkusTestResource(ArtemisServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
public class FilesMutinyTest extends BaseKeycloakTest {

    @Inject
    FilesMutiny filesMutiny;

    @Test
    public void uploadNull() throws IOException {
        // Given
        byte[] file = null;

        // When
        UniAssertSubscriber<String> subscriber1 = filesMutiny.createFile(file, false).subscribe().withSubscriber(UniAssertSubscriber.create());
        UniAssertSubscriber<String> subscriber2 = filesMutiny.createFile(file, true).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber1.assertFailed();
        subscriber2.assertFailed();
    }

    @Test
    public void uploadBytes() throws IOException {
        // Given
        byte[] file = new byte[]{1, 2, 3};

        // When
        UniAssertSubscriber<String> subscriber1 = filesMutiny.createFile(file, false).subscribe().withSubscriber(UniAssertSubscriber.create());
        UniAssertSubscriber<String> subscriber2 = filesMutiny.createFile(file, true).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber1.assertCompleted();
        subscriber2.assertCompleted();
    }

    @Test
    public void uploadFile(@TempDir Path tempPath) throws IOException {
        // Given
        String filename = "myfile.xml";
        byte[] fileContent = new byte[]{1, 2, 3};
        File file = tempPath.resolve(filename).toFile();
        Files.write(fileContent, file);

        // When
        UniAssertSubscriber<String> subscriber1 = filesMutiny.createFile(file, false).subscribe().withSubscriber(UniAssertSubscriber.create());
        UniAssertSubscriber<String> subscriber2 = filesMutiny.createFile(file, true).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber1.assertCompleted();
        subscriber2.assertCompleted();
    }

    @Test
    public void uploadThenGetFile() {
        // Given
        byte[] fileContent = new byte[]{1, 2, 3};
        Uni<String> fileUni = filesMutiny.createFile(fileContent, true);

        // When
        UniAssertSubscriber<byte[]> uniFileAsBytesWithoutUnzipping = fileUni.onItem().transformToUni(fileID -> filesMutiny.getFileAsBytesWithoutUnzipping(fileID))
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        UniAssertSubscriber<byte[]> uniFileAsBytesAfterUnzipping = fileUni.onItem().transformToUni(fileID -> filesMutiny.getFileAsBytesAfterUnzip(fileID))
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        uniFileAsBytesWithoutUnzipping.assertCompleted();
        uniFileAsBytesAfterUnzipping.assertCompleted();
    }

    @Test
    public void uploadFileThenDeleteIt() {
        // Given
        byte[] fileContent = new byte[]{1,2,3};
        Uni<String> fileIdUni = filesMutiny.createFile(fileContent, true);

        // When
        UniAssertSubscriber<Void> fileDeleteUni = fileIdUni.onItem().transformToUni(fileID -> filesMutiny.delete(fileID))
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        fileDeleteUni.assertCompleted();
    }

    @Test
    public void uploadFileThenGetLink() {
        // Given
        byte[] fileContent = new byte[]{1,2,3};
        Uni<String> fileIdUni = filesMutiny.createFile(fileContent, true);

        // When
        UniAssertSubscriber<String> fileLinkUni = fileIdUni.onItem().transformToUni(fileId -> filesMutiny.getFileLink(fileId))
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        fileLinkUni.assertCompleted();
    }
}

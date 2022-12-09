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

import io.github.project.openubl.ublhub.files.exceptions.PersistFileException;
import io.github.project.openubl.ublhub.files.exceptions.ReadFileException;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;

@ApplicationScoped
public class FilesMutiny {

    private static final Logger LOGGER = Logger.getLogger(FilesMutiny.class);

    @Inject
    FilesManager filesManager;

    public Uni<String> createFile(byte[] file, boolean shouldZipFile) {
        return Uni.createFrom().emitter((emitter) -> {
            if (file == null) {
                emitter.fail(new PersistFileException("Can not save null file"));
            } else {
                try {
                    String fileID = filesManager.createFile(file, shouldZipFile);
                    emitter.complete(fileID);
                } catch (Throwable e) {
                    LOGGER.error(e);
                    emitter.fail(new PersistFileException(e));
                }
            }
        });
    }

    public Uni<String> createFile(File file, boolean shouldZipFile) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                String fileID = filesManager.createFile(file, shouldZipFile);
                uniEmitter.complete(fileID);
            } catch (Throwable e) {
                LOGGER.error(e);
                uniEmitter.fail(new PersistFileException(e));
            }
        });
    }

    public Uni<byte[]> getFileAsBytesAfterUnzip(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                byte[] file = filesManager.getFileAsBytesAfterUnzip(fileID);
                uniEmitter.complete(file);
            } catch (Throwable e) {
                LOGGER.error(e);
                uniEmitter.fail(new ReadFileException(e));
            }
        });
    }

    public Uni<byte[]> getFileAsBytesWithoutUnzipping(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                byte[] file = filesManager.getFileAsBytesWithoutUnzipping(fileID);
                uniEmitter.complete(file);
            } catch (Throwable e) {
                LOGGER.error(e);
                uniEmitter.fail(new ReadFileException(e));
            }
        });
    }

    public Uni<String> getFileLink(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                String fileLink = filesManager.getFileLink(fileID);
                uniEmitter.complete(fileLink);
            } catch (Throwable e) {
                LOGGER.error(e);
                uniEmitter.fail(new ReadFileException(e));
            }
        });
    }

    public Uni<Void> delete(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                filesManager.delete(fileID);
                uniEmitter.complete(null);
            } catch (Throwable e) {
                LOGGER.error(e);
                uniEmitter.fail(new PersistFileException(e));
            }
        });
    }

}

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

import io.github.project.openubl.xsender.exceptions.FetchFileException;
import io.github.project.openubl.xsender.exceptions.SaveFileException;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;

@ApplicationScoped
public class FilesMutiny {

    @Inject
    FilesManager filesManager;

    public Uni<String> createFile(byte[] file, boolean shouldZipFile) {
        return Uni.createFrom().item(file)
                .onItem().ifNull().failWith(() -> new SaveFileException("Invalid body"))
                .onItem().ifNotNull().transformToUni((bytes, uniEmitter) -> {
                    try {
                        String fileID = filesManager.createFile(file, shouldZipFile);
                        uniEmitter.complete(fileID);
                    } catch (Throwable e) {
                        uniEmitter.fail(new SaveFileException(e));
                    }
                });
    }

    public Uni<String> createFile(File file, boolean shouldZipFile) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                String fileID = filesManager.createFile(file, shouldZipFile);
                uniEmitter.complete(fileID);
            } catch (Throwable e) {
                uniEmitter.fail(new SaveFileException(e));
            }
        });
    }

    public Uni<byte[]> getFileAsBytesAfterUnzip(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                byte[] file = filesManager.getFileAsBytesAfterUnzip(fileID);
                uniEmitter.complete(file);
            } catch (Throwable e) {
                uniEmitter.fail(new FetchFileException(e));
            }
        });
    }

    public Uni<byte[]> getFileAsBytesWithoutUnzipping(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                byte[] file = filesManager.getFileAsBytesWithoutUnzipping(fileID);
                uniEmitter.complete(file);
            } catch (Throwable e) {
                uniEmitter.fail(new FetchFileException(e));
            }
        });
    }

    public Uni<String> getFileLink(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                String fileLink = filesManager.getFileLink(fileID);
                uniEmitter.complete(fileLink);
            } catch (Throwable e) {
                uniEmitter.fail(new FetchFileException(e));
            }
        });
    }

    public Uni<Void> delete(String fileID) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                filesManager.delete(fileID);
                uniEmitter.complete(null);
            } catch (Throwable e) {
                uniEmitter.fail(new FetchFileException(e));
            }
        });
    }

}

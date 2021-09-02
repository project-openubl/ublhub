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
package io.github.project.openubl.xsender.files.health.impl;

import io.github.project.openubl.xsender.files.health.StorageProvider;
import io.github.project.openubl.xsender.files.health.StorageReadinessCheck;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;

@ApplicationScoped
@StorageProvider(StorageProvider.Type.FILESYSTEM)
public class FilesystemReadinessCheck implements StorageReadinessCheck {

    @ConfigProperty(name = "openubl.storage.filesystem.folder")
    String filesystemFolder;

    @Override
    public boolean isHealthy() {
        return !filesystemFolder.isBlank();
    }

}

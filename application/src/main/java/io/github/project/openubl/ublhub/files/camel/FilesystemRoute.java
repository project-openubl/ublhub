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
package io.github.project.openubl.ublhub.files.camel;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@ApplicationScoped
public class FilesystemRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.storage.filesystem.directory")
    String fileSystemFolder;

    @Override
    public void configure() throws Exception {
        from("direct:filesystem-save-file")
                .id("filesystem-save-file")
                .choice()
                    .when(header("shouldZipFile").isEqualTo(true))
                        .marshal().zipFile()
                    .endChoice()
                .end()
                .setHeader("CamelFileName", () -> UUID.randomUUID() + ".zip")
                .setHeader("folderName", constant(fileSystemFolder))
                .toD("file:${header.folderName}")
                .process(exchange -> {
                    String folderName = exchange.getIn().getHeader("folderName", String.class);
                    String fileName = exchange.getIn().getHeader("CamelFileName", String.class);

                    Path resolve = Paths.get(folderName).resolve(fileName);
                    exchange.getIn().setBody(resolve.toString());
                });

        from("direct:filesystem-get-file")
                .id("filesystem-get-file")
                .process(exchange -> {
                    String filename = exchange.getIn().getBody(String.class);
                    byte[] bytes = Files.readAllBytes(Paths.get(filename));
                    exchange.getIn().setBody(bytes);
                })
                .choice()
                    .when(header("shouldUnzip").isEqualTo(true))
                        .unmarshal(RouteUtils.getZipFileDataFormat())
                        .split(bodyAs(Iterator.class), (oldExchange, newExchange) -> newExchange)
                            .streaming()
                            .convertBodyTo(byte[].class)
                        .end()
                    .endChoice()
                .end();

        from("direct:filesystem-get-file-link")
                .id("filesystem-get-file-link")
                .log(LoggingLevel.WARN, "Filesystem does not support link generation.");

        from("direct:filesystem-delete-file")
                .id("filesystem-delete-file")
                .process(exchange -> {
                    String filename = exchange.getIn().getBody(String.class);
                    Files.delete(Paths.get(filename));
                });
    }

}

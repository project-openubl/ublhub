package org.openubl.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jgroups.util.UUID;
import org.openubl.models.FileType;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class FilesystemRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.storage.filesystem.folder", defaultValue = "xml-sender-filesystem")
    String fileSystemFolder;

    @Override
    public void configure() throws Exception {
        from("direct:filesystem-save-file")
                .id("filesystem-save-file")
                .choice()
                    .when(header("fileType").isEqualTo(FileType.XML))
                        .marshal().zipFile()
                    .endChoice()
                .end()
                .setHeader("CamelFileName", () -> UUID.randomUUID().toString() + ".zip")
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
                });

        from("direct:filesystem-get-file-link")
                .id("filesystem-get-file-link")
                .log(LoggingLevel.WARN, "Filesystem does not support link generation. Will return null");

        from("direct:filesystem-delete-file")
                .id("filesystem-delete-file")
                .process(exchange -> {
                    String filename = exchange.getIn().getBody(String.class);
                    Files.delete(Paths.get(filename));
                });
    }

}

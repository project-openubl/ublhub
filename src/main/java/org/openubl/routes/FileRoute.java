package org.openubl.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jgroups.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class FileRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.storageType")
    String storageType;

    @ConfigProperty(name = "openubl.storage.filesystem.folder")
    String fileSystemFolder;

    @Override
    public void configure() throws Exception {
        from("direct:save-file")
                .setHeader("storageType", constant(storageType))
                .choice()
                    .when(header("storageType").isEqualTo("ftp"))
                        .log("FTP is not supported yet")
                    .endChoice()
                    .when(header("storageType").isEqualTo("s3"))
                        .log("S3 is not supported yet")
                    .endChoice()
                    .otherwise()
                        .to("direct:save-file-in-filesystem")
                    .endChoice()
                .end();

        from("direct:save-file-in-filesystem")
                .setHeader("folder", constant(fileSystemFolder))
                .setHeader("subFolder", simple("${date:now:yyyyMMdd}"))
                .process(exchange -> {
                    String folder = exchange.getIn().getHeader("folder", String.class);
                    String subFolder = exchange.getIn().getHeader("subFolder", String.class);

                    Path resolve = Paths.get(folder).resolve(subFolder);
                    exchange.getIn().setHeader("folderName", resolve.toString());
                })
                .setHeader("CamelFileName", () -> UUID.randomUUID().toString() + ".zip")
                .toD("file:${header.folderName}")
                .process(exchange -> {
                    String folderName = exchange.getIn().getHeader("folderName", String.class);
                    String fileName = exchange.getIn().getHeader("CamelFileName", String.class);

                    Path resolve = Paths.get(folderName).resolve(fileName);
                    exchange.getIn().setBody(resolve.toString());
                });
    }

}

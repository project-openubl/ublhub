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
package io.github.project.openubl.ublhub.consumers;

import io.github.project.openubl.ublhub.documents.DocumentRoute;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.dataformat.YAMLLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;

@ApplicationScoped
public class FilesystemConsumerRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.consumers.filesystem.enabled")
    boolean isConsumerEnabled;

    @ConfigProperty(name = "openubl.consumers.filesystem.directory")
    String consumerTargetDirectory;

    @Override
    public void configure() throws Exception {
        from("file://" + consumerTargetDirectory + "?includeExt=json,yml,yaml,xml&delete=true")
                .autoStartup("{{openubl.consumers.filesystem.enabled}}")
                .id("consumer-filesystem")
                .choice()
                    .when(header(FileConstants.FILE_NAME).regex(".*\\.(yml|yaml)"))
                        .unmarshal().yaml(YAMLLibrary.SnakeYAML, JsonObject.class)
                        .to("direct:import-json")
                    .endChoice()
                    .when(header(FileConstants.FILE_NAME).regex(".*\\.(json)"))
                        .unmarshal().json(JsonLibrary.Jsonb, JsonObject.class)
                        .to("direct:import-json")
                    .endChoice()
                    .when(header(FileConstants.FILE_NAME).regex(".*\\.(xml)"))
                        .process(exchange -> {
                            String fileName = exchange.getIn().getHeader(FileConstants.FILE_NAME_ONLY, String.class);
                            String[] split = fileName.split("\\.");
                            String project = split.length > 2 ? split[split.length - 2] : null;

                            exchange.getIn().setHeader(DocumentRoute.DOCUMENT_PROJECT, project);
                        })
                        .to("direct:import-xml")
                    .endChoice()
                .end();
    }

}

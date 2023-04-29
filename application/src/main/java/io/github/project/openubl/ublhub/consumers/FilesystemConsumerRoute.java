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

import io.github.project.openubl.ublhub.files.camel.RouteUtils;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@ApplicationScoped
public class FilesystemConsumerRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.consumers.filesystem.enabled")
    boolean isConsumerEnabled;

    @ConfigProperty(name = "openubl.consumers.filesystem.directory")
    String consumerTargetDirectory;

    @Override
    public void configure() throws Exception {
        from("file://" + consumerTargetDirectory + "?delete=true")
                .autoStartup("{{openubl.consumers.filesystem.enabled}}")
                .id("consumer-filesystem")
                .process(exchange -> {
                    System.out.println("hello");
                });
    }

}

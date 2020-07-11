/**
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
package io.github.project.openubl.xmlsender.resources;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
        info = @Info(
                title = "XML Sender API",
                version = "1.0.0",
                contact = @Contact(
                        name = "XML Sender API Support",
                        url = "https://github.com/project-openubl/xsender-server/issues",
                        email = "projectopenubl+subscribe@googlegroups.com"
                ),
                license = @License(
                        name = "Eclipse Public License - v 2.0",
                        url = "https://www.eclipse.org/legal/epl-2.0/"
                )
        )
)
@ApplicationPath(ApiApplication.API_BASE)
public class ApiApplication extends Application {
    public static final String API_BASE = "/api";
}

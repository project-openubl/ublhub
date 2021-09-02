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
package io.github.project.openubl.xsender.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RegisterForReflection
public class CompanyRepresentation {

    private String id;

    @NotNull
    @Size(min = 11, max = 11)
    private String ruc;

    @NotNull
    private String name;

    private String description;

    @NotNull
    @Valid
    private SunatUrlsRepresentation webServices;

    @NotNull
    @Valid
    private SunatCredentialsRepresentation credentials;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SunatUrlsRepresentation getWebServices() {
        return webServices;
    }

    public void setWebServices(SunatUrlsRepresentation webServices) {
        this.webServices = webServices;
    }

    public SunatCredentialsRepresentation getCredentials() {
        return credentials;
    }

    public void setCredentials(SunatCredentialsRepresentation credentials) {
        this.credentials = credentials;
    }

}

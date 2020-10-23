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
package io.github.project.openubl.xsender.authz.resources;

import io.github.project.openubl.xsender.core.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.core.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.core.idm.PageRepresentation;
import io.github.project.openubl.xsender.core.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.core.managers.CompanyManager;
import io.github.project.openubl.xsender.core.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.core.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.core.resources.CompanyResource;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Transactional
@ApplicationScoped
public class AuthzCompanyResource implements CompanyResource {


    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyManager companyManager;

    @Override
    public CompanyRepresentation getCompany(@NotNull String company) {
        return null;
    }

    @Override
    public CompanyRepresentation updateCompany(@NotNull String company, @NotNull @Valid CompanyRepresentation rep) {
        return null;
    }

    @Override
    public void updateCompanySUNATCredentials(@NotNull String company, @NotNull @Valid SunatCredentialsRepresentation rep) {

    }

    @Override
    public PageRepresentation<DocumentRepresentation> listDocuments(@NotNull String company, String filterText, Integer offset, Integer limit, List<String> sortBy) {
        return null;
    }

    @Override
    public Response createDocument(@NotNull String company, MultipartFormDataInput input) {
        return null;
    }

    @Override
    public DocumentRepresentation getDocument(@NotNull String company, @NotNull String documentId) {
        return null;
    }

    @Override
    public Response getDocumentFile(@NotNull String company, @NotNull String documentId) {
        return null;
    }

    @Override
    public String getDocumentFileLink(@NotNull String company, @NotNull String documentId) {
        return null;
    }

    @Override
    public Response getDocumentCDR(@NotNull String company, @NotNull String documentId) {
        return null;
    }

    @Override
    public String getDocumentCDRLink(@NotNull String company, @NotNull String documentId) {
        return null;
    }
}


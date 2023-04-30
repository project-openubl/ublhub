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
package io.github.project.openubl.ublhub;

import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.Stream;

@ApplicationScoped
public class ResourceHelpers {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    public void generatePreexistingData() {
        Stream.of(1L, 2L, 3L)
                .forEach(projectId -> {
                    ProjectEntity project = ProjectEntity.builder()
                            .id(projectId)
                            .name("my-project" + projectId)
                            .description("description" + projectId)
                            .sunat(SunatEntity.builder()
                                    .sunatUsername("username" + projectId)
                                    .sunatPassword("password" + projectId)
                                    .sunatUrlFactura("http://factura" + projectId)
                                    .sunatUrlGuiaRemision("http://guia" + projectId)
                                    .sunatUrlPercepcionRetencion("http://percepcionRetencion" + projectId)
                                    .build()
                            )
                            .build();

                    projectRepository.persist(project);

                    // Companies
                    Stream.of(1L, 2L, 3L)
                            .forEach(companyId -> {
                                CompanyEntity company = CompanyEntity.builder()
                                        .projectId(project.getId())
                                        .id(Long.parseLong(project.getId().toString() + companyId))
                                        .ruc(companyId.toString().repeat(11))
                                        .name("company" + companyId)
                                        .sunat(SunatEntity.builder()
                                                .sunatUsername("username-company" + companyId)
                                                .sunatPassword("password-company" + companyId)
                                                .sunatUrlFactura("http://factura-company" + companyId)
                                                .sunatUrlGuiaRemision("http://guia-company" + companyId)
                                                .sunatUrlPercepcionRetencion("http://percepcionRetencion-company" + companyId)
                                                .build()
                                        )
                                        .build();

                                companyRepository.persist(company);
                            });

                    // Documents
                    Stream.of(1L, 2L)
                            .forEach(documentId -> {
                                UBLDocumentEntity document = UBLDocumentEntity.builder()
                                        .projectId(project.getId())
                                        .id(Long.parseLong(project.getId().toString() + documentId))
                                        .xmlFileId("/home/file")
                                        .xmlData(XMLDataEntity.builder()
                                                .ruc("12345678910")
                                                .serieNumero("F-" + documentId)
                                                .tipoDocumento("Invoice")
                                                .build()
                                        )
                                        .build();

                                documentRepository.persist(document);
                            });
                });
    }
}

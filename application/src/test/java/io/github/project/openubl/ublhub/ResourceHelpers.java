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

import com.github.f4b6a3.tsid.TsidFactory;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ApplicationScoped
public class ResourceHelpers {

    public static final List<String> projects = new CopyOnWriteArrayList<>();
    public static final Map<String, List<String>> projectRuc = new ConcurrentHashMap<>();
    public static final Map<String, List<Long>> projectDocumentIds = new ConcurrentHashMap<>();

    @Inject
    ProjectRepository projectRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    TsidFactory tsidFactory;

    private static String generateProjectName(Integer id) {
        return "my-project" + id;
    }

    private static String generateCompanyRuc(Integer id) {
        return id.toString().repeat(11);
    }

    public void generatePreexistingData() {
        IntStream.rangeClosed(1, 3)
                .forEach(projectIndex -> {
                    String projectName = generateProjectName(projectIndex);
                    projects.add(projectName);

                    ProjectEntity projectEntity = ProjectEntity.builder()
                            .name(generateProjectName(projectIndex))
                            .description("description" + projectIndex)
                            .sunat(SunatEntity.builder()
                                    .sunatUsername("username" + projectIndex)
                                    .sunatPassword("password" + projectIndex)
                                    .sunatUrlFactura("http://factura" + projectIndex)
                                    .sunatUrlGuiaRemision("http://guia" + projectIndex)
                                    .sunatUrlPercepcionRetencion("http://percepcionRetencion" + projectIndex)
                                    .build()
                            )
                            .build();

                    projectRepository.persist(projectEntity);

                    // Companies
                    IntStream.rangeClosed(1, 3)
                            .forEach(companyIndex -> {
                                String ruc = generateCompanyRuc(companyIndex);

                                List<String> rucList = projectRuc.getOrDefault(projectName, new ArrayList<>());
                                rucList.add(ruc);
                                projectRuc.put(projectName, rucList);

                                CompanyEntity companyEntity = CompanyEntity.builder()
                                        .id(new CompanyEntity.CompanyId(projectName, ruc))
                                        .name("company" + companyIndex)
                                        .sunat(SunatEntity.builder()
                                                .sunatUsername("username-company" + companyIndex)
                                                .sunatPassword("password-company" + companyIndex)
                                                .sunatUrlFactura("http://factura-company" + companyIndex)
                                                .sunatUrlGuiaRemision("http://guia-company" + companyIndex)
                                                .sunatUrlPercepcionRetencion("http://percepcionRetencion-company" + companyIndex)
                                                .build()
                                        )
                                        .build();

                                companyRepository.persist(companyEntity);
                            });

                    // Documents
                    IntStream.rangeClosed(1, 2)
                            .forEach(documentIndex -> {
                                long documentId = tsidFactory.create().toLong();

                                List<Long> documentIdList = projectDocumentIds.getOrDefault(projectName, new ArrayList<>());
                                documentIdList.add(documentId);
                                projectDocumentIds.put(projectName, documentIdList);

                                UBLDocumentEntity documentEntity = UBLDocumentEntity.builder()
                                        .project(projectName)
                                        .id(documentId)
                                        .xmlFileId("/home/file")
                                        .xmlData(XMLDataEntity.builder()
                                                .ruc("12345678910")
                                                .serieNumero("F-" + documentIndex)
                                                .tipoDocumento("Invoice")
                                                .build()
                                        )
                                        .build();

                                documentRepository.persist(documentEntity);
                            });
                });
    }
}

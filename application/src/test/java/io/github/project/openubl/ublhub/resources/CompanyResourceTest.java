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
package io.github.project.openubl.ublhub.resources;

import io.github.project.openubl.ublhub.AbstractBaseTest;
import io.github.project.openubl.ublhub.ProductionTestProfile;
import io.github.project.openubl.ublhub.dto.CompanyDto;
import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatDto;
import io.github.project.openubl.ublhub.keys.GeneratedRsaKeyProviderFactory;
import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProductionTestProfile.class)
@TestHTTPEndpoint(CompanyResource.class)
public class CompanyResourceTest extends AbstractBaseTest {

    ProjectDto projectDto = ProjectDto.builder()
            .name("myproject")
            .description("my description")
            .sunat(SunatDto.builder()
                    .facturaUrl("http://projectUrl1")
                    .guiaUrl("http://projectUrl2")
                    .retencionUrl("http://projectUrl3")
                    .username("projectUsername")
                    .password("projectPassword")
                    .build()
            )
            .build();

    CompanyDto companyDto = CompanyDto.builder()
            .name("company1")
            .ruc("11111111111")
            .sunat(SunatDto.builder()
                    .facturaUrl("http://companyUrl1")
                    .guiaUrl("http://companyUrl2")
                    .retencionUrl("http://companyUrl3")
                    .username("companyUsername")
                    .password("companyPassword")
                    .build()
            )
            .build();

    @BeforeEach
    public void beforeEach() {
        cleanDB();
    }

    private void createProject(String username, ProjectDto projectDto) {
        givenAuth(username)
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);
    }

    private void createCompany(String username, String projectName, CompanyDto companyDto) {
        givenAuth(username)
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + projectName + "/companies")
                .then()
                .statusCode(201);
    }

    private void createProjectAndCompany(String username, ProjectDto projectDto, CompanyDto companyDto) {
        givenAuth(username)
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        givenAuth(username)
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(201);
    }

    @Test
    public void getCompany() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(200)
                .body("name", is("company1"),
                        "ruc", is("11111111111"),
                        "sunat.facturaUrl", is("http://companyUrl1"),
                        "sunat.guiaUrl", is("http://companyUrl2"),
                        "sunat.retencionUrl", is("http://companyUrl3"),
                        "sunat.username", is("companyUsername"),
                        "sunat.password", nullValue()
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(404);

        // Then
    }

    @Test
    public void getCompanyShouldMatchProjectAndRuc() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(200);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/some-project/companies/" + companyDto.getRuc())
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void createCompany() {
        // Given
        createProject("alice", projectDto);

        // When
        CompanyDto companyDto = CompanyDto.builder()
                .name("My company")
                .ruc("12345678910")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(201)
                .body("name", is(companyDto.getName()),
                        "ruc", is(companyDto.getRuc()),
                        "sunat.facturaUrl", is(companyDto.getSunat().getFacturaUrl()),
                        "sunat.guiaUrl", is(companyDto.getSunat().getGuiaUrl()),
                        "sunat.retencionUrl", is(companyDto.getSunat().getRetencionUrl()),
                        "sunat.username", is(companyDto.getSunat().getUsername()),
                        "sunat.password", nullValue()
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(404);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(200)
                .body("name", is(companyDto.getName()),
                        "sunat.facturaUrl", is(companyDto.getSunat().getFacturaUrl()),
                        "sunat.guiaUrl", is(companyDto.getSunat().getGuiaUrl()),
                        "sunat.retencionUrl", is(companyDto.getSunat().getRetencionUrl()),
                        "sunat.username", is(companyDto.getSunat().getUsername()),
                        "sunat.password", nullValue()
                );
    }

    @Test
    public void create2CompaniesWithSameRuc_shouldNotBeAllowed() {
        // Given
        createProject("alice", projectDto);

        // When
        CompanyDto company = CompanyDto.builder()
                .name("My company")
                .ruc("11111111111")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(company)
                .when()
                .post("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(201);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(company)
                .when()
                .post("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(409);
    }

    @Test
    public void createCompanyWithoutSunatData() {
        // Given
        createProject("alice", projectDto);

        // When
        CompanyDto companyDto = CompanyDto.builder()
                .name("My company")
                .ruc("12345678910")
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(201)
                .body("name", is(companyDto.getName()),
                        "ruc", is(companyDto.getRuc()),
                        "sunat", is(nullValue())
                );

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(200)
                .body("ruc", is(companyDto.getRuc()),
                        "name", is(companyDto.getName()),
                        "sunat", is(nullValue())
                );
    }

    @Test
    public void updateCompany() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        companyDto = CompanyDto.builder()
                .ruc("99999999999")
                .name("new name")
                .description("new description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://newUrl1.com")
                        .retencionUrl("http://newUrl2.com")
                        .guiaUrl("http://newUrl3.com")
                        .username("new username")
                        .password("new password")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .put("/" + projectDto.getName() + "/companies/" + "11111111111")
                .then()
                .statusCode(200)
                .body("ruc", is("11111111111"),
                        "name", is(companyDto.getName()),
                        "description", is(companyDto.getDescription()),
                        "sunat.facturaUrl", is(companyDto.getSunat().getFacturaUrl()),
                        "sunat.retencionUrl", is(companyDto.getSunat().getRetencionUrl()),
                        "sunat.guiaUrl", is(companyDto.getSunat().getGuiaUrl()),
                        "sunat.username", is(companyDto.getSunat().getUsername()),
                        "sunat.password", is(nullValue())
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .put("/" + projectDto.getName() + "/companies/" + "11111111111")
                .then()
                .statusCode(404);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + "11111111111")
                .then()
                .statusCode(200)
                .body("ruc", is("11111111111"),
                        "name", is(companyDto.getName()),
                        "description", is(companyDto.getDescription()),
                        "sunat.facturaUrl", is(companyDto.getSunat().getFacturaUrl()),
                        "sunat.retencionUrl", is(companyDto.getSunat().getRetencionUrl()),
                        "sunat.guiaUrl", is(companyDto.getSunat().getGuiaUrl()),
                        "sunat.username", is(companyDto.getSunat().getUsername()),
                        "sunat.password", is(nullValue())
                );
    }

    @Test
    public void deleteCompany() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(404);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(204);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc())
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteCompany_notFound() {
        // Given
        createProject("alice", projectDto);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectDto.getName() + "/companies/" + "44444444444")
                .then()
                .statusCode(404);

        // Then
    }

    @Test
    public void getCompanies() {
        // Given
        createProject("alice", projectDto);

        IntStream.rangeClosed(1, 3).forEach(value -> {
            CompanyDto companyDto = CompanyDto.builder()
                    .name("company" + value)
                    .ruc(String.valueOf(value).repeat(11))
                    .sunat(SunatDto.builder()
                            .facturaUrl("http://url1")
                            .guiaUrl("http://url2")
                            .retencionUrl("http://url3")
                            .username("username")
                            .password("password")
                            .build()
                    )
                    .build();

            createCompany("alice", projectDto.getName(), companyDto);
        });

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(200)
                .body("size()", is(3),
                        "[0].name", is("company3"),
                        "[1].name", is("company2"),
                        "[2].name", is("company1")
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void getCompaniesFromNonExistentProject_shouldReturn404() {
        // Given
        String projectId = "999";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/companies")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void getCompanyKeys() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/keys")
                .then()
                .statusCode(200)
                .body("active.RS256", is(notNullValue()),
                        "keys.size()", is(1),
                        "keys[0].status", is("ACTIVE"),
                        "keys[0].type", is("RSA"),
                        "keys[0].algorithm", is("RS256"),
                        "keys[0].use", is("SIG"),
                        "keys[0].kid", is(notNullValue()),
                        "keys[0].publicKey", is(notNullValue()),
                        "keys[0].certificate", is(notNullValue()),
                        "keys[0].providerId", is(notNullValue()),
                        "keys[0].providerPriority", is(100)
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/keys")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void createCompanyComponent() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectDto.getName()),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components")
                .then()
                .statusCode(404);
    }

    @Test
    public void getCompanyComponent() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        String componentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectDto.getName()),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components/" + componentId)
                .then()
                .statusCode(404);
    }

    @Test
    public void updateCompanyComponent() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        String componentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        componentDto = ComponentDto.builder()
                .name("myNewKeyname")
                .config(new HashMap<>() {{
                    put("active", List.of("false"));
                    put("algorithm", List.of("RS512"));
                    put("enabled", List.of("false"));
                    put("keySize", List.of("4096"));
                    put("priority", List.of("222"));
                }})
                .build();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .body(componentDto)
                .put("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectDto.getName()),
                        "providerId", is(GeneratedRsaKeyProviderFactory.ID),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("false"),
                        "config.algorithm[0]", is("RS512"),
                        "config.enabled[0]", is("false"),
                        "config.keySize[0]", is("4096"),
                        "config.priority[0]", is("222")
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .body(componentDto)
                .put("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components/" + componentId)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteCompanyComponent() {
        // Given
        createProjectAndCompany("alice", projectDto, companyDto);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        String componentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components/" + componentId)
                .then()
                .statusCode(404);
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components/" + componentId)
                .then()
                .statusCode(204);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/companies/" + companyDto.getRuc() + "/components/" + componentId)
                .then()
                .statusCode(404);
    }
}


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
import io.github.project.openubl.ublhub.ResourceHelpers;
import io.github.project.openubl.ublhub.dto.CompanyDto;
import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.dto.SunatDto;
import io.github.project.openubl.ublhub.keys.GeneratedRsaKeyProviderFactory;
import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProductionTestProfile.class)
@TestHTTPEndpoint(CompanyResource.class)
public class CompanyResourceTest extends AbstractBaseTest {

    @Inject
    ResourceHelpers resourceHelpers;

    @BeforeEach
    public void beforeEach() {
        cleanDB();
        resourceHelpers.generatePreexistingData();
    }

    @Test
    public void getCompany() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(200)
                .body("name", is("company1"),
                        "ruc", is("11111111111"),
                        "sunat.facturaUrl", is("http://factura-company1"),
                        "sunat.guiaUrl", is("http://guia-company1"),
                        "sunat.retencionUrl", is("http://percepcionRetencion-company1"),
                        "sunat.username", is("username-company1"),
                        "sunat.password", nullValue()
                );
        // Then
    }

    @Test
    public void getCompanyShouldMatchProjectAndRuc() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(200);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/some-project/companies/" + ruc)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void createCompany() {
        // Given
        String project = ResourceHelpers.projects.get(0);

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

        // When
        CompanyDto response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + project + "/companies")
                .then()
                .statusCode(201)
                .body("name", is(companyDto.getName()),
                        "ruc", is(companyDto.getRuc()),
                        "sunat.facturaUrl", is(companyDto.getSunat().getFacturaUrl()),
                        "sunat.guiaUrl", is(companyDto.getSunat().getGuiaUrl()),
                        "sunat.retencionUrl", is(companyDto.getSunat().getRetencionUrl()),
                        "sunat.username", is(companyDto.getSunat().getUsername()),
                        "sunat.password", nullValue()
                ).extract().body().as(CompanyDto.class);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + response.getRuc())
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
        String project = ResourceHelpers.projects.get(0);

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

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(company)
                .when()
                .post("/" + project + "/companies")
                .then()
                .statusCode(409);
        // Then
    }

    @Test
    public void createCompanyWithoutSunatData() {
        // Given
        String project = ResourceHelpers.projects.get(0);

        CompanyDto companyDto = CompanyDto.builder()
                .name("My company")
                .ruc("12345678910")
                .build();

        // When
        CompanyDto response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + project + "/companies")
                .then()
                .statusCode(201)
                .body("name", is(companyDto.getName()),
                        "ruc", is(companyDto.getRuc()),
                        "sunat", is(nullValue())
                ).extract().body().as(CompanyDto.class);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + response.getRuc())
                .then()
                .statusCode(200)
                .body("ruc", is(response.getRuc()),
                        "name", is(companyDto.getName()),
                        "sunat", is(nullValue())
                );
    }

    @Test
    public void updateCompany() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

        CompanyDto companyDto = CompanyDto.builder()
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

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .put("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(200)
                .body("ruc", is(ruc),
                        "name", is(companyDto.getName()),
                        "description", is(companyDto.getDescription()),
                        "sunat.facturaUrl", is(companyDto.getSunat().getFacturaUrl()),
                        "sunat.retencionUrl", is(companyDto.getSunat().getRetencionUrl()),
                        "sunat.guiaUrl", is(companyDto.getSunat().getGuiaUrl()),
                        "sunat.username", is(companyDto.getSunat().getUsername()),
                        "sunat.password", is(nullValue())
                );

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(200)
                .body("ruc", is(ruc),
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
    public void updateCompanyWithIncorrectProject_shouldNotBeAllowed() {
        String project = ResourceHelpers.projects.get(0);
        String ruc = "44444444444";

        // Given
        CompanyDto companyRepresentation = CompanyDto.builder()
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

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyRepresentation)
                .when()
                .put("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void deleteCompany() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(204);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteCompany_notFound() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = "44444444444";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + project + "/companies/" + ruc)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void getCompanies() {
        // Given
        String project = ResourceHelpers.projects.get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies")
                .then()
                .statusCode(200)
                .body("size()", is(3),
                        "[0].name", is("company3"),
                        "[1].name", is("company2"),
                        "[2].name", is("company1")
                );
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
        String project = ResourceHelpers.projects.get(0);

        CompanyDto companyDto = CompanyDto.builder()
                .name("mycompany")
                .description("my description")
                .ruc("99999999999")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(companyDto)
                .when()
                .post("/" + project + "/companies")
                .then()
                .statusCode(201);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + companyDto.getRuc() + "/keys")
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
    }

    @Test
    public void createCompanyComponent() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

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
                .post("/" + project + "/companies/" + ruc + "/components")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "name", is(componentDto.getName()),
                        "parentId", is(project),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );
    }

    @Test
    public void getCompanyComponent() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

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
                .post("/" + project + "/companies/" + ruc + "/components")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + ruc + "/components/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(project),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );
    }

    @Test
    public void updateCompanyComponent() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

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
                .post("/" + project + "/companies/" + ruc + "/components")
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
                .put("/" + project + "/companies/" + ruc + "/components/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(project),
                        "providerId", is(GeneratedRsaKeyProviderFactory.ID),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("false"),
                        "config.algorithm[0]", is("RS512"),
                        "config.enabled[0]", is("false"),
                        "config.keySize[0]", is("4096"),
                        "config.priority[0]", is("222")
                );
    }

    @Test
    public void deleteCompanyComponent() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        String ruc = ResourceHelpers.projectRuc.get(project).get(0);

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
                .post("/" + project + "/companies/" + ruc + "/components")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + project + "/companies/" + ruc + "/components/" + componentId)
                .then()
                .statusCode(204);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/companies/" + ruc + "/components/" + componentId)
                .then()
                .statusCode(404);
    }
}


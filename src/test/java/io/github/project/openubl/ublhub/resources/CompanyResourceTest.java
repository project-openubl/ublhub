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
import io.github.project.openubl.ublhub.BasicProfileManager;
import io.github.project.openubl.ublhub.dto.CompanyDto;
import io.github.project.openubl.ublhub.dto.SunatCredentialsDto;
import io.github.project.openubl.ublhub.dto.SunatWebServicesDto;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

@QuarkusTest
@TestProfile(BasicProfileManager.class)
@TestHTTPEndpoint(CompanyResource.class)
public class CompanyResourceTest extends AbstractBaseTest {

    @Override
    public Class<?> getTestClass() {
        return CompanyResourceTest.class;
    }

    @Test
    public void getCompany() {
        // Given
        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/1/companies/11")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()),
                        "name", is("company1"),
                        "ruc", is("11111111111"),
                        "sunatWebServices.factura", is("http://urlFactura1"),
                        "sunatWebServices.guia", is("http://urlGuia1"),
                        "sunatWebServices.retencion", is("http://urlPercepcionRetencion1"),
                        "sunatCredentials.username", is("username1"),
                        "sunatCredentials.password", nullValue()
                );
        // Then
    }

    @Test
    public void getCompanyThatBelongsToOtherProject_shouldNotBeAllowed() {
        // Given
        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/1/companies/11")
                .then()
                .statusCode(200);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/2/companies/11")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void createCompany() {
        // Given
        String projectId = "1";

        CompanyDto companyDto = CompanyDto.builder()
                .name("My company")
                .ruc("12345678910")
                .sunatWebServices(SunatWebServicesDto.builder()
                        .factura("http://url1.com")
                        .guia("http://url2.com")
                        .retencion("http://url3.com")
                        .build()
                )
                .sunatCredentials(SunatCredentialsDto.builder()
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
                .post("/" + projectId + "/companies")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "name", is(companyDto.getName()),
                        "ruc", is(companyDto.getRuc()),
                        "sunatWebServices.factura", is(companyDto.getSunatWebServices().getFactura()),
                        "sunatWebServices.guia", is(companyDto.getSunatWebServices().getGuia()),
                        "sunatWebServices.retencion", is(companyDto.getSunatWebServices().getRetencion()),
                        "sunatCredentials.username", is(companyDto.getSunatCredentials().getUsername()),
                        "sunatCredentials.password", nullValue()
                ).extract().body().as(CompanyDto.class);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/companies/" + response.getId())
                .then()
                .statusCode(200)
                .body("id", is(response.getId()),
                        "name", is(companyDto.getName()),
                        "sunatWebServices.factura", is(companyDto.getSunatWebServices().getFactura()),
                        "sunatWebServices.guia", is(companyDto.getSunatWebServices().getGuia()),
                        "sunatWebServices.retencion", is(companyDto.getSunatWebServices().getRetencion()),
                        "sunatCredentials.username", is(companyDto.getSunatCredentials().getUsername()),
                        "sunatCredentials.password", nullValue()
                );
    }

    @Test
    public void create2CompaniesWithSameRuc_shouldNotBeAllowed() {
        // Given
        String projectId = "1";

        CompanyDto company = CompanyDto.builder()
                .name("My company")
                .ruc("11111111111")
                .sunatWebServices(SunatWebServicesDto.builder()
                        .factura("http://url1.com")
                        .guia("http://url2.com")
                        .retencion("http://url3.com")
                        .build()
                )
                .sunatCredentials(SunatCredentialsDto.builder()
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
                .post("/" + projectId + "/companies")
                .then()
                .statusCode(409);
        // Then
    }

    @Test
    public void updateCompany() {
        // Given
        String projectId = "1";
        String companyId = "11";

        CompanyDto companyDto = CompanyDto.builder()
                .ruc("99999999999")
                .name("new name")
                .description("new description")
                .sunatWebServices(SunatWebServicesDto.builder()
                        .factura("http://newUrl1.com")
                        .retencion("http://newUrl2.com")
                        .guia("http://newUrl3.com")
                        .build()
                )
                .sunatCredentials(SunatCredentialsDto.builder()
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
                .put("/" + projectId + "/companies/" + companyId)
                .then()
                .statusCode(200)
                .body("ruc", is(companyDto.getRuc()),
                        "name", is(companyDto.getName()),
                        "description", is(companyDto.getDescription()),
                        "sunatWebServices.factura", is(companyDto.getSunatWebServices().getFactura()),
                        "sunatWebServices.retencion", is(companyDto.getSunatWebServices().getRetencion()),
                        "sunatWebServices.guia", is(companyDto.getSunatWebServices().getGuia()),
                        "sunatCredentials.username", is(companyDto.getSunatCredentials().getUsername()),
                        "sunatCredentials.password", is(nullValue())
                );

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/companies/" + companyId)
                .then()
                .statusCode(200)
                .body("id", is(companyId),
                        "ruc", is(companyDto.getRuc()),
                        "name", is(companyDto.getName()),
                        "description", is(companyDto.getDescription()),
                        "sunatWebServices.factura", is(companyDto.getSunatWebServices().getFactura()),
                        "sunatWebServices.retencion", is(companyDto.getSunatWebServices().getRetencion()),
                        "sunatWebServices.guia", is(companyDto.getSunatWebServices().getGuia()),
                        "sunatCredentials.username", is(companyDto.getSunatCredentials().getUsername()),
                        "sunatCredentials.password", is(nullValue())
                );
    }

    @Test
    public void updateCompanyWithIncorrectProject_shouldNotBeAllowed() {
        String projectId = "1";
        String companyId = "33";

        // Given
        CompanyDto companyRepresentation = CompanyDto.builder()
                .ruc("99999999999")
                .name("new name")
                .description("new description")
                .sunatWebServices(SunatWebServicesDto.builder()
                        .factura("http://newUrl1.com")
                        .retencion("http://newUrl2.com")
                        .guia("http://newUrl3.com")
                        .build()
                )
                .sunatCredentials(SunatCredentialsDto.builder()
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
                .put("/" + projectId + "/companies/" + companyId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void deleteCompany() {
        // Given
        String projectId = "1";
        String companyId = "11";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectId + "/companies/" + companyId)
                .then()
                .statusCode(204);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/companies/" + companyId)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteCompanyByIncorrectProject_shouldNotBeAllowed() {
        // Given
        String projectId = "1";
        String companyId = "33";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectId + "/companies/" + companyId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void getCompanies() {
        // Given
        String projectId = "1";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/companies")
                .then()
                .statusCode(200)
                .body("size()", is(2),
                        "[0].name", is("company2"),
                        "[1].name", is("company1")
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

}


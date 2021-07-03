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
package io.github.project.openubl.xsender.resources;

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.CompanyRepresentationBuilder;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.resources.config.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(MinioServer.class)
@QuarkusTestResource(ArtemisServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@TestHTTPEndpoint(CompanyResource.class)
public class CompanyResourceTest extends BaseKeycloakTest {

    @Test
    public void getCompany() {
        // Given
        String nsId = "1";
        String companyId = "11";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()),
                        "name", is("company1"),
                        "ruc", is("11111111111"),
                        "webServices.factura", is("http://urlFactura1"),
                        "webServices.guia", is("http://urlGuia1"),
                        "webServices.retenciones", is("http://urlPercepcionRetencion1")
                );
        // Then
    }

    @Test
    public void getCompanyByNotOwner_shouldNotBeAllowed() {
        // Given
        String nsId = "3";
        String companyId = "44";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(200);
        // Then
    }

    @Test
    public void getCompanyThatBelongsToOtherNamespace_shouldNotBeAllowed() {
        // Given
        String nsOwnerId = "1";
        String nsToTestId = "2";

        String companyId = "11";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsOwnerId + "/companies/" + companyId)
                .then()
                .statusCode(200);

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsToTestId + "/companies/" + companyId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void createCompany() {
        // Given
        String nsId = "1";

        CompanyRepresentation company = CompanyRepresentationBuilder.aCompanyRepresentation()
                .withName("My company")
                .withRuc("12345678910")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://url1.com")
                        .withGuia("http://url2.com")
                        .withRetenciones("http://url3.com")
                        .build()
                )
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("myUsername")
                        .withPassword("myPassword")
                        .build()
                )
                .build();

        // When
        CompanyRepresentation response = given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(company)
                .when()
                .post("/" + nsId + "/companies")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()),
                        "name", is(company.getName()),
                        "webServices.factura", is(company.getWebServices().getFactura()),
                        "webServices.guia", is(company.getWebServices().getGuia()),
                        "webServices.retenciones", is(company.getWebServices().getRetenciones()),
                        "credentials.username", is(company.getCredentials().getUsername()),
                        "credentials.password", nullValue()
                ).extract().body().as(CompanyRepresentation.class);

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies/" + response.getId())
                .then()
                .statusCode(200)
                .body("id", is(response.getId()),
                        "name", is(company.getName()),
                        "webServices.factura", is(company.getWebServices().getFactura()),
                        "webServices.guia", is(company.getWebServices().getGuia()),
                        "webServices.retenciones", is(company.getWebServices().getRetenciones()),
                        "credentials.username", is(company.getCredentials().getUsername()),
                        "credentials.password", nullValue()
                );
    }

    @Test
    public void createCompanyByNotNsOwner_shouldNotBeAllowed() {
        // Given
        String nsId = "3";

        CompanyRepresentation company = CompanyRepresentationBuilder.aCompanyRepresentation()
                .withName("My company")
                .withRuc("12345678910")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://url1.com")
                        .withGuia("http://url2.com")
                        .withRetenciones("http://url3.com")
                        .build()
                )
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("myUsername")
                        .withPassword("myPassword")
                        .build()
                )
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(company)
                .when()
                .post("/" + nsId + "/companies")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void create2CompaniesWithSameRuc_shouldNotBeAllowed() {
        // Given
        String nsId = "1";

        CompanyRepresentation company = CompanyRepresentationBuilder.aCompanyRepresentation()
                .withName("My company")
                .withRuc("11111111111")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://url1.com")
                        .withGuia("http://url2.com")
                        .withRetenciones("http://url3.com")
                        .build()
                )
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("myUsername")
                        .withPassword("myPassword")
                        .build()
                )
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(company)
                .when()
                .post("/" + nsId + "/companies")
                .then()
                .statusCode(409);
        // Then
    }

    @Test
    public void updateCompany() {
        // Given
        String nsId = "1";
        String companyId = "11";

        CompanyRepresentation companyRepresentation = CompanyRepresentationBuilder.aCompanyRepresentation()
                .withRuc("99999999999")
                .withName("new name")
                .withDescription("new description")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://newUrl1.com")
                        .withRetenciones("http://newUrl2.com")
                        .withGuia("http://newUrl3.com")
                        .build()
                )
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("new username")
                        .withPassword("new password")
                        .build()
                )
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(companyRepresentation)
                .when()
                .put("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(200)
                .body("ruc", is(companyRepresentation.getRuc()),
                        "name", is(companyRepresentation.getName()),
                        "description", is(companyRepresentation.getDescription()),
                        "webServices.factura", is(companyRepresentation.getWebServices().getFactura()),
                        "webServices.retenciones", is(companyRepresentation.getWebServices().getRetenciones()),
                        "webServices.guia", is(companyRepresentation.getWebServices().getGuia()),
                        "credentials.username", is(companyRepresentation.getCredentials().getUsername()),
                        "credentials.password", is(nullValue())
                );

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(200)
                .body("id", is(companyId),
                        "ruc", is(companyRepresentation.getRuc()),
                        "name", is(companyRepresentation.getName()),
                        "description", is(companyRepresentation.getDescription()),
                        "webServices.factura", is(companyRepresentation.getWebServices().getFactura()),
                        "webServices.retenciones", is(companyRepresentation.getWebServices().getRetenciones()),
                        "webServices.guia", is(companyRepresentation.getWebServices().getGuia()),
                        "credentials.username", is(companyRepresentation.getCredentials().getUsername()),
                        "credentials.password", is(nullValue())
                );
    }

    @Test
    public void updateCompanyByNotOwner_shouldNotBeAllowed() {
        String nsId = "3";
        String companyId = "44";

        // Given
        CompanyRepresentation companyRepresentation = CompanyRepresentationBuilder.aCompanyRepresentation()
                .withRuc("99999999999")
                .withName("new name")
                .withDescription("new description")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://newUrl1.com")
                        .withRetenciones("http://newUrl2.com")
                        .withGuia("http://newUrl3.com")
                        .build()
                )
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("new username")
                        .withPassword("new password")
                        .build()
                )
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(companyRepresentation)
                .when()
                .put("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .body(companyRepresentation)
                .when()
                .put("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(200);
        // Then
    }

    @Test
    public void updateCompanyWithIncorrectNs_shouldNotBeAllowed() {
        String nsId = "1";
        String companyId = "33";

        // Given
        CompanyRepresentation companyRepresentation = CompanyRepresentationBuilder.aCompanyRepresentation()
                .withRuc("99999999999")
                .withName("new name")
                .withDescription("new description")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://newUrl1.com")
                        .withRetenciones("http://newUrl2.com")
                        .withGuia("http://newUrl3.com")
                        .build()
                )
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("new username")
                        .withPassword("new password")
                        .build()
                )
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(companyRepresentation)
                .when()
                .put("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void deleteCompany() {
        // Given
        String nsId = "1";
        String companyId = "11";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(204);

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteCompanyByNotOwner_shouldNotBeAllowed() {
        // Given
        String nsId = "3";
        String companyId = "44";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(204);

        // Then
    }

    @Test
    public void deleteCompanyByIncorrectNs_shouldNotBeAllowed() {
        // Given
        String nsId = "1";
        String companyId = "33";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId + "/companies/" + companyId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void searchCompanies() {
        // Given
        String nsId = "1";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "data.size()", is(2),
                        "data[0].name", is("company2"),
                        "data[1].name", is("company1")
                );

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies?sort_by=createdOn:asc")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "data.size()", is(2),
                        "data[0].name", is("company1"),
                        "data[1].name", is("company2")
                );
        // Then
    }

    @Test
    public void searchCompanies_filterTextByName() {
        // Given
        String nsId = "1";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies?filterText=company1")
                .then()
                .statusCode(200)
                .body("meta.count", is(1),
                        "data.size()", is(1),
                        "data[0].name", is("company1")
                );
        // Then
    }

    @Test
    public void searchCompanies_filterTextByRuc() {
        // Given
        String nsId = "1";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/companies?filterText=222222")
                .then()
                .statusCode(200)
                .body("meta.count", is(1),
                        "data.size()", is(1),
                        "data[0].name", is("company2")
                );
        // Then
    }

}


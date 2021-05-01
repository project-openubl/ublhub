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
package io.github.project.openubl.xsender.basic.resources;

import io.github.project.openubl.xsender.basic.resources.config.*;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@QuarkusTestResource(KafkaServer.class)
@QuarkusTestResource(ApicurioRegistryServer.class)
public class CompanyResourceTest extends BaseKeycloakTest {

    @Inject
    CompanyRepository companyRepository;

    @AfterEach
    public void afterEach() {
        companyRepository.deleteAll();
    }

    @Test
    public void deleteCompany() {
        // Given
        final String COMPANY_NAME = "mycompany";

        SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
                .withSunatUsername("anyUsername")
                .withSunatPassword("anyPassword")
                .build();
        SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
                .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
                .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
                .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
                .build();

        CompanyEntity company1 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName(COMPANY_NAME)
                .withOwner("alice")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();

        companyRepository.persist(company1);

        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        // When

        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .delete("/api/companies/" + COMPANY_NAME)
                .then()
                .statusCode(204);

        // Then
        companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertFalse(companyOptional.isPresent());
    }

    @Test
    public void deleteCompany_usingNonExistsCompany() {
        // Given
        // When
        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .delete("/api/companies/" + "any")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void deleteCompany_byNotOwner() {
        // Given
        final String COMPANY_NAME = "mycompany";

        SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
                .withSunatUsername("anyUsername")
                .withSunatPassword("anyPassword")
                .build();
        SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
                .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
                .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
                .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
                .build();

        CompanyEntity company1 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName(COMPANY_NAME)
                .withOwner("someUser")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();

        companyRepository.persist(company1);

        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .delete("/api/companies/" + COMPANY_NAME)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void getCompany() {
        // Given
        final String COMPANY_NAME = "mycompany";

        SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
                .withSunatUsername("anyUsername")
                .withSunatPassword("anyPassword")
                .build();
        SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
                .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
                .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
                .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
                .build();

        CompanyEntity company1 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName(COMPANY_NAME)
                .withOwner("alice")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();

        companyRepository.persist(company1);

        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .get("/api/companies/" + COMPANY_NAME)
                .then()
                .statusCode(200)
                .body("name", is(COMPANY_NAME),
                        "webServices.factura", is(urls.getSunatUrlFactura()),
                        "webServices.guia", is(urls.getSunatUrlGuiaRemision()),
                        "webServices.retenciones", is(urls.getSunatUrlPercepcionRetencion()),
                        "credentials.username", is(credentials.getSunatUsername()),
                        "credentials.password", is(nullValue())
                );
        // Then
    }

    @Test
    public void getCompany_usingNonExistsCompany() {
        // Given
        // When
        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .get("/api/companies/" + "any")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void getCompany_usingNotOwner() {
        // Given
        final String COMPANY_NAME = "mycompany";

        SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
                .withSunatUsername("anyUsername")
                .withSunatPassword("anyPassword")
                .build();
        SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
                .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
                .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
                .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
                .build();

        CompanyEntity company1 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName(COMPANY_NAME)
                .withOwner("someUser")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();

        companyRepository.persist(company1);

        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .get("/api/companies/" + COMPANY_NAME)
                .then()
                .statusCode(404);
        // Then
    }
}

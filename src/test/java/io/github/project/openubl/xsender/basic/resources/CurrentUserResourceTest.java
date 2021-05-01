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

import io.github.project.openubl.xsender.basic.resources.config.BaseKeycloakTest;
import io.github.project.openubl.xsender.basic.resources.config.KafkaServer;
import io.github.project.openubl.xsender.basic.resources.config.KeycloakServer;
import io.github.project.openubl.xsender.basic.resources.config.PostgreSQLServer;
import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.OutboxEventEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@QuarkusTestResource(KafkaServer.class)
public class CurrentUserResourceTest extends BaseKeycloakTest {

    @Inject
    CompanyRepository companyRepository;

    @BeforeEach
    public void beforeEach() {
        companyRepository.deleteAll();
    }

    @Test
    public void createCompany() {
        // Given
        final String COMPANY_NAME = "mycompany";

        CompanyRepresentation company = CompanyRepresentation.Builder.aCompanyRepresentation()
                .withName(COMPANY_NAME)
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
                .accept(ContentType.JSON)
                .body(company)
                .when()
                .post("/api/user/companies")
                .then()
                .statusCode(200)
                .body("name", is(company.getName()));

        // Then
        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        CompanyEntity companyDB = companyOptional.get();
        assertEquals(companyDB.getName(), COMPANY_NAME);
        assertEquals(companyDB.getOwner(), "alice");
        assertEquals(companyDB.getSunatUrls().getSunatUrlFactura(), "http://url1.com");
        assertEquals(companyDB.getSunatUrls().getSunatUrlGuiaRemision(), "http://url2.com");
        assertEquals(companyDB.getSunatUrls().getSunatUrlPercepcionRetencion(), "http://url3.com");
        assertEquals(companyDB.getSunatCredentials().getSunatUsername(), "myUsername");
        assertEquals(companyDB.getSunatCredentials().getSunatPassword(), "myPassword");

        OutboxEventEntity kafkaMsg = OutboxEventEntity.findByParams(EntityType.company.toString(), companyDB.getId(), EventType.CREATED.toString());
        assertNotNull(kafkaMsg);
    }

    @Test
    public void createDuplicateCompany() {
        // Given
        final String COMPANY_NAME = "mycompany";

        CompanyRepresentation company = CompanyRepresentation.Builder.aCompanyRepresentation()
                .withName(COMPANY_NAME)
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
                .accept(ContentType.JSON)
                .body(company)
                .when()
                .post("/api/user/companies")
                .then()
                .statusCode(200)
                .body("name", is(company.getName()));

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(company)
                .when()
                .post("/api/user/companies")
                .then()
                .statusCode(409);
    }

    @Test
    public void getCompanies() {
        SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
                .withSunatUsername("anyUsername")
                .withSunatPassword("anyPassword")
                .build();
        SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
                .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
                .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
                .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
                .build();

        // Given
        long currentTime = new Date().getTime();

        CompanyEntity company1 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-company1")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime))
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();
        CompanyEntity company2 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-company2")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime + 9_000L))
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();
        CompanyEntity company3 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-company3")
                .withOwner("anotherUser")
                .withCreatedOn(new Date(currentTime + 18_000L))
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();

        companyRepository.persist(company1, company2, company3);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .get("/api/user/companies")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "data.size()", is(2),
                        "data[0].name", is("my-company2"),
                        "data[1].name", is("my-company1")
                );
    }

    @Test
    public void getCompanies_filterText() {
        SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
                .withSunatUsername("anyUsername")
                .withSunatPassword("anyPassword")
                .build();
        SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
                .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
                .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
                .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
                .build();

        // Given
        long currentTime = new Date().getTime();

        CompanyEntity company1 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-company1")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime))
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();
        CompanyEntity company2 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-company2")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime + 9_000L))
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();
        CompanyEntity company3 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-company3")
                .withOwner("anotherUser")
                .withCreatedOn(new Date(currentTime + 18_000L))
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();

        companyRepository.persist(company1, company2, company3);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .header("Content-Type", "application/json")
                .when()
                .get("/api/user/companies?filterText=company1")
                .then()
                .statusCode(200)
                .body("meta.count", is(1),
                        "data.size()", is(1),
                        "data[0].name", is("my-company1")
                );
    }

}


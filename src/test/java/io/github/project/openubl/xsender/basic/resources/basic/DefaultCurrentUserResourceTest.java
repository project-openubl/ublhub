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
package io.github.project.openubl.xsender.basic.resources.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.github.project.openubl.xsender.security.UserIdentity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DefaultCurrentUserResourceTest {

    @Inject
    CompanyRepository companyRepository;

    @AfterEach
    public void afterEach() {
        companyRepository.deleteAll();
    }

    @Test
    public void createCompany() throws JsonProcessingException {
        // Given
        final String COMPANY_NAME = "myCompany";

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

        String body = new ObjectMapper().writeValueAsString(company);

        // When
        given()
                .body(body)
                .header("Content-Type", "application/json")
                .when()
                .post("/user/companies")
                .then()
                .statusCode(200)
                .body("name", is(company.getName()));

        // Then
        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        CompanyEntity companyDB = companyOptional.get();
        assertEquals(companyDB.getName(), COMPANY_NAME);
        assertEquals(companyDB.getOwner(), UserIdentity.DEFAULT_USERNAME);
        assertEquals(companyDB.getSunatUrls().getSunatUrlFactura(), "http://url1.com");
        assertEquals(companyDB.getSunatUrls().getSunatUrlGuiaRemision(), "http://url2.com");
        assertEquals(companyDB.getSunatUrls().getSunatUrlPercepcionRetencion(), "http://url3.com");
        assertEquals(companyDB.getSunatCredentials().getSunatUsername(), "myUsername");
        assertEquals(companyDB.getSunatCredentials().getSunatPassword(), "myPassword");
    }

    @Test
    public void getCompanies() {
        // Given
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
                .withName("myCompany1")
                .withOwner(UserIdentity.DEFAULT_USERNAME)
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();
        CompanyEntity company2 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("myCompany2")
                .withOwner(UserIdentity.DEFAULT_USERNAME)
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();
        CompanyEntity company3 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("myCompany3")
                .withOwner("anotherUser")
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .build();

        companyRepository.persist(company1, company2, company3);

        // When
        given()
                .header("Content-Type", "application/json")
                .when()
                .get("/user/companies")
                .then()
                .statusCode(200)
                .body("meta.offset", is(0),
                        "meta.limit", is(10),
                        "meta.count", is(2),
                        "links.first", is(notNullValue()),
                        "links.last", is(notNullValue()),
                        "links.next", is(nullValue()),
                        "links.previous", is(nullValue()),
                        "data.size()", is(2),
                        "data[0].name", is("myCompany1"),
                        "data[1].name", is("myCompany2")
                );
    }
}

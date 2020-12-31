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
package io.github.project.openubl.xsender.basic.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xsender.basic.resources.config.BaseKeycloakTest;
import io.github.project.openubl.xsender.basic.resources.config.KeycloakServer;
import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
public class CompanyResourceTest extends BaseKeycloakTest {

    @Inject
    CompanyRepository companyRepository;

    @AfterEach
    public void afterEach() {
        companyRepository.deleteAll();
    }

    @Test
    public void deleteCompany() throws JsonProcessingException {
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
        given().auth().oauth2(getAccessToken("alice"))
                .body(body)
                .header("Content-Type", "application/json")
                .when()
                .post("/api/user/companies")
                .then()
                .statusCode(200)
                .body("name", is(company.getName().toLowerCase()));

        // Then
        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME.toLowerCase());
        assertTrue(companyOptional.isPresent());

        CompanyEntity companyDB = companyOptional.get();
        assertEquals(companyDB.getName(), COMPANY_NAME.toLowerCase());
        assertEquals(companyDB.getOwner(), "alice");
        assertEquals(companyDB.getSunatUrls().getSunatUrlFactura(), "http://url1.com");
        assertEquals(companyDB.getSunatUrls().getSunatUrlGuiaRemision(), "http://url2.com");
        assertEquals(companyDB.getSunatUrls().getSunatUrlPercepcionRetencion(), "http://url3.com");
        assertEquals(companyDB.getSunatCredentials().getSunatUsername(), "myUsername");
        assertEquals(companyDB.getSunatCredentials().getSunatPassword(), "myPassword");
    }

}

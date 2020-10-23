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
package io.github.project.openubl.xsender.authz.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xsender.authz.BaseAuthzTest;
import io.github.project.openubl.xsender.authz.KeycloakServer;
import io.github.project.openubl.xsender.core.models.OrganizationType;
import io.github.project.openubl.xsender.core.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.core.resources.Paths;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
public class AuthzCurrentUserResourceTest extends BaseAuthzTest {

    @Inject
    CompanyRepository companyRepository;

    @AfterEach
    public void afterEach() {
        companyRepository.deleteAll();
    }

    @Test
    public void getCurrentUserOrganizations() {
        // Given
        final String USERNAME = "alice";

        CompanyEntity organization1 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("company1")
                .withOwner("unknownUser")
                .build();

        CompanyEntity organization2 = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("company2")
                .withOwner(USERNAME)
                .build();

        companyRepository.persist(Arrays.asList(organization1, organization2));

        // When

        // Then
        RestAssured.given().auth().oauth2(getAccessToken(USERNAME))
                .when()
                .get(Paths.USER + "/companies")
                .then()
                .statusCode(200)
                .body("meta.offset", is(0),
                        "meta.limit", is(10),
                        "meta.count", is(1),
                        "links.first", is(notNullValue()),
                        "links.last", is(notNullValue()),
                        "links.next", is(nullValue()),
                        "links.previous", is(nullValue()),
                        "data.size()", is(1),
                        "data[0].name", is("company2")
                );
    }

    @Test
    public void createCompany() throws JsonProcessingException {
        // Given
        final String USERNAME = "alice";
        final String COMPANY_NAME = "myCompany";

        CompanyEntity company = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName(COMPANY_NAME)
                .build();

        String body = new ObjectMapper().writeValueAsString(company);

        // When
        RestAssured.given().auth().oauth2(getAccessToken(USERNAME))
                .when()
                .header("Content-Type", "application/json")
                .body(body)
                .post(Paths.USER + "/companies")
                .then()
                .statusCode(200)
                .body("name", is(company.getName()));

        // Then
        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        CompanyEntity companyDB = companyOptional.get();
        assertEquals(companyDB.getName(), COMPANY_NAME);
        assertEquals(companyDB.getOwner(), USERNAME);
    }
}

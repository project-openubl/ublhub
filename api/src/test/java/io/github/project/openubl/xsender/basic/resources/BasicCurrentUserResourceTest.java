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
import io.github.project.openubl.xsender.basic.Constants;
import io.github.project.openubl.xsender.core.models.OrganizationType;
import io.github.project.openubl.xsender.core.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.core.resources.Paths;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class BasicCurrentUserResourceTest {

    @Inject
    CompanyRepository companyRepository;

    @AfterEach
    public void afterEach() {
        companyRepository.deleteAll();
    }

    @Test
    public void getCompanies() {
        CompanyEntity company = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("myCompany")
                .withOwner(Constants.DEFAULT_USERNAME)
                .build();
        companyRepository.persist(company);

        given()
                .header("Content-Type", "application/json")
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
                        "data[0].name", is("myCompany")
                );
    }

    @Test
    public void createCompany() throws JsonProcessingException {
        // Given
        final String COMPANY_NAME = "myCompany";

        CompanyEntity company = CompanyEntity.Builder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName(COMPANY_NAME)
                .build();

        String body = new ObjectMapper().writeValueAsString(company);

        // When
        given()
                .body(body)
                .header("Content-Type", "application/json")
                .when()
                .post(Paths.USER + "/companies")
                .then()
                .statusCode(200)
                .body("name", is(company.getName()));

        // Then
        Optional<CompanyEntity> companyOptional = companyRepository.findByName(COMPANY_NAME);
        assertTrue(companyOptional.isPresent());

        CompanyEntity companyDB = companyOptional.get();
        assertEquals(companyDB.getName(), COMPANY_NAME);
        assertEquals(companyDB.getOwner(), Constants.DEFAULT_USERNAME);
    }
}

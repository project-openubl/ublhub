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
package io.github.project.openubl.xsender.basic.resources.admin;

import io.github.project.openubl.xsender.basic.Constants;
import io.github.project.openubl.xsender.core.models.OrganizationType;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.github.project.openubl.xsender.core.resources.Paths;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class BasicOrganizationsResourceTest {

    @Inject
    OrganizationRepository organizationRepository;

    @BeforeEach
    public void beforeAll() {
        OrganizationEntity organization = OrganizationEntity.Builder.anOrganizationEntity()
                .withId(UUID.randomUUID().toString())
                .withName("myOrganization")
                .withOwner("myOwner")
                .withType(OrganizationType.DETACHED)
                .build();

        organizationRepository.persist(organization);
    }

    @Test
    public void listOrganizations() {
        given()
                .header("Content-Type", "application/json")
                .when()
                .get(Paths.ORGANIZATIONS)
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
                        "data[0].name", is(Constants.DEFAULT_USERNAME)
                );
    }
}

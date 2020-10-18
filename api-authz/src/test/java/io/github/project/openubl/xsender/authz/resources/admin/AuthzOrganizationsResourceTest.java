/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * <p>
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.eclipse.org/legal/epl-2.0/
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.authz.resources.admin;

import io.github.project.openubl.xsender.authz.BaseAuthzTest;
import io.github.project.openubl.xsender.authz.KeycloakServer;
import io.github.project.openubl.xsender.core.models.OrganizationType;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.github.project.openubl.xsender.core.resources.Paths;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
public class AuthzOrganizationsResourceTest extends BaseAuthzTest {

    @Inject
    OrganizationRepository organizationRepository;

    @BeforeEach
    public void beforeEach() {
        OrganizationEntity organization = OrganizationEntity.Builder.anOrganizationEntity()
                .withId(UUID.randomUUID().toString())
                .withName("myAuthzOrganization")
                .withOwner("myAuthzOwner")
                .withType(OrganizationType.DETACHED)
                .build();

        organizationRepository.persist(organization);
    }

    @AfterEach
    public void afterEach() {
        organizationRepository.deleteAll();
    }

    @Test
    public void listOrganizationsAsAdmin() {
        RestAssured.given().auth().oauth2(getAccessToken("admin"))
                .when().get(Paths.ORGANIZATIONS)
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
                        "data[0].name", is("myAuthzOrganization")
                );
    }

    @Test
    public void listOrganizationsAsuser() {
        RestAssured.given().auth().oauth2(getAccessToken("alice"))
                .when().get(Paths.ORGANIZATIONS)
                .then()
                .statusCode(403);
    }
}

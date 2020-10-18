package io.github.project.openubl.xsender.authz.resources;

import io.github.project.openubl.xsender.authz.BaseAuthzTest;
import io.github.project.openubl.xsender.authz.KeycloakServer;
import io.github.project.openubl.xsender.core.models.OrganizationType;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.github.project.openubl.xsender.core.resources.Paths;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
public class AuthzAuthenticatedUserResourceTest extends BaseAuthzTest {

    @Inject
    OrganizationRepository organizationRepository;

    @Test
    public void initUser() {
        RestAssured.given().auth().oauth2(getAccessToken("admin"))
                .when()
                .get(Paths.USER)
                .then()
                .statusCode(200)
                .body(is("admin"));

        Optional<OrganizationEntity> organizationOptional = organizationRepository.findByName("admin");
        assertTrue(organizationOptional.isPresent());

        OrganizationEntity organization = organizationOptional.get();
        assertEquals(organization.getName(), "admin");
        assertEquals(organization.getOwner(), "admin");
        assertEquals(organization.getType(), OrganizationType.USER);
    }

    @Test
    public void getCurrentUserOrganizations() {
        RestAssured.given().auth().oauth2(getAccessToken("admin"))
                .when()
                .get(Paths.USER + "/orgs")
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
                        "data[0].name", is("admin")
                );
    }
}

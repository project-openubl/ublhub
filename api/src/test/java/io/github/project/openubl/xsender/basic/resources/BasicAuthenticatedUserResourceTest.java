package io.github.project.openubl.xsender.basic.resources;

import io.github.project.openubl.xsender.basic.Constants;
import io.github.project.openubl.xsender.core.resources.Paths;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class BasicAuthenticatedUserResourceTest {

    @Test
    public void initUser() {
        given()
                .header("Content-Type", "application/json")
                .when()
                .get(Paths.USER)
                .then()
                .statusCode(200)
                .body(is(Constants.DEFAULT_USERNAME));
    }

    @Test
    public void getCurrentUserOrganizations() {
        given()
                .header("Content-Type", "application/json")
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
                        "data[0].name", is(Constants.DEFAULT_USERNAME)
                );
    }

}

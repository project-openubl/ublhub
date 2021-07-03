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
package io.github.project.openubl.xsender.resources;

import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.idm.NamespaceRepresentationBuilder;
import io.github.project.openubl.xsender.resources.config.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(MinioServer.class)
@QuarkusTestResource(ArtemisServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@TestHTTPEndpoint(NamespaceResource.class)
public class NamespaceResourceTest extends BaseKeycloakTest {

    @Test
    public void getNamespace() {
        // Given
        String nsId = "1";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId)
                .then()
                .statusCode(200)
                .body("name", is("my-namespace1"),
                        "description", is("description1")
                );

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + 10)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void getNamespaceByNotOwner_shouldReturnNotFound() {
        // Given
        String nsId = "3";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .header("Content-Type", "application/json")
                .when()
                .get("/" + nsId)
                .then()
                .statusCode(200);
        // Then
    }

    @Test
    public void updateNamespace() {
        // Given
        String nsId = "1";
        NamespaceRepresentation newNamespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName("new name")
                .withDescription("my description")
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(newNamespace)
                .when()
                .put("/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "name", is(newNamespace.getName()),
                        "description", is(newNamespace.getDescription())
                );

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "name", is(newNamespace.getName()),
                        "description", is(newNamespace.getDescription())
                );
    }

    @Test
    public void updateNamespaceByNotOwner_shouldNotBeAllowed() {
        // Given
        String nsId = "3";
        NamespaceRepresentation newNamespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName("new name")
                .withDescription("my description")
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(newNamespace)
                .when()
                .put("/" + nsId)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newNamespace)
                .when()
                .put("/" + nsId)
                .then()
                .statusCode(200);
        // Then
    }

    @Test
    public void deleteNamespace() {
        // Given
        String nsId = "1";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId)
                .then()
                .statusCode(204);

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteNamespaceByNotOwner_shouldNotBeAllowed() {
        // Given
        String nsId = "3";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId)
                .then()
                .statusCode(204);
        // Then
    }

}


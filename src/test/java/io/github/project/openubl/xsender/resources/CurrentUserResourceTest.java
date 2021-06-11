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

import com.radcortez.flyway.test.annotation.DataSource;
import com.radcortez.flyway.test.annotation.FlywayTest;
import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.resources.config.BaseKeycloakTest;
import io.github.project.openubl.xsender.resources.config.KeycloakServer;
import io.github.project.openubl.xsender.resources.config.PostgreSQLServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(CurrentUserResource.class)
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@FlywayTest(@DataSource(QuarkusDataSourceProvider.class))
public class CurrentUserResourceTest extends BaseKeycloakTest {

    @Inject
    NamespaceRepository namespaceRepository;

    @Test
    public void createNamespace() {
        // Given
        final String NAME = "mynamespace";

        NamespaceRepresentation namespace = NamespaceRepresentation.NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName(NAME)
                .withDescription("my description")
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/namespaces")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()),
                        "name", is(namespace.getName())
                );

        // Then
    }

    @Test
    public void createDuplicateNamespace() {
        // Given
        final String NAME = "mynamespace";

        NamespaceRepresentation namespace = NamespaceRepresentation.NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName(NAME)
                .build();

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/namespaces")
                .then()
                .statusCode(200)
                .body("name", is(namespace.getName()));

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/namespaces")
                .then()
                .statusCode(409);
    }

}


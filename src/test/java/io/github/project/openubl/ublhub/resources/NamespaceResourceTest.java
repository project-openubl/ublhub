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
package io.github.project.openubl.ublhub.resources;

import io.github.project.openubl.ublhub.ProfileManager;
import io.github.project.openubl.ublhub.idm.NamespaceRepresentation;
import io.github.project.openubl.ublhub.idm.NamespaceRepresentationBuilder;
import io.github.project.openubl.ublhub.AbstractBaseTest;
import io.github.project.openubl.ublhub.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.ublhub.idm.SunatUrlsRepresentation;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class NamespaceResourceTest extends AbstractBaseTest {

    @Override
    public Class<?> getTestClass() {
        return NamespaceResourceTest.class;
    }

    @Test
    public void getNamespace() {
        // Given
        String nsId = "1";

        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId)
                .then()
                .statusCode(200)
                .body("name", is("my-namespace1"),
                        "description", is("description1"),
                        "webServices.factura", is("http://url1"),
                        "webServices.guia", is("http://url11"),
                        "webServices.retenciones", is("http://url111"),
                        "credentials.username", is("username1"),
                        "credentials.password", nullValue()
                );

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + 10)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void updateNamespace_NameAndDescription() {
        // Given
        String nsId = "1";
        NamespaceRepresentation namespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName("new name")
                .withDescription("my description")
                .build();

        // When
        given()
                .contentType(ContentType.JSON)
                .body(namespace)
                .when()
                .put("/api/namespaces/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "name", is(namespace.getName()),
                        "description", is(namespace.getDescription())
                );

        // Then
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "name", is(namespace.getName()),
                        "description", is(namespace.getDescription())
                );
    }

    @Test
    public void updateNamespace_urls() {
        // Given
        String nsId = "1";
        NamespaceRepresentation namespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://url1Changed.com")
                        .withGuia("http://url2Changed.com")
                        .withRetenciones("http://url3Changed.com")
                        .build()
                )
                .build();

        // When
        given()
                .contentType(ContentType.JSON)
                .body(namespace)
                .when()
                .put("/api/namespaces/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "webServices.factura", is(namespace.getWebServices().getFactura()),
                        "webServices.guia", is(namespace.getWebServices().getGuia()),
                        "webServices.retenciones", is(namespace.getWebServices().getRetenciones())
                );

        // Then
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "webServices.factura", is(namespace.getWebServices().getFactura()),
                        "webServices.guia", is(namespace.getWebServices().getGuia()),
                        "webServices.retenciones", is(namespace.getWebServices().getRetenciones())
                );
    }

    @Test
    public void updateNamespace_credentials() {
        // Given
        String nsId = "1";
        NamespaceRepresentation namespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("myNewUsername")
                        .withPassword("myNewPassword")
                        .build()
                )
                .build();

        // When
        given()
                .contentType(ContentType.JSON)
                .body(namespace)
                .when()
                .put("/api/namespaces/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "credentials.username", is(namespace.getCredentials().getUsername()),
                        "credentials.password", nullValue()
                );

        // Then
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "credentials.username", is(namespace.getCredentials().getUsername()),
                        "credentials.password", nullValue()
                );
    }

    @Test
    public void deleteNamespace() {
        // Given
        String nsId = "1";

        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/namespaces/" + nsId)
                .then()
                .statusCode(204);

        // Then
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId)
                .then()
                .statusCode(404);
    }

}


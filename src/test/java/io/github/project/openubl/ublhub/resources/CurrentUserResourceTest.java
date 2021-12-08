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

import io.github.project.openubl.ublhub.AbstractBaseTest;
import io.github.project.openubl.ublhub.ProfileManager;
import io.github.project.openubl.ublhub.idm.NamespaceRepresentation;
import io.github.project.openubl.ublhub.idm.NamespaceRepresentationBuilder;
import io.github.project.openubl.ublhub.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.ublhub.idm.SunatUrlsRepresentation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class CurrentUserResourceTest extends AbstractBaseTest {

    @Override
    public Class<?> getTestClass() {
        return CurrentUserResourceTest.class;
    }

    @Test
    public void createNamespace() {
        // Given
        final String NAME = "mynamespace";

        NamespaceRepresentation namespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName(NAME)
                .withDescription("my description")
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

        // When
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/api/user/namespaces")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()),
                        "name", is(namespace.getName()),
                        "description", is(namespace.getDescription()),
                        "webServices.factura", is(namespace.getWebServices().getFactura()),
                        "webServices.guia", is(namespace.getWebServices().getGuia()),
                        "webServices.retenciones", is(namespace.getWebServices().getRetenciones()),
                        "credentials.username", is(namespace.getCredentials().getUsername()),
                        "credentials.password", nullValue()
                );

        // Then
    }

    @Test
    public void createDuplicateNamespace() {
        // Given
        final String NAME = "mynamespace";

        NamespaceRepresentation namespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName(NAME)
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

        // When
        given()
                .contentType(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/api/user/namespaces")
                .then()
                .statusCode(200)
                .body("name", is(namespace.getName()));

        // Then
        given()
                .contentType(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/api/user/namespaces")
                .then()
                .statusCode(409);
    }

    @Test
    public void getNamespaces() {
        // Given
        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/user/namespaces")
                .then()
                .statusCode(200)
                .body("meta.count", is(3),
                        "data.size()", is(3),
                        "data[0].name", is("my-namespace3"),
                        "data[1].name", is("my-namespace2"),
                        "data[2].name", is("my-namespace1")
                );
        // Then
    }

    @Test
    public void getNamespaces_filterText() {
        // Given
        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/user/namespaces?filterText=namespace1")
                .then()
                .statusCode(200)
                .body("meta.count", is(1),
                        "data.size()", is(1),
                        "data[0].name", is("my-namespace1")
                );
        //then
    }
}


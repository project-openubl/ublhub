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
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.github.project.openubl.xsender.resources.config.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@QuarkusTestResource(StorageServer.class)
@QuarkusTestResource(SenderServer.class)
public class CurrentUserResourceTest extends BaseKeycloakTest {

    @Inject
    NamespaceRepository namespaceRepository;

    @BeforeEach
    public void beforeEach() {
        namespaceRepository.deleteAll();
    }

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
                .post("/api/user/namespaces")
                .then()
                .statusCode(200)
                .body("name", is(namespace.getName()));

        // Then
        Optional<NamespaceEntity> namespaceOptional = namespaceRepository.findByName(NAME);
        assertTrue(namespaceOptional.isPresent());

        NamespaceEntity dbEntity = namespaceOptional.get();
        assertEquals(dbEntity.getName(), NAME);
        assertEquals(dbEntity.getDescription(), "my description");
        assertEquals(dbEntity.getOwner(), "alice");
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
                .post("/api/user/namespaces")
                .then()
                .statusCode(200)
                .body("name", is(namespace.getName()));

        // Then
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/api/user/namespaces")
                .then()
                .statusCode(409);
    }

    @Test
    public void getNamespaces() {
        // Given
        long currentTime = new Date().getTime();

        NamespaceEntity namespace1 = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace1")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime))
                .build();
        NamespaceEntity namespace2 = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace2")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime + 9_000L))
                .build();
        NamespaceEntity namespace3 = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace3")
                .withOwner("anotherUser")
                .withCreatedOn(new Date(currentTime + 18_000L))
                .build();

        namespaceRepository.persist(namespace1, namespace2, namespace3);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/user/namespaces")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "data.size()", is(2),
                        "data[0].name", is("my-namespace2"),
                        "data[1].name", is("my-namespace1")
                );
    }

    @Test
    public void getNamespaces_filterText() {
        SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
                .withSunatUsername("anyUsername")
                .withSunatPassword("anyPassword")
                .build();
        SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
                .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
                .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
                .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
                .build();

        // Given
        long currentTime = new Date().getTime();

        NamespaceEntity namespace1 = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace1")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime))
                .build();
        NamespaceEntity namespace2 = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace2")
                .withOwner("alice")
                .withCreatedOn(new Date(currentTime + 9_000L))
                .build();
        NamespaceEntity namespace3 = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace3")
                .withOwner("anotherUser")
                .withCreatedOn(new Date(currentTime + 18_000L))
                .build();

        namespaceRepository.persist(namespace1, namespace2, namespace3);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/user/namespaces?filterText=namespace1")
                .then()
                .statusCode(200)
                .body("meta.count", is(1),
                        "data.size()", is(1),
                        "data[0].name", is("my-namespace1")
                );
    }

}


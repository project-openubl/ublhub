/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class NamespaceKeysResourceTest extends AbstractBaseTest {

    @Override
    public Class<?> getTestClass() {
        return NamespaceKeysResourceTest.class;
    }

//    @Test
//    public void createNamespace_shouldCreateKeys() {
//        // Given
//        final String NAME = "mynamespace";
//
//        NamespaceDto namespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
//                .withName(NAME)
//                .withCredentials(SunatCredentials.Builder.aSunatCredentialsRepresentation()
//                        .withUsername("myUsername")
//                        .withPassword("myPassword")
//                        .build()
//                )
//                .withWebServices(SunatWebServicesDto.Builder.aSunatUrlsRepresentation()
//                        .withFactura("http://url1.com")
//                        .withGuia("http://url2.com")
//                        .withRetenciones("http://url3.com")
//                        .build()
//                )
//                .build();
//
//        // When
//        namespace = given()
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body(namespace)
//                .when()
//                .post("/api/namespaces")
//                .then()
//                .statusCode(201)
//                .body("id", is(notNullValue()),
//                        "name", is(namespace.getName())
//                )
//                .extract()
//                .body().as(NamespaceDto.class);
//
//        // Then
//        KeysMetadataRepresentation keys = given()
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body(namespace)
//                .when()
//                .get("/api/namespaces/" + namespace.getId() + "/keys")
//                .then()
//                .statusCode(200)
//                .body("active.RS256", is(notNullValue()),
//                        "keys.size()", is(1),
//                        "keys[0].status", is("ACTIVE"),
//                        "keys[0].type", is("RSA"),
//                        "keys[0].algorithm", is("RS256"),
//                        "keys[0].use", is("SIG"),
//                        "keys[0].kid", is(notNullValue()),
//                        "keys[0].publicKey", is(notNullValue()),
//                        "keys[0].certificate", is(notNullValue()),
//                        "keys[0].providerId", is(notNullValue()),
//                        "keys[0].providerPriority", is(100)
//                )
//                .extract()
//                .body()
//                .as(KeysMetadataRepresentation.class);
//
//        given()
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body(namespace)
//                .when()
//                .get("/api/namespaces/" + namespace.getId() + "/components")
//                .then()
//                .statusCode(200)
//                .body("size()", is(1),
//                        "[0].id", is(notNullValue()),
//                        "[0].name", is("rsa-generated"),
//                        "[0].providerId", is("rsa-generated"),
//                        "[0].providerType", is(KeyProvider.class.getName()),
//                        "[0].parentId", is(namespace.getId()),
//                        "[0].subType", is(nullValue()),
//                        "[0].config.priority[0]", is("100")
//                );
//    }

}


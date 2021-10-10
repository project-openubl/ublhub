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

import io.github.project.openubl.xsender.BaseAuthTest;
import io.github.project.openubl.xsender.ProfileManager;
import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.idm.NamespaceRepresentationBuilder;
import io.github.project.openubl.xsender.keys.KeyProvider;
import io.github.project.openubl.xsender.keys.component.ComponentModel;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.KeysMetadataRepresentation;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class NamespaceKeysResourceTest extends BaseAuthTest {

    @Test
    public void createNamespace_shouldCreateKeys() {
        // Given
        final String NAME = "mynamespace";

        NamespaceRepresentation namespace = NamespaceRepresentationBuilder.aNamespaceRepresentation()
                .withName(NAME)
                .build();

        // When
        namespace = givenAuth("alice")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .post("/api/user/namespaces")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()),
                        "name", is(namespace.getName())
                )
                .extract()
                .body().as(NamespaceRepresentation.class);

        // Then
        KeysMetadataRepresentation keys = givenAuth("alice")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .get("/api/namespaces/" + namespace.getId() + "/keys")
                .then()
                .statusCode(200)
                .body("active.RS256", is(notNullValue()),
                        "keys.size()", is(1),
                        "keys[0].status", is("ACTIVE"),
                        "keys[0].type", is("RSA"),
                        "keys[0].algorithm", is("RS256"),
                        "keys[0].use", is("SIG"),
                        "keys[0].kid", is(notNullValue()),
                        "keys[0].publicKey", is(notNullValue()),
                        "keys[0].certificate", is(notNullValue()),
                        "keys[0].providerId", is(notNullValue()),
                        "keys[0].providerPriority", is(100)
                )
                .extract()
                .body()
                .as(KeysMetadataRepresentation.class);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespace)
                .when()
                .get("/api/namespaces/" + namespace.getId() + "/components")
                .then()
                .statusCode(200)
                .body("size()", is(1),
                        "[0].id", is(notNullValue()),
                        "[0].name", is("rsa-generated"),
                        "[0].providerId", is("rsa-generated"),
                        "[0].providerType", is(KeyProvider.class.getName()),
                        "[0].parentId", is(namespace.getId()),
                        "[0].subType", is(nullValue()),
                        "[0].config.priority[0]", is("100")
                );
    }

}


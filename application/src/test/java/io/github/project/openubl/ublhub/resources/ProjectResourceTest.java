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
import io.github.project.openubl.ublhub.ProductionTestProfile;
import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatDto;
import io.github.project.openubl.ublhub.keys.GeneratedRsaKeyProviderFactory;
import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProductionTestProfile.class)
@TestHTTPEndpoint(ProjectResource.class)
public class ProjectResourceTest extends AbstractBaseTest {

    @BeforeEach
    public void beforeEach() {
        cleanDB();
    }

    @Test
    public void createProject() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .description("my description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .body("name", is(projectDto.getName()),
                        "description", is(projectDto.getDescription()),
                        "sunat.facturaUrl", is(projectDto.getSunat().getFacturaUrl()),
                        "sunat.guiaUrl", is(projectDto.getSunat().getGuiaUrl()),
                        "sunat.retencionUrl", is(projectDto.getSunat().getRetencionUrl()),
                        "sunat.username", is(projectDto.getSunat().getUsername()),
                        "sunat.password", nullValue()
                );

        // Then
    }

    @Test
    public void createProject_duplicate() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .body("name", is(projectDto.getName()));

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(409);
    }

    @Test
    public void getProjects() {
        // Given
        Arrays.asList("alice", "bob").forEach(username -> {
            IntStream.rangeClosed(1, 3).forEach(projectIndex -> {
                ProjectDto projectDto = ProjectDto.builder()
                        .name("project-" + projectIndex + "-" + username)
                        .sunat(SunatDto.builder()
                                .facturaUrl("http://url1.com")
                                .guiaUrl("http://url2.com")
                                .retencionUrl("http://url3.com")
                                .username("myUsername")
                                .password("myPassword")
                                .build()
                        )
                        .build();

                givenAuth(username)
                        .contentType(ContentType.JSON)
                        .body(projectDto)
                        .when()
                        .post("/")
                        .then()
                        .statusCode(201);
            });
        });

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body("size()", is(3),
                        "[0].name", is("project-3-alice"),
                        "[1].name", is("project-2-alice"),
                        "[2].name", is("project-1-alice")
                );
        // Then
    }

    @Test
    public void getProject() {
        // Given
        String projectName = "my-project1";
        ProjectDto projectDto = ProjectDto.builder()
                .name(projectName)
                .description("description1")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://factura1")
                        .guiaUrl("http://guia1")
                        .retencionUrl("http://percepcionRetencion1")
                        .username("username1")
                        .password("password1")
                        .build()
                )
                .build();

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectName)
                .then()
                .statusCode(200)
                .body("name", is(projectName),
                        "description", is("description1"),
                        "sunat.facturaUrl", is("http://factura1"),
                        "sunat.guiaUrl", is("http://guia1"),
                        "sunat.retencionUrl", is("http://percepcionRetencion1"),
                        "sunat.username", is("username1"),
                        "sunat.password", nullValue()
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectName)
                .then()
                .statusCode(404);
    }

    @Test
    public void getProject_notFound() {
        // Given
        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/10")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void updateProject() {
        // Given
        String projectName = "my-project";
        ProjectDto projectDto = ProjectDto.builder()
                .name(projectName)
                .description("description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://factura")
                        .guiaUrl("http://guia")
                        .retencionUrl("http://percepcionRetencion")
                        .username("username")
                        .password("password")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        projectDto = ProjectDto.builder()
                .name("new name")
                .description("my description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1Changed.com")
                        .guiaUrl("http://url2Changed.com")
                        .retencionUrl("http://url3Changed.com")
                        .username("new username")
                        .password("new password")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .put("/" + projectName)
                .then()
                .statusCode(200)
                .body("name", is(projectName), // Name has not changed
                        "description", is(projectDto.getDescription()),
                        "sunat.facturaUrl", is(projectDto.getSunat().getFacturaUrl()),
                        "sunat.guiaUrl", is(projectDto.getSunat().getGuiaUrl()),
                        "sunat.retencionUrl", is(projectDto.getSunat().getRetencionUrl()),
                        "sunat.username", is(projectDto.getSunat().getUsername()),
                        "sunat.password", nullValue()
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .put("/" + projectName)
                .then()
                .statusCode(404);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectName)
                .then()
                .statusCode(200)
                .body("name", is(projectName),
                        "description", is(projectDto.getDescription()),
                        "sunat.facturaUrl", is(projectDto.getSunat().getFacturaUrl()),
                        "sunat.guiaUrl", is(projectDto.getSunat().getGuiaUrl()),
                        "sunat.retencionUrl", is(projectDto.getSunat().getRetencionUrl()),
                        "sunat.username", is(projectDto.getSunat().getUsername()),
                        "sunat.password", nullValue()
                );
    }

    @Test
    public void deleteProject() {
        // Given
        String projectName = "my-project";
        ProjectDto projectDto = ProjectDto.builder()
                .name(projectName)
                .description("description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://factura")
                        .guiaUrl("http://guia")
                        .retencionUrl("http://percepcionRetencion")
                        .username("username")
                        .password("password")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectName)
                .then()
                .statusCode(404);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectName)
                .then()
                .statusCode(204);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectName)
                .then()
                .statusCode(404);
    }

    @Test
    public void getProjectKeys() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .description("my description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/keys")
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
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectDto.getName() + "/keys")
                .then()
                .statusCode(404);
    }

    @Test
    public void createProjectComponent() {
        // Given
        String projectName = "myproject";
        ProjectDto projectDto = ProjectDto.builder()
                .name(projectName)
                .description("my description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectName + "/components/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectName),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectName + "/components/")
                .then()
                .statusCode(404);
    }

    @Test
    public void getProjectComponent() {
        // Given
        String projectName = "myproject";
        ProjectDto projectDto = ProjectDto.builder()
                .name(projectName)
                .description("my description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        String componentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectName + "/components/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectName + "/components/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectName),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectName + "/components/" + componentId)
                .then()
                .statusCode(404);
    }

    @Test
    public void updateProjectComponent() {
        // Given
        String projectName = "myproject";
        ProjectDto projectDto = ProjectDto.builder()
                .name(projectName)
                .description("my description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        String componentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectName + "/components/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        componentDto = ComponentDto.builder()
                .name("myNewKeyname")
                .config(new HashMap<>() {{
                    put("active", List.of("false"));
                    put("algorithm", List.of("RS512"));
                    put("enabled", List.of("false"));
                    put("keySize", List.of("4096"));
                    put("priority", List.of("222"));
                }})
                .build();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .body(componentDto)
                .put("/" + projectName + "/components/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectName),
                        "providerId", is(GeneratedRsaKeyProviderFactory.ID),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("false"),
                        "config.algorithm[0]", is("RS512"),
                        "config.enabled[0]", is("false"),
                        "config.keySize[0]", is("4096"),
                        "config.priority[0]", is("222")
                );

        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .body(componentDto)
                .put("/" + projectName + "/components/" + componentId)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteProjectComponent() {
        // Given
        String projectName = "myproject";
        ProjectDto projectDto = ProjectDto.builder()
                .name(projectName)
                .description("my description")
                .sunat(SunatDto.builder()
                        .facturaUrl("http://url1.com")
                        .guiaUrl("http://url2.com")
                        .retencionUrl("http://url3.com")
                        .username("myUsername")
                        .password("myPassword")
                        .build()
                )
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of("RS256"));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        String componentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectName + "/components/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectName + "/components/" + componentId)
                .then()
                .statusCode(204);
        givenAuth("bob")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectName + "/components/" + componentId)
                .then()
                .statusCode(404);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectName + "/components/" + componentId)
                .then()
                .statusCode(404);
    }
}


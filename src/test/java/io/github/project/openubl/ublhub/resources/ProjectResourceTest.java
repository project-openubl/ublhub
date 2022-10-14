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
import io.github.project.openubl.ublhub.BasicProfileManager;
import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatDto;
import io.github.project.openubl.ublhub.keys.GeneratedRsaKeyProviderFactory;
import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

@QuarkusTest
@TestProfile(BasicProfileManager.class)
@TestHTTPEndpoint(ProjectResource.class)
public class ProjectResourceTest extends AbstractBaseTest {

    @Override
    public Class<?> getTestClass() {
        return ProjectResourceTest.class;
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
                .body("id", is(notNullValue()),
                        "name", is(projectDto.getName()),
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
        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body("size()", is(3),
                        "[0].name", is("my-project3"),
                        "[1].name", is("my-project2"),
                        "[2].name", is("my-project1")
                );
        // Then
    }

    @Test
    public void getProject() {
        // Given
        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/1")
                .then()
                .statusCode(200)
                .body("name", is("my-project1"),
                        "description", is("description1"),
                        "sunat.facturaUrl", is("http://url1"),
                        "sunat.guiaUrl", is("http://url11"),
                        "sunat.retencionUrl", is("http://url111"),
                        "sunat.username", is("username1"),
                        "sunat.password", nullValue()
                );
        // Then
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
        String projectId = "1";
        ProjectDto projectDto = ProjectDto.builder()
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

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .put("/" + projectId)
                .then()
                .statusCode(200)
                .body("id", is(projectId),
                        "name", is(projectDto.getName()),
                        "description", is(projectDto.getDescription()),
                        "sunat.facturaUrl", is(projectDto.getSunat().getFacturaUrl()),
                        "sunat.guiaUrl", is(projectDto.getSunat().getGuiaUrl()),
                        "sunat.retencionUrl", is(projectDto.getSunat().getRetencionUrl()),
                        "sunat.username", is(projectDto.getSunat().getUsername()),
                        "sunat.password", nullValue()
                );

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId)
                .then()
                .statusCode(200)
                .body("id", is(projectId),
                        "name", is(projectDto.getName()),
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
        String projectId = "1";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectId)
                .then()
                .statusCode(204);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId)
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
        String projectId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/keys")
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
    }

    @Test
    public void createProjectKey() {
        // Given
        String projectId = "1";

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>(){{
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
                .post("/" + projectId + "/keys/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectId),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );
    }

    @Test
    public void getProjectKey() {
        // Given
        String projectId = "1";

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>(){{
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
                .post("/" + projectId + "/keys/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/keys/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectId),
                        "providerId", is(componentDto.getProviderId()),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("true"),
                        "config.algorithm[0]", is("RS256"),
                        "config.enabled[0]", is("true"),
                        "config.keySize[0]", is("2048"),
                        "config.priority[0]", is("111")
                );
    }

    @Test
    public void updateProjectKey() {
        // Given
        String projectId = "1";

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>(){{
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
                .post("/" + projectId + "/keys/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        componentDto = ComponentDto.builder()
                .name("myNewKeyname")
                .config(new HashMap<>(){{
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
                .put("/" + projectId + "/keys/" + componentId)
                .then()
                .statusCode(200)
                .body("id", is(componentId),
                        "name", is(componentDto.getName()),
                        "parentId", is(projectId),
                        "providerId", is(GeneratedRsaKeyProviderFactory.ID),
                        "providerType", is(KeyProvider.class.getName()),
                        "config.active[0]", is("false"),
                        "config.algorithm[0]", is("RS512"),
                        "config.enabled[0]", is("false"),
                        "config.keySize[0]", is("4096"),
                        "config.priority[0]", is("222")
                );
    }

    @Test
    public void deleteProjectKey() {
        // Given
        String projectId = "1";

        // When
        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>(){{
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
                .post("/" + projectId + "/keys/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + projectId + "/keys/" + componentId)
                .then()
                .statusCode(204);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/keys/" + componentId)
                .then()
                .statusCode(404);
    }
}


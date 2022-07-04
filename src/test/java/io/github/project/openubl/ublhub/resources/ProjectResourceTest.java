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
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatCredentialsDto;
import io.github.project.openubl.ublhub.dto.SunatWebServicesDto;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

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
                .sunatWebServices(SunatWebServicesDto.builder()
                        .factura("http://url1.com")
                        .guia("http://url2.com")
                        .retencion("http://url3.com")
                        .build()
                )
                .sunatCredentials(SunatCredentialsDto.builder()
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
                        "sunatWebServices.factura", is(projectDto.getSunatWebServices().getFactura()),
                        "sunatWebServices.guia", is(projectDto.getSunatWebServices().getGuia()),
                        "sunatWebServices.retencion", is(projectDto.getSunatWebServices().getRetencion()),
                        "sunatCredentials.username", is(projectDto.getSunatCredentials().getUsername()),
                        "sunatCredentials.password", nullValue()
                );

        // Then
    }

    @Test
    public void createProject_duplicate() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .sunatWebServices(SunatWebServicesDto.builder()
                        .factura("http://url1.com")
                        .guia("http://url2.com")
                        .retencion("http://url3.com")
                        .build()
                )
                .sunatCredentials(SunatCredentialsDto.builder()
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
                        "sunatWebServices.factura", is("http://url1"),
                        "sunatWebServices.guia", is("http://url11"),
                        "sunatWebServices.retencion", is("http://url111"),
                        "sunatCredentials.username", is("username1"),
                        "sunatCredentials.password", nullValue()
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
        String nsId = "1";
        ProjectDto projectDto = ProjectDto.builder()
                .name("new name")
                .description("my description")
                .sunatWebServices(SunatWebServicesDto.builder()
                        .factura("http://url1Changed.com")
                        .guia("http://url2Changed.com")
                        .retencion("http://url3Changed.com")
                        .build()
                )
                .sunatCredentials(SunatCredentialsDto.builder()
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
                .put("/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "name", is(projectDto.getName()),
                        "description", is(projectDto.getDescription()),
                        "sunatWebServices.factura", is(projectDto.getSunatWebServices().getFactura()),
                        "sunatWebServices.guia", is(projectDto.getSunatWebServices().getGuia()),
                        "sunatWebServices.retencion", is(projectDto.getSunatWebServices().getRetencion()),
                        "sunatCredentials.username", is(projectDto.getSunatCredentials().getUsername()),
                        "sunatCredentials.password", nullValue()
                );

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId)
                .then()
                .statusCode(200)
                .body("id", is(nsId),
                        "name", is(projectDto.getName()),
                        "description", is(projectDto.getDescription()),
                        "sunatWebServices.factura", is(projectDto.getSunatWebServices().getFactura()),
                        "sunatWebServices.guia", is(projectDto.getSunatWebServices().getGuia()),
                        "sunatWebServices.retencion", is(projectDto.getSunatWebServices().getRetencion()),
                        "sunatCredentials.username", is(projectDto.getSunatCredentials().getUsername()),
                        "sunatCredentials.password", nullValue()
                );
    }

    @Test
    public void deleteProject() {
        // Given
        String nsId = "1";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .delete("/" + nsId)
                .then()
                .statusCode(204);

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId)
                .then()
                .statusCode(404);
    }

}


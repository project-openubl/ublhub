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
import io.github.project.openubl.ublhub.ResourceHelpers;
import io.github.project.openubl.ublhub.dto.DocumentInputDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatDto;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog6;
import io.github.project.openubl.xbuilder.content.models.common.Cliente;
import io.github.project.openubl.xbuilder.content.models.common.Proveedor;
import io.github.project.openubl.xbuilder.content.models.standard.general.DocumentoVentaDetalle;
import io.github.project.openubl.xbuilder.content.models.standard.general.Invoice;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProductionTestProfile.class)
@TestHTTPEndpoint(DocumentResource.class)
public class DocumentResourceTest extends AbstractBaseTest {

    @Inject
    ResourceHelpers resourceHelpers;

    final int TIMEOUT = 60;

    final static SunatDto sunatDto = SunatDto.builder()
            .facturaUrl("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
            .guiaUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
            .retencionUrl("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem")
            .username("MODDATOS")
            .password("MODDATOS")
            .build();

    final static Invoice invoice = Invoice.builder()
            .serie("F001")
            .numero(1)
            .proveedor(Proveedor.builder()
                    .ruc("12345678912")
                    .razonSocial("Softgreen S.A.C.")
                    .build()
            )
            .cliente(Cliente.builder()
                    .nombre("Carlos Feria")
                    .numeroDocumentoIdentidad("12121212121")
                    .tipoDocumentoIdentidad(Catalog6.RUC.toString())
                    .build()
            )
            .detalle(DocumentoVentaDetalle.builder()
                    .descripcion("Item1")
                    .cantidad(new BigDecimal(10))
                    .precio(new BigDecimal(100))
                    .build()
            )
            .detalle(DocumentoVentaDetalle.builder()
                    .descripcion("Item2")
                    .cantidad(new BigDecimal(10))
                    .precio(new BigDecimal(100))
                    .build()
            )
            .build();

    @BeforeEach
    public void beforeEach() {
        cleanDB();
        resourceHelpers.generatePreexistingData();
    }

    @Test
    public void getDocument() {
        // Given
        String project = ResourceHelpers.projects.get(0);
        Long documentId = ResourceHelpers.projectDocumentIds.get(project).get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("id", is(documentId.toString()));
        // Then
    }

    @Test
    public void getDocumentThatBelongsToOtherNamespace_shouldNotBeAllowed() {
        // Given
        String projectOwnerId = ResourceHelpers.projects.get(0);
        String projectToTestId = ResourceHelpers.projects.get(1);

        Long documentId = ResourceHelpers.projectDocumentIds.get(projectOwnerId).get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectOwnerId + "/documents/" + documentId)
                .then()
                .statusCode(200);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectToTestId + "/documents/" + documentId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void searchDocuments() {
        // Given
        String project = ResourceHelpers.projects.get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents")
                .then()
                .statusCode(200)
                .body("count", is(2),
                        "items.size()", is(2),
                        "items[0].status.xmlData.serieNumero", is("F-2"),
                        "items[1].status.xmlData.serieNumero", is("F-1")
                );

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents?sort_by=created:asc")
                .then()
                .statusCode(200)
                .body("count", is(2),
                        "items.size()", is(2),
                        "items[0].status.xmlData.serieNumero", is("F-1"),
                        "items[1].status.xmlData.serieNumero", is("F-2")
                );
        // Then
    }

    @Test
    public void searchDocuments_filterTextByName() {
        // Given
        String project = ResourceHelpers.projects.get(0);

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents?filterText=1")
                .then()
                .statusCode(200)
                .body("count", is(1),
                        "items.size()", is(1),
                        "items[0].status.xmlData.serieNumero", is("F-1")
                );
        // Then
    }

    @Test
    public void createInvoiceWithDefaultSignAlgorithm() {
        // Given
        String project = "myproject";

        ProjectDto projectDto = ProjectDto.builder()
                .name(project)
                .description("my description")
                .sunat(sunatDto)
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        JsonObject inputDto = Json.createObjectBuilder()
                .add("kind", DocumentInputDto.Kind.Invoice.toString())
                .add("spec", Json.createObjectBuilder()
                        .add("document", toJavax(invoice))
                        .build()
                )
                .build();

        // Then
        String documentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(inputDto.toString())
                .when()
                .post("/" + project + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "status.inProgress", is(true)
                )
                .extract().path("id").toString();

        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            String inProgress = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/" + project + "/documents/" + documentId)
                    .then()
                    .statusCode(200)
                    .extract().path("status.inProgress").toString();
            return !Boolean.parseBoolean(inProgress);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("status.inProgress", is(false),
                        "status.xmlData.ruc", is("12345678912"),
                        "status.xmlData.serieNumero", is("F001-1"),
                        "status.xmlData.tipoDocumento", is("Invoice"),
                        "status.error", is(nullValue()),
                        "status.sunat.code", is(0),
                        "status.sunat.ticket", is(nullValue()),
                        "status.sunat.status", is("ACEPTADO"),
                        "status.sunat.description", is("La Factura numero F001-1, ha sido aceptada"),
                        "status.sunat.hasCdr", is(true),
                        "status.sunat.notes", is(Collections.emptyList())
                );
    }

//    @Test
//    public void createInvoiceWithCustomSignAlgorithm() {
//        // Given
//        ProjectDto projectDto = ProjectDto.builder()
//                .name("myproject")
//                .description("my description")
//                .sunat(sunatDto)
//                .build();
//
//        String projectId = givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .body(projectDto)
//                .when()
//                .post("/")
//                .then()
//                .statusCode(201)
//                .body("id", is(notNullValue()))
//                .extract().path("id").toString();
//
//        ComponentDto componentDto = ComponentDto.builder()
//                .name("myKey")
//                .providerId(GeneratedRsaKeyProviderFactory.ID)
//                .config(new HashMap<>() {{
//                    put("active", List.of("true"));
//                    put("algorithm", List.of(Algorithm.RS512));
//                    put("enabled", List.of("true"));
//                    put("keySize", List.of("2048"));
//                    put("priority", List.of("111"));
//                }})
//                .build();
//
//        givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .body(componentDto)
//                .when()
//                .post("/" + projectId + "/components/")
//                .then()
//                .statusCode(201)
//                .body("config.algorithm[0]", is(Algorithm.RS512));
//
//        // When
//        DocumentInputDto inputDto = DocumentInputDto.builder()
//                .kind(DocumentInputDto.Kind.Invoice)
//                .spec(DocumentInputDto.Spec.builder()
//                        .id(null)
//                        .signature(DocumentInputDto.Signature.builder()
//                                .algorithm(Algorithm.RS512)
//                                .build()
//                        )
//                        .document(JsonObject.mapFrom(invoice))
//                        .build()
//                )
//                .build();
//
//        // Then
//        String documentId = givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .body(JsonObject.mapFrom(inputDto).toString())
//                .when()
//                .post("/" + projectId + "/documents")
//                .then()
//                .statusCode(201)
//                .body("id", is(notNullValue()),
//                        "status.inProgress", is(true)
//                )
//                .extract().path("id").toString();
//
//
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            String inProgress = givenAuth("alice")
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/" + projectId + "/documents/" + documentId)
//                    .then()
//                    .statusCode(200)
//                    .extract().path("status.inProgress").toString();
//            return !Boolean.parseBoolean(inProgress);
//        });
//
//        givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/" + projectId + "/documents/" + documentId)
//                .then()
//                .statusCode(200)
//                .body("status.inProgress", is(false),
//                        "status.xmlData.ruc", is("12345678912"),
//                        "status.xmlData.serieNumero", is("F001-1"),
//                        "status.xmlData.tipoDocumento", is("Invoice"),
//                        "status.error", is(nullValue()),
//                        "status.sunat.code", is(0),
//                        "status.sunat.ticket", is(nullValue()),
//                        "status.sunat.status", is("ACEPTADO"),
//                        "status.sunat.description", is("La Factura numero F001-1, ha sido aceptada"),
//                        "status.sunat.hasCdr", is(true),
//                        "status.sunat.notes", is(Collections.emptyList())
//                );
//    }

//    @Test
//    public void createInvoiceWithCustomSignAlgorithm_CertificateNotFound() {
//        // Given
//        ProjectDto projectDto = ProjectDto.builder()
//                .name("myproject")
//                .description("my description")
//                .sunat(sunatDto)
//                .build();
//
//        String projectId = givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .body(projectDto)
//                .when()
//                .post("/")
//                .then()
//                .statusCode(201)
//                .body("id", is(notNullValue()))
//                .extract().path("id").toString();
//
//        // When
//        DocumentInputDto inputDto = DocumentInputDto.builder()
//                .kind(DocumentInputDto.Kind.Invoice)
//                .spec(DocumentInputDto.Spec.builder()
//                        .id(null)
//                        .signature(DocumentInputDto.Signature.builder()
//                                .algorithm(Algorithm.RS512)
//                                .build()
//                        )
//                        .document(JsonObject.mapFrom(invoice))
//                        .build()
//                )
//                .build();
//
//        // Then
//        givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .body(JsonObject.mapFrom(inputDto).toString())
//                .when()
//                .post("/" + projectId + "/documents")
//                .then()
//                .statusCode(400);
//    }

//    @Test
//    public void createInvoiceWithAutoIDGenerator() {
//        // Given
//        ProjectDto projectDto = ProjectDto.builder()
//                .name("myproject")
//                .description("my description")
//                .sunat(sunatDto)
//                .build();
//
//        String projectId = givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .body(projectDto)
//                .when()
//                .post("/")
//                .then()
//                .statusCode(201)
//                .body("id", is(notNullValue()))
//                .extract().path("id").toString();
//
//        // When
//        Invoice invoice = Invoice.builder()
//                .proveedor(Proveedor.builder()
//                        .ruc("12345678912")
//                        .razonSocial("Softgreen S.A.C.")
//                        .build()
//                )
//                .cliente(Cliente.builder()
//                        .nombre("Carlos Feria")
//                        .numeroDocumentoIdentidad("12121212121")
//                        .tipoDocumentoIdentidad(Catalog6.RUC.toString())
//                        .build()
//                )
//                .detalle(DocumentoVentaDetalle.builder()
//                        .descripcion("Item1")
//                        .cantidad(new BigDecimal(10))
//                        .precio(new BigDecimal(100))
//                        .build()
//                )
//                .detalle(DocumentoVentaDetalle.builder()
//                        .descripcion("Item2")
//                        .cantidad(new BigDecimal(10))
//                        .precio(new BigDecimal(100))
//                        .build()
//                )
//                .build();
//
//        DocumentInputDto inputDto = DocumentInputDto.builder()
//                .kind(DocumentInputDto.Kind.Invoice)
//                .spec(DocumentInputDto.Spec.builder()
//                        .id(DocumentInputDto.ID.builder()
//                                .type(IDGeneratorType.generated)
//                                .config(Map.of(
//                                        "isFactura", "true",
//                                        "minSerie", "3",
//                                        "minNumero", "99"
//                                ))
//                                .build()
//                        )
//                        .signature(null)
//                        .document(JsonObject.mapFrom(invoice))
//                        .build()
//                )
//                .build();
//
//        // Then
//        String documentId = givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .body(JsonObject.mapFrom(inputDto).toString())
//                .when()
//                .post("/" + projectId + "/documents")
//                .then()
//                .statusCode(201)
//                .body("id", is(notNullValue()),
//                        "status.inProgress", is(true)
//                )
//                .extract().path("id").toString();
//
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            String inProgress = givenAuth("alice")
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/" + projectId + "/documents/" + documentId)
//                    .then()
//                    .statusCode(200)
//                    .extract().path("status.inProgress").toString();
//            return !Boolean.parseBoolean(inProgress);
//        });
//
//        givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/" + projectId + "/documents/" + documentId)
//                .then()
//                .statusCode(200)
//                .body("status.inProgress", is(false),
//                        "status.xmlData.ruc", is("12345678912"),
//                        "status.xmlData.serieNumero", is("F003-99"),
//                        "status.xmlData.tipoDocumento", is("Invoice"),
//                        "status.error", is(nullValue()),
//                        "status.sunat.code", is(0),
//                        "status.sunat.ticket", is(nullValue()),
//                        "status.sunat.status", is("ACEPTADO"),
//                        "status.sunat.description", is("La Factura numero F003-99, ha sido aceptada"),
//                        "status.sunat.hasCdr", is(true),
//                        "status.sunat.notes", is(Collections.emptyList())
//                );
//    }

    @Test
    public void uploadXml_notXMLFileShouldBeRejected() throws URISyntaxException {
        // Given
        String project = ResourceHelpers.projects.get(0);

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("images/java-icon.png").toURI();
        File file = new File(fileURI);

        // When
        // Then
        givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + project + "/upload/document")
                .then()
                .statusCode(400);
    }

    @Test
    public void uploadXML_notUblXMLFileShouldBeRejected() throws URISyntaxException {
        // Given
        String project = ResourceHelpers.projects.get(0);

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        File file = new File(fileURI);

        // When
        givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + project + "/upload/document")
                .then()
                .statusCode(400);
    }

    @Test
    public void createInvoiceAndPrint() {
        // Given
        String project = "myproject";

        ProjectDto projectDto = ProjectDto.builder()
                .name(project)
                .description("my description")
                .sunat(sunatDto)
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        JsonObject inputDto = Json.createObjectBuilder()
                .add("kind", DocumentInputDto.Kind.Invoice.toString())
                .add("spec", Json.createObjectBuilder()
                        .add("document", toJavax(invoice))
                        .build()
                )
                .build();

        String documentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(inputDto.toString())
                .when()
                .post("/" + project + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents/" + documentId + "/print")
                .then()
                .statusCode(200);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents/" + documentId + "/print?format=html")
                .then()
                .statusCode(200);
    }

    @Test
    public void enrichInvoice() {
        // Given
        String project = "myproject";

        ProjectDto projectDto = ProjectDto.builder()
                .name(project)
                .description("my description")
                .sunat(sunatDto)
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        JsonObject inputDto = Json.createObjectBuilder()
                .add("kind", DocumentInputDto.Kind.Invoice.toString())
                .add("spec", Json.createObjectBuilder()
                        .add("document", toJavax(invoice))
                        .build()
                )
                .build();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(inputDto.toString())
                .when()
                .post("/" + project + "/enrich-document")
                .then()
                .statusCode(200)
                .body("moneda", is("PEN"));
    }

    @Test
    public void renderInvoice() {
        // Given
        String project = "myproject";

        ProjectDto projectDto = ProjectDto.builder()
                .name(project)
                .description("my description")
                .sunat(sunatDto)
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        JsonObject inputDto = Json.createObjectBuilder()
                .add("kind", DocumentInputDto.Kind.Invoice.toString())
                .add("spec", Json.createObjectBuilder()
                        .add("document", toJavax(invoice))
                        .build()
                )
                .build();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(inputDto.toString())
                .when()
                .post("/" + project + "/render-document")
                .then()
                .statusCode(200);
    }
}


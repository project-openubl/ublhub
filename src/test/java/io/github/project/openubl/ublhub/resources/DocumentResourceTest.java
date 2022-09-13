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
import io.github.project.openubl.ublhub.dto.DocumentInputDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatCredentialsDto;
import io.github.project.openubl.ublhub.dto.SunatWebServicesDto;
import io.github.project.openubl.ublhub.keys.GeneratedRsaKeyProviderFactory;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog6;
import io.github.project.openubl.xbuilder.content.models.common.Cliente;
import io.github.project.openubl.xbuilder.content.models.common.Proveedor;
import io.github.project.openubl.xbuilder.content.models.standard.general.DocumentoDetalle;
import io.github.project.openubl.xbuilder.content.models.standard.general.Invoice;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.keycloak.crypto.Algorithm;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.github.project.openubl.ublhub.models.JobPhaseType.READ_XML_FILE;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

@QuarkusTest
@TestProfile(BasicProfileManager.class)
@TestHTTPEndpoint(DocumentResource.class)
public class DocumentResourceTest extends AbstractBaseTest {

    final int TIMEOUT = 60;


    final static SunatWebServicesDto sunatWebServicesDto = SunatWebServicesDto.builder()
            .factura("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
            .guia("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
            .retencion("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService")
            .build();

    final static SunatCredentialsDto sunatCredentialsDto = SunatCredentialsDto.builder()
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
            .detalle(DocumentoDetalle.builder()
                    .descripcion("Item1")
                    .cantidad(new BigDecimal(10))
                    .precio(new BigDecimal(100))
                    .build()
            )
            .detalle(DocumentoDetalle.builder()
                    .descripcion("Item2")
                    .cantidad(new BigDecimal(10))
                    .precio(new BigDecimal(100))
                    .build()
            )
            .build();

    @Override
    public Class<?> getTestClass() {
        return DocumentResourceTest.class;
    }

//    @Test
//    public void getDocument() {
//        // Given
//        String nsId = "1";
//        String documentId = "11";
//
//        // When
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents/" + documentId)
//                .then()
//                .statusCode(200)
//                .body("id", is(documentId),
//                        "jobInProgress", is(false),
//                        "created", is(notNullValue()),
//                        "updated", is(nullValue())
//                );
//        // Then
//    }
//
//    @Test
//    public void getDocumentThatBelongsToOtherNamespace_shouldNotBeAllowed() {
//        // Given
//        String nsOwnerId = "1";
//        String nsToTestId = "2";
//
//        String documentId = "11";
//
//        // When
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsOwnerId + "/documents/" + documentId)
//                .then()
//                .statusCode(200);
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsToTestId + "/documents/" + documentId)
//                .then()
//                .statusCode(404);
//        // Then
//    }
//
//    @Test
//    public void searchDocuments() {
//        // Given
//        String nsId = "1";
//
//        // When
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents")
//                .then()
//                .statusCode(200)
//                .body("meta.count", is(2),
//                        "items.size()", is(2),
//                        "items[0].id", is("22"),
//                        "items[1].id", is("11")
//                );
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents?sort_by=created:asc")
//                .then()
//                .statusCode(200)
//                .body("meta.count", is(2),
//                        "items.size()", is(2),
//                        "items[0].id", is("11"),
//                        "items[1].id", is("22")
//                );
//        // Then
//    }
//
//    @Test
//    public void searchDocuments_filterTextByName() {
//        // Given
//        String nsId = "1";
//
//        // When
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents?filterText=11")
//                .then()
//                .statusCode(200)
//                .body("meta.count", is(1),
//                        "items.size()", is(1),
//                        "items[0].xmlFileContent.serieNumero", is("F-11")
//                );
//        // Then
//    }

    @Test
    public void createInvoiceWithDefaultSignAlgorithm() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .description("my description")
                .sunatWebServices(sunatWebServicesDto)
                .sunatCredentials(sunatCredentialsDto)
                .build();

        String projectId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // When
        DocumentInputDto inputDto = DocumentInputDto.builder()
                .kind(DocumentInputDto.Kind.Invoice)
                .spec(DocumentInputDto.Spec.builder()
                        .id(null)
                        .signature(null)
                        .document(JsonObject.mapFrom(invoice))
                        .build()
                )
                .build();

        // Then
        String documentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(inputDto).toString())
                .when()
                .post("/" + projectId + "/documents")
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
                    .get("/" + projectId + "/documents/" + documentId)
                    .then()
                    .statusCode(200)
                    .extract().path("status.inProgress").toString();
            return !Boolean.parseBoolean(inProgress);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/documents/" + documentId)
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

    @Test
    public void createInvoiceWithCustomSignAlgorithm() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .description("my description")
                .sunatWebServices(sunatWebServicesDto)
                .sunatCredentials(sunatCredentialsDto)
                .build();

        String projectId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        ComponentDto componentDto = ComponentDto.builder()
                .name("myKey")
                .providerId(GeneratedRsaKeyProviderFactory.ID)
                .config(new HashMap<>() {{
                    put("active", List.of("true"));
                    put("algorithm", List.of(Algorithm.RS512));
                    put("enabled", List.of("true"));
                    put("keySize", List.of("2048"));
                    put("priority", List.of("111"));
                }})
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(componentDto)
                .when()
                .post("/" + projectId + "/keys/")
                .then()
                .statusCode(201)
                .body("config.algorithm[0]", is(Algorithm.RS512));

        // When
        DocumentInputDto inputDto = DocumentInputDto.builder()
                .kind(DocumentInputDto.Kind.Invoice)
                .spec(DocumentInputDto.Spec.builder()
                        .id(null)
                        .signature(DocumentInputDto.Signature.builder()
                                .algorithm(Algorithm.RS512)
                                .build()
                        )
                        .document(JsonObject.mapFrom(invoice))
                        .build()
                )
                .build();

        // Then
        String documentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(inputDto).toString())
                .when()
                .post("/" + projectId + "/documents")
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
                    .get("/" + projectId + "/documents/" + documentId)
                    .then()
                    .statusCode(200)
                    .extract().path("status.inProgress").toString();
            return !Boolean.parseBoolean(inProgress);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/documents/" + documentId)
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

    @Test
    public void createInvoiceWithCustomSignAlgorithm_CertificateNotFound() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .description("my description")
                .sunatWebServices(sunatWebServicesDto)
                .sunatCredentials(sunatCredentialsDto)
                .build();

        String projectId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // When
        DocumentInputDto inputDto = DocumentInputDto.builder()
                .kind(DocumentInputDto.Kind.Invoice)
                .spec(DocumentInputDto.Spec.builder()
                        .id(null)
                        .signature(DocumentInputDto.Signature.builder()
                                .algorithm(Algorithm.RS512)
                                .build()
                        )
                        .document(JsonObject.mapFrom(invoice))
                        .build()
                )
                .build();

        // Then
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(inputDto).toString())
                .when()
                .post("/" + projectId + "/documents")
                .then()
                .statusCode(400);
    }

    @Test
    public void createInvoiceWithAutoIDGenerator() {
        // Given
        ProjectDto projectDto = ProjectDto.builder()
                .name("myproject")
                .description("my description")
                .sunatWebServices(sunatWebServicesDto)
                .sunatCredentials(sunatCredentialsDto)
                .build();

        String projectId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .extract().path("id").toString();

        // When
        Invoice invoice = Invoice.builder()
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
                .detalle(DocumentoDetalle.builder()
                        .descripcion("Item1")
                        .cantidad(new BigDecimal(10))
                        .precio(new BigDecimal(100))
                        .build()
                )
                .detalle(DocumentoDetalle.builder()
                        .descripcion("Item2")
                        .cantidad(new BigDecimal(10))
                        .precio(new BigDecimal(100))
                        .build()
                )
                .build();

        DocumentInputDto inputDto = DocumentInputDto.builder()
                .kind(DocumentInputDto.Kind.Invoice)
                .spec(DocumentInputDto.Spec.builder()
                        .id(DocumentInputDto.ID.builder()
                                .type(IDGeneratorType.generated)
                                .config(Map.of(
                                        "isFactura", "true",
                                        "minSerie", "3",
                                        "minNumero", "99"
                                ))
                                .build()
                        )
                        .signature(null)
                        .document(JsonObject.mapFrom(invoice))
                        .build()
                )
                .build();

        // Then
        String documentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(inputDto).toString())
                .when()
                .post("/" + projectId + "/documents")
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
                    .get("/" + projectId + "/documents/" + documentId)
                    .then()
                    .statusCode(200)
                    .extract().path("status.inProgress").toString();
            return !Boolean.parseBoolean(inProgress);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("status.inProgress", is(false),
                        "status.xmlData.ruc", is("12345678912"),
                        "status.xmlData.serieNumero", is("F003-99"),
                        "status.xmlData.tipoDocumento", is("Invoice"),
                        "status.error", is(nullValue()),
                        "status.sunat.code", is(0),
                        "status.sunat.ticket", is(nullValue()),
                        "status.sunat.status", is("ACEPTADO"),
                        "status.sunat.description", is("La Factura numero F003-99, ha sido aceptada"),
                        "status.sunat.hasCdr", is(true),
                        "status.sunat.notes", is(Collections.emptyList())
                );
    }

    @Test
    public void uploadInvalidImageFile_shouldSetErrorStatus() throws URISyntaxException {
        // Given
        String projectId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("images/java-icon.png").toURI();
        File file = new File(fileURI);

        // When
        String documentId = givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + projectId + "/upload/document")
                .then()
                .statusCode(201)
                .extract().path("id").toString();

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            String inProgress = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/" + projectId + "/documents/" + documentId)
                    .then()
                    .statusCode(200)
                    .extract().path("status.inProgress").toString();
            return !Boolean.parseBoolean(inProgress);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + projectId + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("status.inProgress", is(false),
                        "status.error.phase", is(READ_XML_FILE.toString())
                );
    }

//    @Test
//    public void uploadInvalidXMLFile_shouldSetErrorStatus() throws URISyntaxException {
//        // Given
//        String nsId = "1";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
//        File file = new File(fileURI);
//
//        // When
//        DocumentRepresentation response = given()
//                .accept(ContentType.JSON)
//                .multiPart("file", file, "application/xml")
//                .when()
//                .post("/api/namespaces/" + nsId + "/documents/upload")
//                .then()
//                .statusCode(200)
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given()
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return !watchResponse.isJobInProgress();
//        });
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(false),
//                        "jobError", is(notNullValue()),
//                        "xmlFileContent", is(nullValue())
//                );
//    }

//    @Test
//    public void uploadValidXMLFile_noCompanyRuc_shouldSetErrorStatus() throws URISyntaxException {
//        // Given
//        String nsId = "1";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_22222222222.xml").toURI();
//        File file = new File(fileURI);
//
//        // When
//        DocumentRepresentation response = given()
//                .accept(ContentType.JSON)
//                .multiPart("file", file, "application/xml")
//                .when()
//                .post("/" + nsId + "/documents/upload")
//                .then()
//                .statusCode(200)
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given()
//                    .contentType(ContentType.JSON)
//                    .when()
//
//                    .get("/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.COMPANY_NOT_FOUND);
//        });
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(false),
//                        "jobError", is(ErrorType.COMPANY_NOT_FOUND.toString()),
////                        "scheduledDelivery", is(nullValue()),
////                        "retryCount", is(0),
//                        
//                        "xmlFileContent.ruc", is("22222222222"),
//                        "xmlFileContent.serieNumero", is("F001-1"),
//                        "xmlFileContent.tipoDocumento", is("Invoice")
//                );
//    }

//    @Test
//    public void uploadValidXMLFile_existingCompanyRuc_wrongUrls_shouldHaveError() throws URISyntaxException {
//        // Given
//        String nsId = "2";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
//        File file = new File(fileURI);
//
//        // When
//        DocumentRepresentation response = given()
//                .accept(ContentType.JSON)
//                .multiPart("file", file, "application/xml")
//                .when()
//                .post("/api/namespaces/" + nsId + "/documents/upload")
//                .then()
//                .statusCode(200)
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given()
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return !watchResponse.isJobInProgress() && watchResponse.getJobError() != null && watchResponse.getJobError().getPhase().equals(JobPhaseType.SEND_XML_FILE);
//        });
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(false),
//                        "jobError", is(notNullValue()),
//                        "xmlFileContent.ruc", is("11111111111"),
//                        "xmlFileContent.serieNumero", is("F001-1"),
//                        "xmlFileContent.tipoDocumento", is("Invoice")
//                );
//    }
//
//    @Test
//    public void uploadAlteredXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
//        // Given
//        String nsId = "1";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
//        File file = new File(fileURI);
//
//        // When
//        DocumentRepresentation response = given()
//                .accept(ContentType.JSON)
//                .multiPart("file", file, "application/xml")
//                .when()
//                .post("/api/namespaces/" + nsId + "/documents/upload")
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(true))
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given()
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return !watchResponse.isJobInProgress();
//        });
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(false),
//                        "jobError", is(nullValue()),
//                        "xmlFileContent.ruc", is("12345678912"),
//                        "xmlFileContent.serieNumero", is("F001-1"),
//                        "xmlFileContent.tipoDocumento", is("Invoice"),
//                        "sunatResponse.code", is(2335),
//                        "sunatResponse.ticket", is(nullValue()),
//                        "sunatResponse.status", is("RECHAZADO"),
//                        "sunatResponse.description", is("El documento electrónico ingresado ha sido alterado"),
//                        "sunatResponse.hasCdr", is(false)
//                );
//    }
//
//    @Test
//    public void uploadValidInvoiceXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
//        // Given
//        String nsId = "1";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_12345678912.xml").toURI();
//        File file = new File(fileURI);
//
//        // When
//        DocumentRepresentation response = given()
//                .accept(ContentType.JSON)
//                .multiPart("file", file, "application/xml")
//                .when()
//                .post("/api/namespaces/" + nsId + "/documents/upload")
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(true))
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given()
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return !watchResponse.isJobInProgress();
//        });
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(false),
//                        "jobError", is(nullValue()),
//                        "xmlFileContent.ruc", is("12345678912"),
//                        "xmlFileContent.serieNumero", is("F001-1"),
//                        "xmlFileContent.tipoDocumento", is("Invoice"),
//                        "sunatResponse.code", is(0),
//                        "sunatResponse.ticket", is(nullValue()),
//                        "sunatResponse.status", is("ACEPTADO"),
//                        "sunatResponse.description", is("La Factura numero F001-1, ha sido aceptada"),
//                        "sunatResponse.hasCdr", is(true)
//                );
//    }
//
//    @Test
//    public void uploadValidVoidDocumentXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
//        // Given
//        String nsId = "1";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/voided-document_12345678912.xml").toURI();
//        File file = new File(fileURI);
//
//        // When
//        DocumentRepresentation response = given()
//                .accept(ContentType.JSON)
//                .multiPart("file", file, "application/xml")
//                .when()
//                .post("/api/namespaces/" + nsId + "/documents/upload")
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(true))
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given()
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return !watchResponse.isJobInProgress();
//        });
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(false),
//                        "jobError", is(nullValue()),
//                        "xmlFileContent.ruc", is("12345678912"),
//                        "xmlFileContent.serieNumero", is("RA-20200328-1"),
//                        "xmlFileContent.tipoDocumento", is("VoidedDocuments"),
//                        "sunatResponse.code", is(0),
//                        "sunatResponse.ticket", is(notNullValue()),
//                        "sunatResponse.status", is("ACEPTADO"),
//                        "sunatResponse.description", is("La Comunicacion de baja RA-20200328-1, ha sido aceptada"),
//                        "sunatResponse.hasCdr", is(true)
//                );
//    }
}


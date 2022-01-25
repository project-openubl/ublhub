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

import io.github.project.openubl.ublhub.models.JobPhaseType;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog1;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog19;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog6;
import io.github.project.openubl.xmlbuilderlib.models.input.common.ClienteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.common.ProveedorInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.DocumentLineInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.*;
import io.github.project.openubl.ublhub.AbstractBaseTest;
import io.github.project.openubl.ublhub.ProfileManager;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.impl.GeneratedIDGenerator;
import io.github.project.openubl.ublhub.idm.DocumentRepresentation;
import io.github.project.openubl.ublhub.idm.input.*;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class DocumentResourceTest extends AbstractBaseTest {

    final int TIMEOUT = 60;

    @Override
    public Class<?> getTestClass() {
        return DocumentResourceTest.class;
    }

    @Test
    public void getDocument() {
        // Given
        String nsId = "1";
        String documentId = "11";

        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("id", is(documentId),
                        "jobInProgress", is(false),
                        "created", is(notNullValue()),
                        "updated", is(nullValue())
                );
        // Then
    }

    @Test
    public void getDocumentThatBelongsToOtherNamespace_shouldNotBeAllowed() {
        // Given
        String nsOwnerId = "1";
        String nsToTestId = "2";

        String documentId = "11";

        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsOwnerId + "/documents/" + documentId)
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsToTestId + "/documents/" + documentId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void searchDocuments() {
        // Given
        String nsId = "1";

        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "items.size()", is(2),
                        "items[0].id", is("22"),
                        "items[1].id", is("11")
                );

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents?sort_by=created:asc")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "items.size()", is(2),
                        "items[0].id", is("11"),
                        "items[1].id", is("22")
                );
        // Then
    }

    @Test
    public void searchDocuments_filterTextByName() {
        // Given
        String nsId = "1";

        // When
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents?filterText=11")
                .then()
                .statusCode(200)
                .body("meta.count", is(1),
                        "items.size()", is(1),
                        "items[0].xmlFileContent.serieNumero", is("F-11")
                );
        // Then
    }

    @Test
    public void createInvoiceWithDefaultSignAlgorithm() {
        // Given
        String nsId = "1";

        InvoiceInputModel input = InvoiceInputModel.Builder.anInvoiceInputModel()
                .withSerie("F001")
                .withNumero(1)
                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
                        .withRuc("12345678912")
                        .withRazonSocial("Softgreen S.A.C.")
                        .build()
                )
                .withCliente(ClienteInputModel.Builder.aClienteInputModel()
                        .withNombre("Carlos Feria")
                        .withNumeroDocumentoIdentidad("12121212121")
                        .withTipoDocumentoIdentidad(Catalog6.RUC.toString())
                        .build()
                )
                .withDetalle(Arrays.asList(
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item1")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build(),
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item2")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build())
                )
                .build();

        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
                .withKind(KindRepresentation.Invoice)
                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
                                .withName(IDGeneratorType.none)
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(template).toString())
                .when()
                .post("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "jobInProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("F001-1"),
                        "xmlFileContent.tipoDocumento", is("Invoice")
                );
    }

    @Test
    public void createInvoiceWithCustomSignAlgorithm() {
        // Given
        String nsId = "1";

        InvoiceInputModel input = InvoiceInputModel.Builder.anInvoiceInputModel()
                .withSerie("F001")
                .withNumero(1)
                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
                        .withRuc("12345678912")
                        .withRazonSocial("Softgreen S.A.C.")
                        .build()
                )
                .withCliente(ClienteInputModel.Builder.aClienteInputModel()
                        .withNombre("Carlos Feria")
                        .withNumeroDocumentoIdentidad("12121212121")
                        .withTipoDocumentoIdentidad(Catalog6.RUC.toString())
                        .build()
                )
                .withDetalle(Arrays.asList(
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item1")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build(),
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item2")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build())
                )
                .build();

        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
                .withKind(KindRepresentation.Invoice)
                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
                                .withName(IDGeneratorType.none)
                                .build()
                        )
                        .withSignature(SignatureGeneratorRepresentation.Builder.aSignatureGeneratorRepresentation()
                                .withAlgorithm(Algorithm.RS512)
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(template).toString())
                .when()
                .post("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "jobInProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("F001-1"),
                        "xmlFileContent.tipoDocumento", is("Invoice")
                );
    }

    @Test
    public void createInvoiceWithAutoIDGenerator() {
        // Given
        String nsId = "1";

        InvoiceInputModel input = InvoiceInputModel.Builder.anInvoiceInputModel()
                .withSerie("F001")
                .withNumero(1)
                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
                        .withRuc("12345678912")
                        .withRazonSocial("Softgreen S.A.C.")
                        .build()
                )
                .withCliente(ClienteInputModel.Builder.aClienteInputModel()
                        .withNombre("Carlos Feria")
                        .withNumeroDocumentoIdentidad("12121212121")
                        .withTipoDocumentoIdentidad(Catalog6.RUC.toString())
                        .build()
                )
                .withDetalle(Arrays.asList(
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item1")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build(),
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item2")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build())
                )
                .build();

        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
                .withKind(KindRepresentation.Invoice)
                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
                                .withName(IDGeneratorType.generated)
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(template).toString())
                .when()
                .post("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "jobInProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("F001-1"),
                        "xmlFileContent.tipoDocumento", is("Invoice")
                );
    }

//    @Test
//    public void createInvoiceWithAutoIDGeneratorConfig() {
//        // Given
//        String nsId = "1";
//
//        InvoiceInputModel input = InvoiceInputModel.Builder.anInvoiceInputModel()
//                .withSerie("F001")
//                .withNumero(1)
//                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
//                        .withRuc("12345678912")
//                        .withRazonSocial("Softgreen S.A.C.")
//                        .build()
//                )
//                .withCliente(ClienteInputModel.Builder.aClienteInputModel()
//                        .withNombre("Carlos Feria")
//                        .withNumeroDocumentoIdentidad("12121212121")
//                        .withTipoDocumentoIdentidad(Catalog6.RUC.toString())
//                        .build()
//                )
//                .withDetalle(Arrays.asList(
//                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
//                                .withDescripcion("Item1")
//                                .withCantidad(new BigDecimal(10))
//                                .withPrecioUnitario(new BigDecimal(100))
//                                .withUnidadMedida("KGM")
//                                .build(),
//                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
//                                .withDescripcion("Item2")
//                                .withCantidad(new BigDecimal(10))
//                                .withPrecioUnitario(new BigDecimal(100))
//                                .withUnidadMedida("KGM")
//                                .build())
//                )
//                .build();
//
//        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
//                .withKind(KindRepresentation.Invoice)
//                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
//                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
//                                .withName(IDGeneratorType.generated)
//                                .withConfig(new HashMap<>() {{
//                                    put(GeneratedIDGenerator.SERIE_PROPERTY, "2");
//                                    put(GeneratedIDGenerator.NUMERO_PROPERTY, "33");
//                                }})
//                                .build()
//                        )
//                        .withDocument(JsonObject.mapFrom(input))
//                        .build()
//                )
//                .build();
//
//        // When
//        DocumentRepresentation response = given()
//                .contentType(ContentType.JSON)
//                .body(JsonObject.mapFrom(template).toString())
//                .when()
//                .post("/api/namespaces/" + nsId + "/documents")
//                .then()
//                .statusCode(201)
//                .body("id", is(notNullValue()),
//                        "jobInProgress", is(true)
//                )
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given()
//                    .contentType(ContentType.JSON)
//                    .when()
//
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
//                        "xmlFileContent.serieNumero", is("F002-33"),
//                        "xmlFileContent.tipoDocumento", is("Invoice")
//                );
//    }

    @Test
    public void createCreditNoteWithAutoIDGenerator() {
        // Given
        String nsId = "1";

        CreditNoteInputModel input = CreditNoteInputModel.Builder.aCreditNoteInputModel()
                .withSerieNumeroComprobanteAfectado("F001-1")
                .withDescripcionSustento("Descripción")
                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
                        .withRuc("12345678912")
                        .withRazonSocial("Softgreen S.A.C.")
                        .build()
                )
                .withCliente(ClienteInputModel.Builder.aClienteInputModel()
                        .withNombre("Carlos Feria")
                        .withNumeroDocumentoIdentidad("12121212121")
                        .withTipoDocumentoIdentidad(Catalog6.RUC.toString())
                        .build()
                )
                .withDetalle(Arrays.asList(
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item1")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build(),
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item2")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build())
                )
                .build();

        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
                .withKind(KindRepresentation.CreditNote)
                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
                                .withName(IDGeneratorType.generated)
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(template).toString())
                .when()
                .post("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "jobInProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("FC01-1"),
                        "xmlFileContent.tipoDocumento", is("CreditNote")
                );
    }

    @Test
    public void createDebitNoteWithAutoIDGenerator() {
        // Given
        String nsId = "1";

        DebitNoteInputModel input = DebitNoteInputModel.Builder.aDebitNoteInputModel()
                .withSerieNumeroComprobanteAfectado("F001-1")
                .withDescripcionSustento("Descripción")
                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
                        .withRuc("12345678912")
                        .withRazonSocial("Softgreen S.A.C.")
                        .build()
                )
                .withCliente(ClienteInputModel.Builder.aClienteInputModel()
                        .withNombre("Carlos Feria")
                        .withNumeroDocumentoIdentidad("12121212121")
                        .withTipoDocumentoIdentidad(Catalog6.RUC.toString())
                        .build()
                )
                .withDetalle(Arrays.asList(
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item1")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build(),
                        DocumentLineInputModel.Builder.aDocumentLineInputModel()
                                .withDescripcion("Item2")
                                .withCantidad(new BigDecimal(10))
                                .withPrecioUnitario(new BigDecimal(100))
                                .withUnidadMedida("KGM")
                                .build())
                )
                .build();

        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
                .withKind(KindRepresentation.DebitNote)
                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
                                .withName(IDGeneratorType.generated)
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(template).toString())
                .when()
                .post("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "jobInProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("FD01-1"),
                        "xmlFileContent.tipoDocumento", is("DebitNote")
                );
    }

    @Test
    public void createVoidedDocumentWithAutoIDGenerator() {
        // Given
        String nsId = "1";

        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, Calendar.DECEMBER, 1, 20, 30, 59);

        VoidedDocumentInputModel input = VoidedDocumentInputModel.Builder.aVoidedDocumentInputModel()
                .withNumero(1)
                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
                        .withRuc("12345678912")
                        .withRazonSocial("Softgreen S.A.C.")
                        .build()
                )
                .withDescripcionSustento("mi razon de baja")
                .withComprobante(VoidedDocumentLineInputModel.Builder.aVoidedDocumentLineInputModel()
                        .withSerieNumero("F001-1")
                        .withTipoComprobante(Catalog1.FACTURA.toString())
                        .withFechaEmision(calendar.getTimeInMillis())
                        .build()
                )
                .build();

        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
                .withKind(KindRepresentation.VoidedDocument)
                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
                                .withName(IDGeneratorType.generated)
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(template).toString())
                .when()
                .post("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "jobInProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("RA-20191224-1"),
                        "xmlFileContent.tipoDocumento", is("VoidedDocuments")
                );
    }

    @Test
    public void createSummaryDocumentWithAutoIDGenerator() {
        // Given
        String nsId = "1";

        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, Calendar.DECEMBER, 1, 20, 30, 59);

        SummaryDocumentInputModel input = SummaryDocumentInputModel.Builder.aSummaryDocumentInputModel()
                .withNumero(1)
                .withFechaEmisionDeComprobantesAsociados(calendar.getTimeInMillis())
                .withProveedor(ProveedorInputModel.Builder.aProveedorInputModel()
                        .withRuc("12345678912")
                        .withRazonSocial("Softgreen S.A.C.")
                        .build()
                )
                .withDetalle(Collections.singletonList(
                        SummaryDocumentLineInputModel.Builder.aSummaryDocumentLineInputModel()
                                .withTipoOperacion(Catalog19.ADICIONAR.toString())
                                .withComprobante(SummaryDocumentComprobanteInputModel.Builder.aSummaryDocumentComprobanteInputModel()
                                        .withTipo(Catalog1.BOLETA.toString())
                                        .withSerieNumero("B001-1")
                                        .withCliente(ClienteInputModel.Builder.aClienteInputModel()
                                                .withNombre("Carlos Feria")
                                                .withNumeroDocumentoIdentidad("12345678")
                                                .withTipoDocumentoIdentidad(Catalog6.DNI.toString())
                                                .build()
                                        )
                                        .withImpuestos(SummaryDocumentImpuestosInputModel.Builder.aSummaryDocumentImpuestosInputModel()
                                                .withIgv(new BigDecimal("100"))
                                                .build()
                                        )
                                        .withValorVenta(SummaryDocumentComprobanteValorVentaInputModel.Builder.aSummaryDocumentComprobanteValorVentaInputModel()
                                                .withImporteTotal(new BigDecimal("118"))
                                                .withGravado(new BigDecimal("100"))
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                ))
                .build();

        InputTemplateRepresentation template = InputTemplateRepresentation.Builder.anInputTemplateRepresentation()
                .withKind(KindRepresentation.SummaryDocument)
                .withSpec(SpecRepresentation.Builder.aSpecRepresentation()
                        .withIdGenerator(IDGeneratorRepresentation.Builder.anIDGeneratorRepresentation()
                                .withName(IDGeneratorType.generated)
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = given()
                .contentType(ContentType.JSON)
                .body(JsonObject.mapFrom(template).toString())
                .when()
                .post("/api/namespaces/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "jobInProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("RC-20191224-1"),
                        "xmlFileContent.tipoDocumento", is("SummaryDocuments")
                );
    }

//    @Test
//    public void uploadInvalidImageFile_shouldSetErrorStatus() throws URISyntaxException {
//        // Given
//        String nsId = "1";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("images/java-jar-icon-59.png").toURI();
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
//                    .get("/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.READ_FILE);
//        });
//
//        given()
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("jobInProgress", is(false),
//                        "jobError", is(ErrorType.READ_FILE.toString()),
////                        "scheduledDelivery", is(nullValue()),
////                        "retryCount", is(0),
//                        
//                        "xmlFileContent.ruc", is(nullValue()),
//                        "xmlFileContent.serieNumero", is(nullValue()),
//                        "xmlFileContent.tipoDocumento", is(nullValue())
//                );
//    }

    @Test
    public void uploadInvalidXMLFile_shouldSetErrorStatus() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given()
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/api/namespaces/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(notNullValue()),
                        "xmlFileContent", is(nullValue())
                );
    }

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

    @Test
    public void uploadValidXMLFile_existingCompanyRuc_wrongUrls_shouldHaveError() throws URISyntaxException {
        // Given
        String nsId = "2";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given()
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/api/namespaces/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress() && watchResponse.getJobError() != null && watchResponse.getJobError().getPhase().equals(JobPhaseType.SEND_XML_FILE);
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(notNullValue()),
                        "xmlFileContent.ruc", is("11111111111"),
                        "xmlFileContent.serieNumero", is("F001-1"),
                        "xmlFileContent.tipoDocumento", is("Invoice")
                );
    }

    @Test
    public void uploadAlteredXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given()
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/api/namespaces/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .body("jobInProgress", is(true))
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("F001-1"),
                        "xmlFileContent.tipoDocumento", is("Invoice"),
                        "sunatResponse.code", is(2335),
                        "sunatResponse.ticket", is(nullValue()),
                        "sunatResponse.status", is("RECHAZADO"),
                        "sunatResponse.description", is("El documento electrónico ingresado ha sido alterado"),
                        "sunatResponse.hasCdr", is(false)
                );
    }

    @Test
    public void uploadValidInvoiceXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given()
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/api/namespaces/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .body("jobInProgress", is(true))
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("F001-1"),
                        "xmlFileContent.tipoDocumento", is("Invoice"),
                        "sunatResponse.code", is(0),
                        "sunatResponse.ticket", is(nullValue()),
                        "sunatResponse.status", is("ACEPTADO"),
                        "sunatResponse.description", is("La Factura numero F001-1, ha sido aceptada"),
                        "sunatResponse.hasCdr", is(true)
                );
    }

    @Test
    public void uploadValidVoidDocumentXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/voided-document_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given()
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/api/namespaces/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .body("jobInProgress", is(true))
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isJobInProgress();
        });

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("jobInProgress", is(false),
                        "jobError", is(nullValue()),
                        "xmlFileContent.ruc", is("12345678912"),
                        "xmlFileContent.serieNumero", is("RA-20200328-1"),
                        "xmlFileContent.tipoDocumento", is("VoidedDocuments"),
                        "sunatResponse.code", is(0),
                        "sunatResponse.ticket", is(notNullValue()),
                        "sunatResponse.status", is("ACEPTADO"),
                        "sunatResponse.description", is("La Comunicacion de baja RA-20200328-1, ha sido aceptada"),
                        "sunatResponse.hasCdr", is(true)
                );
    }
}


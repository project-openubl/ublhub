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

import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog6;
import io.github.project.openubl.xmlbuilderlib.models.input.common.ClienteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.common.ProveedorInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.DocumentLineInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xsender.BaseAuthTest;
import io.github.project.openubl.xsender.ProfileManager;
import io.github.project.openubl.xsender.idgenerator.IDGeneratorType;
import io.github.project.openubl.xsender.idgenerator.generators.GeneratedIDGenerator;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.idm.input.*;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.idm.input.KindRepresentation;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestProfile(ProfileManager.class)
@TestHTTPEndpoint(DocumentResource.class)
public class DocumentResourceTest extends BaseAuthTest {

    final int TIMEOUT = 60;

    @Test
    public void getDocument() {
        // Given
        String nsId = "1";
        String documentId = "11";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("id", is("11"),
                        "createdOn", is(notNullValue()),
                        "inProgress", is(false),
                        "error", is(nullValue()),
//                        "scheduledDelivery", is(nullValue()),
//                        "retryCount", is(0),
                        "fileContentValid", is(nullValue())
                );
        // Then
    }

    @Test
    public void getDocumentByNotOwner_shouldNotBeAllowed() {
        // Given
        String nsId = "3";
        String documentId = "44";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + documentId)
                .then()
                .statusCode(404);

        givenAuth("carlos")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + documentId)
                .then()
                .statusCode(200);
        // Then
    }

    @Test
    public void getDocumentThatBelongsToOtherNamespace_shouldNotBeAllowed() {
        // Given
        String nsOwnerId = "1";
        String nsToTestId = "2";

        String documentId = "11";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsOwnerId + "/documents/" + documentId)
                .then()
                .statusCode(200);

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsToTestId + "/documents/" + documentId)
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void searchDocuments() {
        // Given
        String nsId = "1";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "data.size()", is(2),
                        "data[0].id", is("22"),
                        "data[1].id", is("11")
                );

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents?sort_by=createdOn:asc")
                .then()
                .statusCode(200)
                .body("meta.count", is(2),
                        "data.size()", is(2),
                        "data[0].id", is("11"),
                        "data[1].id", is("22")
                );
        // Then
    }

    @Test
    public void searchDocuments_filterTextByName() {
        // Given
        String nsId = "1";

        // When
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents?filterText=11")
                .then()
                .statusCode(200)
                .body("meta.count", is(1),
                        "data.size()", is(1),
                        "data[0].fileContent.documentID", is("F-11")
                );
        // Then
    }

    @Test
    public void createInvoice_byNotNsOwnerShouldNotBeAllowed() {
        // Given
        String nsId = "3";

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
        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(template)
                .when()
                .post("/" + nsId + "/documents")
                .then()
                .statusCode(404);
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
        DocumentRepresentation response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(template)
                .when()
                .post("/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "namespaceId", is("1"),
                        "inProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice")
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
        DocumentRepresentation response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(template)
                .when()
                .post("/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "namespaceId", is("1"),
                        "inProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice")
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
        DocumentRepresentation response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(template)
                .when()
                .post("/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "namespaceId", is("1"),
                        "inProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice")
                );
    }

    @Test
    public void createInvoiceWithAutoIDGeneratorConfig() {
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
                                .withConfig(new HashMap<>() {{
                                    put(GeneratedIDGenerator.SERIE_PROPERTY, "2");
                                    put(GeneratedIDGenerator.NUMERO_PROPERTY, "33");
                                }})
                                .build()
                        )
                        .withDocument(JsonObject.mapFrom(input))
                        .build()
                )
                .build();

        // When
        DocumentRepresentation response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(template)
                .when()
                .post("/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "namespaceId", is("1"),
                        "inProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("F002-33"),
                        "fileContent.documentType", is("Invoice")
                );
    }

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
        DocumentRepresentation response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(template)
                .when()
                .post("/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "namespaceId", is("1"),
                        "inProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("FC01-1"),
                        "fileContent.documentType", is("CreditNote")
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
        DocumentRepresentation response = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(template)
                .when()
                .post("/" + nsId + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "namespaceId", is("1"),
                        "inProgress", is(true)
                )
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("FD01-1"),
                        "fileContent.documentType", is("DebitNote")
                );
    }

    @Test
    public void uploadXML_byNotNsOwnerShouldNotBeAllowed() throws URISyntaxException {
        // Given
        String nsId = "3";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(404);
        // Then
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
//        DocumentRepresentation response = givenAuth("alice")
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
//            DocumentRepresentation watchResponse = givenAuth("alice")
//                    .contentType(ContentType.JSON)
//                    .when()
//                    .get("/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.READ_FILE);
//        });
//
//        givenAuth("alice")
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("inProgress", is(false),
//                        "error", is(ErrorType.READ_FILE.toString()),
////                        "scheduledDelivery", is(nullValue()),
////                        "retryCount", is(0),
//                        "fileContentValid", is(false),
//                        "fileContent.ruc", is(nullValue()),
//                        "fileContent.documentID", is(nullValue()),
//                        "fileContent.documentType", is(nullValue())
//                );
//    }

    @Test
    public void uploadInvalidXMLFile_shouldSetErrorStatus() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.UNSUPPORTED_DOCUMENT_TYPE);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(ErrorType.UNSUPPORTED_DOCUMENT_TYPE.toString()),
//                        "scheduledDelivery", is(nullValue()),
//                        "retryCount", is(0),
                        "fileContentValid", is(false),
                        "fileContent.ruc", is(nullValue()),
                        "fileContent.documentID", is(nullValue()),
                        "fileContent.documentType", is("project")
                );
    }

    @Test
    public void uploadValidXMLFile_noCompanyRuc_shouldSetErrorStatus() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_22222222222.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.COMPANY_NOT_FOUND);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(ErrorType.COMPANY_NOT_FOUND.toString()),
//                        "scheduledDelivery", is(nullValue()),
//                        "retryCount", is(0),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("22222222222"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice")
                );
    }

    @Test
    public void uploadValidXMLFile_existingCompanyRuc_wrongUrls_shouldHaveError() throws URISyntaxException {
        // Given
        String nsId = "2";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.SEND_FILE);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(ErrorType.SEND_FILE.toString()),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("11111111111"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice")
//                        "scheduledDelivery", is(notNullValue()),
//                        "retryCount", is(1),
                );
    }

    @Test
    public void uploadAlteredXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .body("inProgress", is(true))
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
//                        "scheduledDelivery", is(nullValue()),
//                        "retryCount", is(0),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice"),
                        "sunat.code", is(2335),
                        "sunat.ticket", is(nullValue()),
                        "sunat.status", is("RECHAZADO"),
                        "sunat.description", is("El documento electrónico ingresado ha sido alterado"),
                        "sunat.hasCdr", is(false)
                );
    }

    @Test
    public void uploadValidInvoiceXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .body("inProgress", is(true))
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
//                        "scheduledDelivery", is(nullValue()),
//                        "retryCount", is(0),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice"),
                        "sunat.code", is(0),
                        "sunat.ticket", is(nullValue()),
                        "sunat.status", is("ACEPTADO"),
                        "sunat.description", is("La Factura numero F001-1, ha sido aceptada"),
                        "sunat.hasCdr", is(true)
                );
    }

    @Test
    public void uploadValidVoidDocumentXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/voided-document_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = givenAuth("alice")
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .body("inProgress", is(true))
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
//                        "scheduledDelivery", is(nullValue()),
//                        "retryCount", is(0),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("12345678912"),
                        "fileContent.documentID", is("RA-20200328-1"),
                        "fileContent.documentType", is("VoidedDocuments"),
                        "sunat.code", is(0),
                        "sunat.ticket", is(notNullValue()),
                        "sunat.status", is("ACEPTADO"),
                        "sunat.description", is("La Comunicacion de baja RA-20200328-1, ha sido aceptada"),
                        "sunat.hasCdr", is(true)
                );
    }
}


/**
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
package io.github.project.openubl.xmlsender.resources;

import io.github.project.openubl.xmlsender.idm.DocumentRepresentation;
import io.github.project.openubl.xmlsender.models.DeliveryStatusType;
import io.github.project.openubl.xmlsender.resources.ApiApplication;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.awaitility.Duration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DocumentsResourceTest {

    static final long AWAIT_TIMEOUT = 10000;

    @Test
    void withNoFileShouldReturnError() {
        given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("myParamName", "myParamValue")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(400)
                .body("error", is("Form[file] is required"));
    }

    @Test
    void withEmptyFileShouldReturnError() {
        given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", "")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(400)
                .body("error", is("Form[file] is empty"));
    }

    @Test
    void withInvalidFileShouldReturnError() {
        given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", "anyContent")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(400)
                .body("error", is("Form[file] is not a valid XML file or is corrupted"));
    }

    @Test
    void invoice_withSystemCredentials_shouldReturnOK() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/invoice.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .body("customId", is("myCustomSoftwareID"))
                .body("fileInfo.ruc", is("12345678912"))
                .body("fileInfo.filename", is("12345678912-01-F001-1"))
                .body("fileInfo.documentID", is("F001-1"))
                .body("fileInfo.documentType", is("Invoice"))
                .body("fileInfo.deliveryURL", is("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService"))
                .body("sunatCredentials.username", nullValue())
                .body("sunatCredentials.password", nullValue());
    }

    @Test
    void invoice_withCustomCredentials_shouldReturnOK() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/invoice.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "myUsername")
                .formParam("password", "myPassword")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .body("customId", is("myCustomSoftwareID"))
                .body("fileInfo.ruc", is("12345678912"))
                .body("fileInfo.filename", is("12345678912-01-F001-1"))
                .body("fileInfo.documentID", is("F001-1"))
                .body("fileInfo.documentType", is("Invoice"))
                .body("fileInfo.deliveryURL", is("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService"))
                .body("sunatCredentials.username", is("myUsername"))
                .body("sunatCredentials.password", is("******"));
    }

    @Test
    void invoice_downloadFile() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/invoice.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract()
                .as(DocumentRepresentation.class);

        Response response = given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId() + "/file")
                .thenReturn();

        assertEquals(200, response.getStatusCode());

        byte[] bytes = response.getBody().asByteArray();
        assertArrayEquals(Files.readAllBytes(Paths.get(resource.getPath())), bytes);
    }

    @Test
    void validInvoice_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/invoice_signed.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
//                .body("id", is(rep.getId()))
                .body("cdrID", notNullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(0))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("ACEPTADO"))
                .body("sunatStatus.description", is("La Factura numero F001-1, ha sido aceptada"));

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId() + "/cdr")
                .then()
                .statusCode(200);
    }

    @Test
    void invalidInvoice_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/invoice.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(2335))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("RECHAZADO"))
                .body("sunatStatus.description", is("El documento electrónico ingresado ha sido alterado"));
    }

    @Test
    void validCreditNote_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/credit-note_signed.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
//                .body("id", is(rep.getId()))
                .body("cdrID", notNullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(0))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("ACEPTADO"))
                .body("sunatStatus.description", is("La Nota de Credito numero F001-1, ha sido aceptada"));

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId() + "/cdr")
                .then()
                .statusCode(200);
    }

    @Test
    void invalidCreditNote_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/credit-note.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(2335))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("RECHAZADO"))
                .body("sunatStatus.description", is("El documento electrónico ingresado ha sido alterado"));
    }

    @Test
    void validDebitNote_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/debit-note_signed.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
//                .body("id", is(rep.getId()))
                .body("cdrID", notNullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(0))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("ACEPTADO"))
                .body("sunatStatus.description", is("La Nota de Debito numero F001-1, ha sido aceptada"));

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId() + "/cdr")
                .then()
                .statusCode(200);
    }

    @Test
    void invalidDebitNote_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/debit-note.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(2335))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("RECHAZADO"))
                .body("sunatStatus.description", is("El documento electrónico ingresado ha sido alterado"));
    }

    @Test
    void validVoidedDocument_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/voided-document_signed.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
//                .body("id", is(rep.getId()))
                .body("cdrID", notNullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(0))
                .body("sunatStatus.ticket", notNullValue())
                .body("sunatStatus.status", is("ACEPTADO"))
                .body("sunatStatus.description", is("La Comunicacion de baja RA-20200328-1, ha sido aceptada"));

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId() + "/cdr")
                .then()
                .statusCode(200);
    }

    @Test
    void invalidVoidedDocument_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/voided-document.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(2335))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("RECHAZADO"))
                .body("sunatStatus.description", is("El documento electrónico ingresado ha sido alterado"));
    }

    @Test
    void validSummaryDocument_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/summary-document_signed.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
//                .body("id", is(rep.getId()))
                .body("cdrID", notNullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(0))
                .body("sunatStatus.ticket", notNullValue())
                .body("sunatStatus.status", is("ACEPTADO"))
                .body("sunatStatus.description", is("El Resumen diario RC-20200328-1, ha sido aceptado"));

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId() + "/cdr")
                .then()
                .statusCode(200);
    }

    @Test
    void invalidSummaryDocument_customCredentials_shouldBeSentToSunat() throws InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls2/summary-document.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        DocumentRepresentation rep = given()
                .when()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", file, "application/xml")
                .formParam("customId", "myCustomSoftwareID")
                .formParam("username", "12345678912MODDATOS")
                .formParam("password", "MODDATOS")
                .post(ApiApplication.API_BASE + "/documents")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
                .extract()
                .as(DocumentRepresentation.class);

        await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
                .with().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    DocumentRepresentation currentRep = given()
                            .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                            .then()
                            .extract()
                            .as(DocumentRepresentation.class);

                    return currentRep.getDeliveryStatus().equals(DeliveryStatusType.DELIVERED.toString());
                });

        given()
                .when().get(ApiApplication.API_BASE + "/documents/" + rep.getId())
                .then()
                .statusCode(200)
                .body("cdrID", nullValue())
                .body("fileID", notNullValue())
                .body("deliveryStatus", is("DELIVERED"))
                .body("sunatStatus.code", is(2513))
                .body("sunatStatus.ticket", nullValue())
                .body("sunatStatus.status", is("RECHAZADO"))
                .body("sunatStatus.description", is("Dato no cumple con formato de acuerdo al número de comprobante"));
    }
}

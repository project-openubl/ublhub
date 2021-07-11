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

import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.resources.config.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(MinioServer.class)
@QuarkusTestResource(ArtemisServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@TestHTTPEndpoint(DocumentResource.class)
public class DocumentResourceTest extends BaseKeycloakTest {

    final int TIMEOUT = 40;

    @Test
    public void getDocument() {
        // Given
        String nsId = "1";
        String documentId = "11";

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("id", is("11"),
                        "createdOn", is(notNullValue()),
                        "inProgress", is(false),
                        "error", is(nullValue()),
                        "scheduledDelivery", is(nullValue()),
                        "retryCount", is(0),
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
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + documentId)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
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
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsOwnerId + "/documents/" + documentId)
                .then()
                .statusCode(200);

        given().auth().oauth2(getAccessToken("alice"))
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
        given().auth().oauth2(getAccessToken("alice"))
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

        given().auth().oauth2(getAccessToken("alice"))
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
        given().auth().oauth2(getAccessToken("alice"))
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
    public void uploadXML_byNotNsOwnerShouldNotBeAllowed() throws URISyntaxException {
        // Given
        String nsId = "3";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(404);
        // Then
    }

    @Test
    public void uploadInvalidImageFile_shouldSetErrorStatus() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("images/java-jar-icon-59.png").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given().auth().oauth2(getAccessToken("alice"))
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.READ_FILE.getMessage());
        });

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(ErrorType.READ_FILE.getMessage()),
                        "scheduledDelivery", is(nullValue()),
                        "retryCount", is(0),
                        "fileContentValid", is(false),
                        "fileContent.ruc", is(nullValue()),
                        "fileContent.documentID", is(nullValue()),
                        "fileContent.documentType", is(nullValue())
                );
    }

    @Test
    public void uploadInvalidXMLFile_shouldSetErrorStatus() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given().auth().oauth2(getAccessToken("alice"))
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.UNSUPPORTED_DOCUMENT_TYPE.getMessage());
        });

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(ErrorType.UNSUPPORTED_DOCUMENT_TYPE.getMessage()),
                        "scheduledDelivery", is(nullValue()),
                        "retryCount", is(0),
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
        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given().auth().oauth2(getAccessToken("alice"))
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.COMPANY_NOT_FOUND.getMessage());
        });

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(ErrorType.COMPANY_NOT_FOUND.getMessage()),
                        "scheduledDelivery", is(nullValue()),
                        "retryCount", is(0),
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
        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/" + nsId + "/documents/upload")
                .then()
                .statusCode(200)
                .extract().body().as(DocumentRepresentation.class);

        // Then
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            DocumentRepresentation watchResponse = given().auth().oauth2(getAccessToken("alice"))
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return watchResponse.getError() != null && watchResponse.getError().equals(ErrorType.SEND_FILE.getMessage());
        });

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(ErrorType.SEND_FILE.getMessage()),
                        "scheduledDelivery", is(notNullValue()),
                        "retryCount", is(1),
                        "fileContentValid", is(true),
                        "fileContent.ruc", is("11111111111"),
                        "fileContent.documentID", is("F001-1"),
                        "fileContent.documentType", is("Invoice")
                );
    }

    @Test
    public void uploadAlteredXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
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
            DocumentRepresentation watchResponse = given().auth().oauth2(getAccessToken("alice"))
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "scheduledDelivery", is(nullValue()),
                        "retryCount", is(0),
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
    public void uploadValidXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
        // Given
        String nsId = "1";

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_12345678912.xml").toURI();
        File file = new File(fileURI);

        // When
        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
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
            DocumentRepresentation watchResponse = given().auth().oauth2(getAccessToken("alice"))
                    .contentType(ContentType.JSON)
                    .when()

                    .get("/" + nsId + "/documents/" + response.getId())
                    .then()
                    .statusCode(200)
                    .extract().body().as(DocumentRepresentation.class);
            return !watchResponse.isInProgress();
        });

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .when()
                .get("/" + nsId + "/documents/" + response.getId())
                .then()
                .statusCode(200)
                .body("inProgress", is(false),
                        "error", is(nullValue()),
                        "scheduledDelivery", is(nullValue()),
                        "retryCount", is(0),
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

//    @Test
//    public void uploadValidVoidedXMLFile_existingCompanyRuc_validURLs_shouldNotHaveError() throws URISyntaxException {
//        // Given
//        String nsId = "1";
//
//        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/voided-document_12345678912.xml").toURI();
//        File file = new File(fileURI);
//
//        // When
//        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
//                .accept(ContentType.JSON)
//                .multiPart("file", file, "application/xml")
//                .when()
//                .post("/" + nsId + "/documents/upload")
//                .then()
//                .statusCode(200)
//                .body("inProgress", is(true))
//                .extract().body().as(DocumentRepresentation.class);
//
//        // Then
//        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
//            DocumentRepresentation watchResponse = given().auth().oauth2(getAccessToken("alice"))
//                    .contentType(ContentType.JSON)
//                    .when()
//
//                    .get("/" + nsId + "/documents/" + response.getId())
//                    .then()
//                    .statusCode(200)
//                    .extract().body().as(DocumentRepresentation.class);
//            return !watchResponse.isInProgress();
//        });
//
//        given().auth().oauth2(getAccessToken("alice"))
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/" + nsId + "/documents/" + response.getId())
//                .then()
//                .statusCode(200)
//                .body("inProgress", is(false),
//                        "error", is(nullValue()),
//                        "scheduledDelivery", is(nullValue()),
//                        "retryCount", is(0),
//                        "fileContentValid", is(true),
//                        "fileContent.ruc", is("12345678912"),
//                        "fileContent.documentID", is("F001-1"),
//                        "fileContent.documentType", is("Invoice"),
//                        "sunat.code", is(0),
//                        "sunat.ticket", is(nullValue()),
//                        "sunat.status", is("ACEPTADO"),
//                        "sunat.description", is("La Factura numero F001-1, ha sido aceptada"),
//                        "sunat.hasCdr", is(true)
//                );
//    }
}


package org.openubl.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class DocumentsResourceTest {

    @Test
    void withNoFileShouldReturnError() {
        given()
                .when()
                    .header(new Header("content-type", "multipart/form-data"))
                    .multiPart("myParamName", "myParamValue")
                    .post("/documents/xml/send")
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
                    .post("/documents/xml/send")
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
                    .post("/documents/xml/send")
                .then()
                    .statusCode(400)
                    .body("error", is("Form[file] is not a valid XML file or is corrupted"));
    }

    @Test
    void invoice_shouldReturnOK() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("xmls/invoice.xml");
        assertNotNull(resource);
        File file = new File(resource.getPath());

        given()
                .when()
                    .header(new Header("content-type", "multipart/form-data"))
                    .multiPart("file", file, "application/xml")
                        .formParam("customId", "myCustomSoftwareID")
                    .post("/documents/xml/send")
                .then()
                    .statusCode(200)
                    .body("id", notNullValue())
                    .body("fileID", notNullValue())
                    .body("serverUrl", notNullValue())
                    .body("customId", is("myCustomSoftwareID"))
                    .body("ruc", is("12345678912"))
                    .body("documentID", is("F001-1"))
                    .body("documentType", is("INVOICE"))
                    .body("filename", is("12345678912-01-F001-1.xml"))
                    .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"));
    }
}

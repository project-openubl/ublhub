package org.openubl.resources;

import io.restassured.http.Header;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentsResourceTest {

    @Test
    void sendXML() {
        String fileName = "invoice.xml";
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("xmls/invoice.xml");
        assertNotNull(inputStream);

        given()
                .header(new Header("content-type", "multipart/form-data"))
                .multiPart("file", inputStream)
                    .formParam("username", "username")
                    .formParam("password", "password")
                .when()
                    .post("/documents/xml/send")
                .then()
                    .statusCode(200);
    }
}
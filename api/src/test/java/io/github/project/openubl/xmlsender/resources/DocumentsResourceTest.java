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
//                    .body("fileID", notNullValue())
//                    .body("serverUrl", notNullValue())
//                    .body("customId", is("myCustomSoftwareID"))
//                    .body("ruc", is("12345678912"))
//                    .body("documentID", is("F001-1"))
//                    .body("documentType", is("INVOICE"))
//                    .body("filename", is("12345678912-01-F001-1"))
//                    .body("deliveryStatus", is("SCHEDULED_TO_DELIVER"))
        ;
    }
}

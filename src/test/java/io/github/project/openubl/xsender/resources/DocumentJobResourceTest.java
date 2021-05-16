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
import io.github.project.openubl.xsender.kafka.consumers.DocumentEvents;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.*;
import io.github.project.openubl.xsender.resources.config.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = KeycloakServer.class)
@QuarkusTestResource(value = PostgreSQLServer.class)
@QuarkusTestResource(value = KafkaServer.class)
@QuarkusTestResource(value = S3Server.class)
public class DocumentJobResourceTest extends BaseKeycloakTest {

    final SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
            .withSunatUsername("12345678912MODDATOS")
            .withSunatPassword("MODDATOS")
            .build();
    final SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
            .withSunatUrlFactura("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService111")
            .withSunatUrlGuiaRemision("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService111")
            .withSunatUrlPercepcionRetencion("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService111")
            .build();

    NamespaceEntity namespace;
    CompanyEntity company;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @BeforeEach
    public void beforeEach() {
        this.documentRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.namespaceRepository.deleteAll();

        // Create namespace
        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace")
                .withOwner("alice")
                .withCreatedOn(new Date())
                .build();

        this.namespaceRepository.persist(namespace);
        this.namespace = namespace;

        // Create company
        CompanyEntity company = CompanyEntity.CompanyEntityBuilder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my company")
                .withRuc("12345678912")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .withNamespace(namespace)
                .build();
        this.companyRepository.persist(company);
        this.company = company;
    }


    @Test
    public void uploadInvalidInvoice_validRucSettings_shouldFailOnSendToSunat() throws URISyntaxException {
        // Given

        // When

        URI fileURI = DocumentJobResourceTest.class.getClassLoader().getResource("xml/invoice_12345678912.xml").toURI();
        File file = new File(fileURI);

        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/api/namespaces/" + namespace.getId() + "/documents/upload")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue()),
                        "inProgress", is(true),
                        "createdOn", is(notNullValue())
                ).extract().body().as(DocumentRepresentation.class);

        // Then
//        await().atMost(120, TimeUnit.SECONDS).until(() -> !documentRepository.findById(response.getId()).isInProgress());
        await().atMost(120, TimeUnit.SECONDS).until(() -> false);

        //

        UBLDocumentEntity document = documentRepository.findById(response.getId());
        assertNull(document.getError());
        assertFalse(document.isInProgress());

        assertTrue(document.getFileValid());
        assertNull(document.getFileValidationError());

        assertNotNull(document.getStorageFile());

        assertEquals("12345678912", document.getRuc());
        assertEquals("F001-1", document.getDocumentID());
        assertEquals("Invoice", document.getDocumentType());
        assertEquals("RECHAZADO", document.getSunatStatus());
        assertEquals(2335, document.getSunatCode());
        assertEquals("El documento electrónico ingresado ha sido alterado", document.getSunatDescription());
        assertNull(document.getSunatTicket());
    }

}

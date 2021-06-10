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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog6;
import io.github.project.openubl.xmlbuilderlib.models.input.common.ClienteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.common.ProveedorInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.DocumentLineInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.idm.InputDocumentRepresentation;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.*;
import io.github.project.openubl.xsender.resources.config.BaseKeycloakTest;
import io.github.project.openubl.xsender.resources.config.ServerDependencies;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@ServerDependencies
public class DocumentResourceTest extends BaseKeycloakTest {

    final int TIMEOUT = 40;

    final SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
            .withSunatUsername("12345678912MODDATOS")
            .withSunatPassword("MODDATOS")
            .build();
    final SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
            .withSunatUrlFactura("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
            .withSunatUrlGuiaRemision("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
            .withSunatUrlPercepcionRetencion("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService")
            .build();

    NamespaceEntity namespace;
    CompanyEntity company;

    @Inject
    ObjectMapper objectMapper;

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
    public void uploadInvalidXML_shouldFail() throws URISyntaxException {
        // Given

        // When

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
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
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> !documentRepository.findById(response.getId()).isInProgress());

        //
        UBLDocumentEntity document = documentRepository.findById(response.getId());

        assertFalse(document.getFileValid());
        assertEquals(ErrorType.INVALID_FILE, document.getError());

        assertNotNull(document.getStorageFile());
    }

    @Test
    public void uploadXMLAndRucSettingsNotAvailable_shouldFail() throws URISyntaxException {
        // Given

        // When

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_11111111111.xml").toURI();
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
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> !documentRepository.findById(response.getId()).isInProgress());

        //
        UBLDocumentEntity document = documentRepository.findById(response.getId());

        assertNotNull(document.getStorageFile());

        assertFalse(document.isInProgress());
        assertTrue(document.getFileValid());
        assertEquals("11111111111", document.getRuc());
        assertEquals("F001-1", document.getDocumentID());
        assertEquals("Invoice", document.getDocumentType());
        assertEquals(ErrorType.NS_COMPANY_NOT_FOUND, document.getError());
    }

    @Test
    public void uploadInvalidInvoice_validRucSettings_shouldFailOnSendToSunat() throws URISyntaxException {
        // Given

        // When

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/invoice_12345678912.xml").toURI();
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
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> !documentRepository.findById(response.getId()).isInProgress());

        //

        UBLDocumentEntity document = documentRepository.findById(response.getId());
        assertNull(document.getError());
        assertFalse(document.isInProgress());

        assertTrue(document.getFileValid());

        assertNotNull(document.getStorageFile());

        assertEquals("12345678912", document.getRuc());
        assertEquals("F001-1", document.getDocumentID());
        assertEquals("Invoice", document.getDocumentType());
        assertEquals("RECHAZADO", document.getSunatStatus());
        assertEquals(2335, document.getSunatCode());
        assertEquals("El documento electrónico ingresado ha sido alterado", document.getSunatDescription());
        assertNull(document.getSunatTicket());
    }

    @Test
    public void uploadVoidedDocument_validRucSettings_shouldVerifyTicket() throws URISyntaxException {
        // Given

        // When

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xml/voided-document_12345678912.xml").toURI();
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
        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> !documentRepository.findById(response.getId()).isInProgress());

        //

        UBLDocumentEntity document = documentRepository.findById(response.getId());
        assertNull(document.getError());
        assertFalse(document.isInProgress());

        assertTrue(document.getFileValid());

        assertNotNull(document.getStorageFile());
        assertNotNull(document.getStorageCdr());

        assertEquals("12345678912", document.getRuc());
        assertEquals("RA-20200328-1", document.getDocumentID());
        assertEquals("VoidedDocuments", document.getDocumentType());
        assertEquals("ACEPTADO", document.getSunatStatus());
        assertEquals(0, document.getSunatCode());
        assertEquals("La Comunicacion de baja RA-20200328-1, ha sido aceptada", document.getSunatDescription());
        assertNotNull(document.getSunatTicket());
    }

    @Test
    public void createXMLFromSpec() throws URISyntaxException {
        // Given
        InvoiceInputModel invoiceInput = InvoiceInputModel.Builder.anInvoiceInputModel()
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

        InputDocumentRepresentation payload = new InputDocumentRepresentation();
        payload.setKind(DocumentType.INVOICE.toString());
        payload.setSpec(objectMapper.valueToTree(invoiceInput));

        // When

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/namespaces/" + namespace.getId() + "/documents")
                .then()
                .statusCode(200);

        // Then
    }

}

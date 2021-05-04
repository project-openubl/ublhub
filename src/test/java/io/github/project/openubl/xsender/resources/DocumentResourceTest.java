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

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.kafka.producers.UBLDocumentCreatedEventProducer;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@QuarkusTestResource(KafkaServer.class)
@QuarkusTestResource(S3Server.class)
public class DocumentResourceTest extends BaseKeycloakTest {

    final SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
            .withSunatUsername("anyUsername")
            .withSunatPassword("anyPassword")
            .build();
    final SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
            .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
            .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
            .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
            .build();

    NamespaceEntity namespace;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @BeforeEach
    public void beforeEach() {
        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace")
                .withOwner("alice")
                .withCreatedOn(new Date())
                .build();

        this.documentRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.namespaceRepository.deleteAll();

        this.namespaceRepository.persist(namespace);
        this.namespace = namespace;
    }

    @Test
    public void uploadXML() throws URISyntaxException {
        // Given

        // When

        URI fileURI = DocumentResourceTest.class.getClassLoader().getResource("xmls/invoice_signed.xml").toURI();
        File file = new File(fileURI);

        DocumentRepresentation response = given().auth().oauth2(getAccessToken("alice"))
                .accept(ContentType.JSON)
                .multiPart("file", file, "application/xml")
                .when()
                .post("/api/namespaces/" + namespace.getName() + "/documents/upload")
                .then()
                .statusCode(200)
                .body("id", is(notNullValue())
                ).extract().body().as(DocumentRepresentation.class);

        // Then
        OutboxEventEntity kafkaMsg = OutboxEventEntity.findByParams(EntityType.sunat_document.toString(), response.getId(), EventType.SCHEDULED.toString());
        assertNotNull(kafkaMsg);
    }


}

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
package io.github.project.openubl.ublhub.ubl.sender;

import io.github.project.openubl.ublhub.ProductionTestProfile;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ConnectToSUNATException;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ReadXMLFileContentException;
import io.github.project.openubl.xsender.files.xml.DocumentType;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import io.github.project.openubl.xsender.models.Status;
import io.github.project.openubl.xsender.models.SunatResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(ProductionTestProfile.class)
public class XMLSenderManagerTest {

    @Inject
    XMLSenderManager xmlSenderManager;

    @Test
    public void getXMLContent_nullFile() {
        // Given
        byte[] file = null;

        // When
        Assertions.assertThrows(ReadXMLFileContentException.class, () -> {
            xmlSenderManager.getXMLContent(file);
        });
    }

    @Test
    public void getXMLContent_invalidXMLFile() throws URISyntaxException, IOException {
        // Given
        URI uri = XMLSenderManagerTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        Assertions.assertThrows(ReadXMLFileContentException.class, () -> {
            xmlSenderManager.getXMLContent(file);
        });
    }

    @Test
    public void getXMLContent_validXMLFile() throws URISyntaxException, IOException, ReadXMLFileContentException {
        // Given
        URI uri = XMLSenderManagerTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        XmlContent result = xmlSenderManager.getXMLContent(file);

        // Then
        assertEquals("Invoice", result.getDocumentType());
        assertEquals("F001-1", result.getDocumentID());
        assertEquals("11111111111", result.getRuc());
        Assertions.assertNull(result.getVoidedLineDocumentTypeCode());
    }

    @Test
    public void sendToSUNAT_invalidUrls() throws URISyntaxException, IOException {
        // Given
        URI uri = XMLSenderManagerTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        XMLSenderConfig wsConfig = XMLSenderConfig.builder()
                .facturaUrl("http://url1.com")
                .guiaRemisionUrl("http://url1.com")
                .percepcionRetencionUrl("http://url1.com")
                .username("MODDATOS11111111111")
                .password("MODDATOS")
                .build();

        // When
        Assertions.assertThrows(ConnectToSUNATException.class, () -> {
            xmlSenderManager.sendToSUNAT(file, wsConfig);
        });
    }

    @Test
    public void sendToSUNAT_validUrls() throws URISyntaxException, IOException, ConnectToSUNATException {
        // Given
        URI uri = XMLSenderManagerTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        XMLSenderConfig wsConfig = XMLSenderConfig.builder()
                .facturaUrl("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .guiaRemisionUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
                .percepcionRetencionUrl("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem")
                .username("MODDATOS11111111111")
                .password("MODDATOS")
                .build();

        // When
        SunatResponse result = xmlSenderManager.sendToSUNAT(file, wsConfig);

        // Then
        assertEquals(Status.RECHAZADO, result.getStatus());
        assertNotNull(result.getMetadata().getResponseCode());
        assertNotNull(result.getMetadata().getDescription());
        assertNull(result.getSunat());
    }

    // TODO enable this test as soon as SUNAT fixes the endpoint
    @Disabled
    @Test
    public void verifyTicketAtSUNAT() throws ConnectToSUNATException {
        // Given
        String ticket = "123456789";

        XMLSenderConfig wsConfig = XMLSenderConfig.builder()
                .facturaUrl("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .guiaRemisionUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
                .percepcionRetencionUrl("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem")
                .username("MODDATOS11111111111")
                .password("MODDATOS")
                .build();

        XmlContent xmlContentModel = XmlContent.builder()
                .ruc("12345678912")
                .documentID("RA-20200328-1")
                .documentType(DocumentType.VOIDED_DOCUMENT)
                .voidedLineDocumentTypeCode("01")
                .build();

        // When
        SunatResponse result = xmlSenderManager.verifyTicketAtSUNAT(ticket, xmlContentModel, wsConfig);

        // Then
        assertEquals(Status.EXCEPCION, result.getStatus());
        assertNotNull(result.getMetadata().getResponseCode());
        assertNotNull(result.getMetadata().getDescription());
        assertNotNull(result.getSunat().getCdr());
    }
}

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

import io.github.project.openubl.ublhub.BasicProfileManager;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ConnectToSUNATException;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ReadXMLFileContentException;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@TestProfile(BasicProfileManager.class)
public class XMLSenderManagerTest {

    @Inject
    XMLSenderManager xmlSenderManager;

    @Test
    public void getXMLContent_nullFile() {
        // Given
        byte[] file = null;

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xmlSenderManager
                .getXMLContent(file)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(ReadXMLFileContentException.class);
    }

    @Test
    public void getXMLContent_invalidXMLFile() throws URISyntaxException, IOException {
        // Given
        URI uri = XMLSenderManagerTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xmlSenderManager
                .getXMLContent(file)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(ReadXMLFileContentException.class);
    }

    @Test
    public void getXMLContent_validXMLFile() throws URISyntaxException, IOException {
        // Given
        URI uri = XMLSenderManagerTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xmlSenderManager
                .getXMLContent(file)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // Then
        XmlContentModel result = subscriber.assertCompleted().getItem();
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

        XMLSenderConfig wsConfig = XMLSenderConfigBuilder.aXMLSenderConfig()
                .withFacturaUrl("http://url1.com")
                .withGuiaRemisionUrl("http://url1.com")
                .withPercepcionRetencionUrl("http://url1.com")
                .withUsername("MODDATOS11111111111")
                .withPassword("MODDATOS")
                .build();

        // When
        UniAssertSubscriber<BillServiceModel> subscriber = xmlSenderManager.sendToSUNAT(file, wsConfig)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(ConnectToSUNATException.class);
    }

    @Test
    public void sendToSUNAT_validUrls() throws URISyntaxException, IOException {
        // Given
        URI uri = XMLSenderManagerTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        XMLSenderConfig wsConfig = XMLSenderConfigBuilder.aXMLSenderConfig()
                .withFacturaUrl("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .withGuiaRemisionUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
                .withPercepcionRetencionUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService")
                .withUsername("MODDATOS11111111111")
                .withPassword("MODDATOS")
                .build();

        // When
        UniAssertSubscriber<BillServiceModel> subscriber = xmlSenderManager.sendToSUNAT(file, wsConfig)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // Then
        BillServiceModel result = subscriber.assertCompleted().getItem();
        assertEquals(BillServiceModel.Status.RECHAZADO, result.getStatus());
        assertNotNull(result.getCode());
        assertNotNull(result.getDescription());
        assertNull(result.getCdr());
        assertNull(result.getTicket());
    }

    @Test
    public void verifyTicketAtSUNAT() {
        // Given
        String ticket = "123456789";

        XMLSenderConfig wsConfig = XMLSenderConfigBuilder.aXMLSenderConfig()
                .withFacturaUrl("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .withGuiaRemisionUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
                .withPercepcionRetencionUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService")
                .withUsername("MODDATOS11111111111")
                .withPassword("MODDATOS")
                .build();

        XmlContentModel xmlContentModel = XmlContentModel.Builder.aXmlContentModel()
                .withRuc("11111111111")
                .withDocumentID("F001-1")
                .withDocumentType("Invoice")
                .withVoidedLineDocumentTypeCode("01")
                .build();

        // When
        UniAssertSubscriber<BillServiceModel> subscriber = xmlSenderManager.verifyTicketAtSUNAT(ticket, xmlContentModel, wsConfig)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // Then
        BillServiceModel result = subscriber.assertCompleted().getItem();
        assertEquals(BillServiceModel.Status.EXCEPCION, result.getStatus());
        assertNotNull(result.getCode());
        assertNotNull(result.getDescription());
        assertNotNull(result.getCdr());
        assertNotNull(result.getTicket());
    }
}

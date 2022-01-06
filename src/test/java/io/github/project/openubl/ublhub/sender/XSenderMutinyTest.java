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
package io.github.project.openubl.ublhub.sender;

import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.ublhub.ProfileManager;
import io.github.project.openubl.ublhub.exceptions.ReadFileException;
import io.github.project.openubl.ublhub.exceptions.SendFileToSUNATException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class XSenderMutinyTest {

    @Inject
    XSenderMutiny xSenderMutiny;

    @Test
    public void getFileContentOf_nullFile() {
        // Given
        byte[] file = null;

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xSenderMutiny.getFileContent(file).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(ReadFileException.class, null);
    }

    @Test
    public void getFileContentOf_invalidXMLFile() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xSenderMutiny.getFileContent(file).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(ReadFileException.class, null);
    }

    @Test
    public void getFileContentOf_validXMLFile() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xSenderMutiny.getFileContent(file).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();
    }

//    @Test
//    public void getXSenderRequiredData_namespaceNotFound() {
//        // Given
//        String namespaceId = "9";
//        String ruc = "1111111111";
//
//        // When
//        UniAssertSubscriber<XSenderRequiredData> subscriber = xSenderMutiny.getXSenderRequiredData(namespaceId, ruc).subscribe().withSubscriber(UniAssertSubscriber.create());
//
//        // Then
//        subscriber.assertFailedWith(NoCompanyWithRucException.class, "No company with ruc found");
//    }

    @Test
    public void sendFile_invalidUrls() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/invoice_alterado_11111111111.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        XSenderConfig wsConfig = XSenderConfigBuilder.aXSenderConfig()
                .withFacturaUrl( "http://url1.com")
                .withGuiaUrl("http://url1.com")
                .withPercepcionRetencionUrl("http://url1.com")
                .withUsername("MODDATOS11111111111")
                .withPassword("MODDATOS")
                .build();

        // When
        UniAssertSubscriber<BillServiceModel> subscriber = xSenderMutiny.sendFile(file, wsConfig).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(SendFileToSUNATException.class, "Could not send file");
    }

    @Test
    public void sendFile_validUrls() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        XSenderConfig wsConfig = XSenderConfigBuilder.aXSenderConfig()
                .withFacturaUrl( "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .withGuiaUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
                .withPercepcionRetencionUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService")
                .withUsername("MODDATOS11111111111")
                .withPassword("MODDATOS")
                .build();

        // When
        UniAssertSubscriber<BillServiceModel> subscriber = xSenderMutiny.sendFile(file, wsConfig).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();
    }

    @Test
    public void verifyTicket() {
    }
}

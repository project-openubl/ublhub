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
package io.github.project.openubl.ublhub.ubl;

import io.github.project.openubl.xmlbuilderlib.utils.CertificateDetails;
import io.github.project.openubl.xmlbuilderlib.utils.CertificateDetailsFactory;
import io.github.project.openubl.xmlbuilderlib.xml.XMLSigner;
import io.github.project.openubl.xmlbuilderlib.xml.XmlSignatureHelper;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.SmartBillServiceConfig;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.SmartBillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.SmartBillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class XMLAssertUtils {

    private static final String SUNAT_BETA_USERNAME = "MODDATOS";
    private static final String SUNAT_BETA_PASSWORD = "MODDATOS";

    private static final String SIGN_REFERENCE_ID = "PROJECT-OPENUBL";
    private static final String KEYSTORE = "LLAMA-PE-CERTIFICADO-DEMO-10467793549.pfx";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final CertificateDetails CERTIFICATE;

    static {
        InputStream ksInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(KEYSTORE);
        try {
            CERTIFICATE = CertificateDetailsFactory.create(ksInputStream, KEYSTORE_PASSWORD);
            SmartBillServiceConfig.getInstance()
                    .withInvoiceAndNoteDeliveryURL("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                    .withPerceptionAndRetentionDeliveryURL("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
                    .withDespatchAdviceDeliveryURL("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void assertSnapshot(String expected, String snapshotFile) {
        InputStream snapshotInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(snapshotFile);
        assertNotNull(snapshotInputStream, "Could not find snapshot file " + snapshotFile);

        Diff myDiff = DiffBuilder
                .compare(snapshotInputStream)
                .withTest(expected)
                .ignoreComments()
                .ignoreWhitespace()
                .build();

        assertFalse(myDiff.hasDifferences(), expected + "\n" + myDiff.toString());
    }

    public static void assertSendSunat(String xmlWithoutSignature, String... allowedNotes) throws Exception {
        String skipSunat = System.getProperty("skipSunat", "false");
        if (skipSunat != null && skipSunat.equals("false")) {
            Document signedXML = XMLSigner.signXML(xmlWithoutSignature, SIGN_REFERENCE_ID, CERTIFICATE.getX509Certificate(), CERTIFICATE.getPrivateKey());
            sendFileToSunat(signedXML, xmlWithoutSignature, allowedNotes);
        }
    }

    //

    private static void sendFileToSunat(Document document, String xmlWithoutSignature, String... allowedNotes) throws Exception {
        SmartBillServiceModel smartBillServiceModel = SmartBillServiceManager.send(XmlSignatureHelper.getBytesFromDocument(document), SUNAT_BETA_USERNAME, SUNAT_BETA_PASSWORD);
        XmlContentModel xmlContentModel = smartBillServiceModel.getXmlContentModel();
        BillServiceModel billServiceModel = smartBillServiceModel.getBillServiceModel();

        if (billServiceModel.getNotes() != null) {
            List<String> allowedNotesList = Arrays.asList(allowedNotes);

            List<String> notesToCheck = billServiceModel.getNotes().stream().filter(f -> allowedNotesList.stream().noneMatch(f::startsWith)).collect(Collectors.toList());
            notesToCheck.forEach(f -> System.out.println("WARNING:" + f));

            assertTrue(notesToCheck.isEmpty(), "Notes fom SUNAT:\n" + String.join("\n", notesToCheck));
        }

        // Check ticket
        if (!xmlContentModel.getDocumentType().equals(DocumentType.VOIDED_DOCUMENT.getType()) && !xmlContentModel.getDocumentType().equals(DocumentType.SUMMARY_DOCUMENT.getType())) {
            assertEquals(
                    BillServiceModel.Status.ACEPTADO,
                    billServiceModel.getStatus(),
                    xmlWithoutSignature + " \n sunat [codigo=" + billServiceModel.getCode() + "], [descripcion=" + billServiceModel.getDescription() + "]"
            );
        } else {
            assertNotNull(billServiceModel.getTicket());

            BillServiceModel statusModel = SmartBillServiceManager.getStatus(billServiceModel.getTicket(), xmlContentModel, SUNAT_BETA_USERNAME, SUNAT_BETA_PASSWORD);
            assertEquals(
                    BillServiceModel.Status.ACEPTADO,
                    statusModel.getStatus(),
                    xmlWithoutSignature + " sunat [status=" + statusModel.getStatus() + "], [descripcion=" + statusModel.getDescription() + "]"
            );
            assertNotNull(
                    statusModel.getCdr(),
                    xmlWithoutSignature + " sunat [codigo=" + billServiceModel.getCode() + "], [descripcion=" + billServiceModel.getDescription() + "]"
            );
        }
    }
}

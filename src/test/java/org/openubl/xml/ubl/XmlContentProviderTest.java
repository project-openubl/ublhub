package org.openubl.xml.ubl;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.openubl.models.DocumentType;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class XmlContentProviderTest {

    @Inject
    XmlContentProvider xmlContentProvider;

    @Test
    void getSunatDocument_invoice() throws IOException, SAXException, ParserConfigurationException {
        // Given
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("xmls/invoice.xml");
        assertNotNull(inputStream);

        // When
        XmlContentModel model = xmlContentProvider.getSunatDocument(inputStream);

        // Then
        assertNotNull(model);
        assertEquals(DocumentType.INVOICE.getDocumentType(), model.getDocumentType());
        assertEquals("F001-1", model.getDocumentID());
        assertEquals("12345678912", model.getRuc());
    }

    @Test
    void getSunatDocument_creditNote() throws IOException, SAXException, ParserConfigurationException {
        // Given
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("xmls/credit-note.xml");
        assertNotNull(inputStream);

        // When
        XmlContentModel model = xmlContentProvider.getSunatDocument(inputStream);

        // Then
        assertNotNull(model);
        assertEquals(DocumentType.CREDIT_NOTE.getDocumentType(), model.getDocumentType());
        assertEquals("BC01-1", model.getDocumentID());
        assertEquals("12345678912", model.getRuc());
    }

    @Test
    void getSunatDocument_debitNote() throws IOException, SAXException, ParserConfigurationException {
        // Given
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("xmls/debit-note.xml");
        assertNotNull(inputStream);

        // When
        XmlContentModel model = xmlContentProvider.getSunatDocument(inputStream);

        // Then
        assertNotNull(model);
        assertEquals(DocumentType.DEBIT_NOTE.getDocumentType(), model.getDocumentType());
        assertEquals("BD01-1", model.getDocumentID());
        assertEquals("12345678912", model.getRuc());
    }

    @Test
    void getSunatDocument_voidedDocument() throws IOException, SAXException, ParserConfigurationException {
        // Given
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("xmls/voided-document.xml");
        assertNotNull(inputStream);

        // When
        XmlContentModel model = xmlContentProvider.getSunatDocument(inputStream);

        // Then
        assertNotNull(model);
        assertEquals(DocumentType.VOIDED_DOCUMENT.getDocumentType(), model.getDocumentType());
        assertEquals("RA-20191224-1", model.getDocumentID());
        assertEquals("12345678912", model.getRuc());
    }

    @Test
    void getSunatDocument_summaryDocument() throws IOException, SAXException, ParserConfigurationException {
        // Given
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("xmls/summary-document.xml");
        assertNotNull(inputStream);

        // When
        XmlContentModel model = xmlContentProvider.getSunatDocument(inputStream);

        // Then
        assertNotNull(model);
        assertEquals(DocumentType.SUMMARY_DOCUMENT.getDocumentType(), model.getDocumentType());
        assertEquals("RC-20191224-1", model.getDocumentID());
        assertEquals("12345678912", model.getRuc());
    }
}

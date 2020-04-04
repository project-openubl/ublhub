package org.openubl.providers;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SunatMessageConsumer {

    public void sendSunat() {
        //        ServiceConfig config = new ServiceConfig.Builder()
//                .url(fileInfo.getUrl())
//                .username(username)
//                .password(password)
//                .build();
//
//        Document xmlDocument = inputStreamToDocument(is);
//
//        BillServiceModel billServiceModel;
//        switch (fileInfo.getDocumentType()) {
//            case INVOICE:
//            case CREDIT_NOTE:
//            case DEBIT_NOTE:
//                billServiceModel = BillServiceManager.sendBill(
//                        fileInfo.getFileName(), documentToBytes(xmlDocument), config
//                );
//                break;
//            case VOIDED_DOCUMENT:
//            case SUMMARY_DOCUMENT:
//                billServiceModel = BillServiceManager.sendSummary(
//                        fileInfo.getFileName(), documentToBytes(xmlDocument), config
//                );
//                break;
//            default:
//                throw new IllegalStateException("Could not determine where to send file");
//        }
    }
    private byte[] documentToBytes(Document document) throws TransformerException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        transformer.transform(new DOMSource(document), result);
        return bos.toByteArray();
    }

    private Document inputStreamToDocument(InputStream in) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(in));
    }
}

package org.openubl.xml;

import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class SunatDocumentProvider {

    public SunatDocumentModel getSunatDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        SunatDocumentHandler handler = new SunatDocumentHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        parser.parse(is, handler);
        return handler.getModel();
    }
}

package org.openubl.providers;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openubl.jms.DefaultJMSProducer;
import org.openubl.models.SendFileModel;
import org.openubl.models.DocumentType;
import org.openubl.xml.SunatDocumentModel;
import org.openubl.xml.SunatDocumentProvider;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

@ApplicationScoped
public class SendFileProvider {

    public static final Pattern FACTURA_SERIE_REGEX = Pattern.compile("^[F|f].*$");
    public static final Pattern BOLETA_SERIE_REGEX = Pattern.compile("^[B|b].*$");

    @ConfigProperty(name = "openubl.sunatUrl1")
    String sunatUrl1;

    @Inject
    DefaultJMSProducer sunatJMSProducer;

    @Inject
    SunatDocumentProvider sunatDocumentProvider;

    public void sendFile(byte[] file, String username, String password) throws IOException, SAXException, ParserConfigurationException {
        SunatDocumentModel sunatDocument = sunatDocumentProvider.getSunatDocument(new ByteArrayInputStream(file));

        String serverUrl = getServerUrl(sunatDocument.getDocumentType());
        String fileName = getFileName(sunatDocument.getDocumentType(), sunatDocument.getRuc(), sunatDocument.getDocumentID());

        SendFileModel messageModel = SendFileModel.Builder.aSunatJMSMessageModel()
                .withServerUrl(serverUrl)
                .withDocumentType(sunatDocument.getDocumentType().getDocumentType())
                .withFileName(fileName)
                .withUsername(username)
                .withPassword(password)
                .build();

        sunatJMSProducer.produceSendFileMessage(messageModel, file);
    }

    private String getServerUrl(DocumentType documentType) {
        return sunatUrl1;
    }

    private String getFileName(DocumentType type, String ruc, String documentID) {
        String codigoDocumento;
        switch (type) {
            case INVOICE:
                if (FACTURA_SERIE_REGEX.matcher(documentID).find()) {
                    codigoDocumento = "01";
                } else if (BOLETA_SERIE_REGEX.matcher(documentID).find()) {
                    codigoDocumento = "03";
                } else {
                    throw new IllegalStateException("Invalid Serie, can not detect code");
                }

                return MessageFormat.format("{0}-{1}-{2}.xml", ruc, codigoDocumento, documentID);
            case CREDIT_NOTE:
                codigoDocumento = "07";
                return MessageFormat.format("{0}-{1}-{2}.xml", ruc, codigoDocumento, documentID);
            case DEBIT_NOTE:
                codigoDocumento = "08";
                return MessageFormat.format("{0}-{1}-{2}.xml", ruc, codigoDocumento, documentID);
            case VOIDED_DOCUMENT:
            case SUMMARY_DOCUMENT:
                return MessageFormat.format("{0}-{1}.xml", ruc, documentID);
            default:
                throw new IllegalStateException("Invalid type of UBL Document, can not extract Serie Numero to create fileName");
        }
    }

}

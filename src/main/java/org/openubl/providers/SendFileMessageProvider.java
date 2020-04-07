package org.openubl.providers;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openubl.exceptions.InvalidXMLFileException;
import org.openubl.jms.SendFileJMSProducer;
import org.openubl.models.FileDeliveryStatusType;
import org.openubl.models.SendFileMessageModel;
import org.openubl.models.DocumentType;
import org.openubl.models.jpa.FileDeliveryRepository;
import org.openubl.models.jpa.entities.FileDeliveryEntity;
import org.openubl.xml.SunatDocumentModel;
import org.openubl.xml.SunatDocumentProvider;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

@ApplicationScoped
public class SendFileMessageProvider {

    public static final Pattern FACTURA_SERIE_REGEX = Pattern.compile("^[F|f].*$");
    public static final Pattern BOLETA_SERIE_REGEX = Pattern.compile("^[B|b].*$");

    @ConfigProperty(name = "openubl.sunatUrl1")
    String sunatUrl1;

    @Inject
    SendFileJMSProducer sendFileJMSProducer;

    @Inject
    SunatDocumentProvider sunatDocumentProvider;

    @Inject
    FileDeliveryRepository fileDeliveryRepository;

    /**
     * @param file     file to be sent to SUNAT
     * @param username username in SUNAT
     * @param password password in SUNAT
     * @return true if file was scheduled to be send
     */
    @Transactional
    public FileDeliveryEntity sendFile(byte[] file, String username, String password, String customId) throws InvalidXMLFileException, JMSException {
        SunatDocumentModel sunatDocument;
        try {
            sunatDocument = sunatDocumentProvider.getSunatDocument(new ByteArrayInputStream(file));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new InvalidXMLFileException(e);
        }

        String serverUrl = getServerUrl(sunatDocument.getDocumentType());
        String fileName = getFileName(sunatDocument.getDocumentType(), sunatDocument.getRuc(), sunatDocument.getDocumentID());

        SendFileMessageModel messageModel = SendFileMessageModel.Builder.aSendFileMessageModel()
                .withServerUrl(serverUrl)
                .withDocumentType(sunatDocument.getDocumentType().getDocumentType())
                .withFileName(fileName)
                .withUsername(username)
                .withPassword(password)
                .withCustomId(customId)
                .build();

        sendFileJMSProducer.produceSendFileMessage(messageModel, file);

        FileDeliveryEntity fileDeliveryEntity = FileDeliveryEntity.Builder.aFileDeliveryEntity()
                .withStatus(FileDeliveryStatusType.SCHEDULED_TO_DELIVER)
                .build();
        fileDeliveryRepository.persist(fileDeliveryEntity);

        return fileDeliveryEntity;
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

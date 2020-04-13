package org.openubl.providers;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.exceptions.InvalidXMLFileException;
import org.openubl.exceptions.UnsupportedDocumentTypeException;
import org.openubl.jms.SendFileJMSProducer;
import org.openubl.models.FileDeliveryStatusType;
import org.openubl.models.MessageModel;
import org.openubl.models.DocumentType;
import org.openubl.models.jpa.FileDeliveryRepository;
import org.openubl.models.jpa.entities.FileDeliveryEntity;
import org.openubl.xml.ubl.XmlContentModel;
import org.openubl.xml.ubl.XmlContentProvider;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class DocumentsManager {

    private static final Logger LOG = Logger.getLogger(DocumentsManager.class);

    public static final Pattern FACTURA_SERIE_REGEX = Pattern.compile("^[F|f].*$");
    public static final Pattern BOLETA_SERIE_REGEX = Pattern.compile("^[B|b].*$");

    @ConfigProperty(name = "openubl.sunatUrl1")
    String sunatUrl1;

    @Inject
    SendFileJMSProducer sendFileJMSProducer;

    @Inject
    XmlContentProvider xmlContentProvider;

    @Inject
    FileDeliveryRepository fileDeliveryRepository;

    /**
     * @param file     file to be sent to SUNAT
     * @param username username in SUNAT
     * @param password password in SUNAT
     * @return true if file was scheduled to be send
     */
    @Transactional
    public FileDeliveryEntity sendFile(byte[] file, String username, String password, String customId) throws InvalidXMLFileException, UnsupportedDocumentTypeException {
        // Read file
        XmlContentModel xmlContentModel;
        try {
            xmlContentModel = xmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new InvalidXMLFileException(e);
        }

        //
        Optional<DocumentType> documentTypeOptional = DocumentType.valueFromDocumentType(xmlContentModel.getDocumentType());
        if (!documentTypeOptional.isPresent()) {
            throw new UnsupportedDocumentTypeException(xmlContentModel.getDocumentType() + " is not supported yet");
        }

        DocumentType documentType = documentTypeOptional.get();
        String serverUrl = getServerUrl(documentType);
        String fileName = getFileName(documentType, xmlContentModel.getRuc(), xmlContentModel.getDocumentID());

        // Create Entity in DB
        FileDeliveryEntity deliveryEntity = FileDeliveryEntity.Builder.aFileDeliveryEntity()
                .withRuc(xmlContentModel.getRuc())
                .withDocumentID(xmlContentModel.getDocumentID())
                .withDocumentType(documentType)
                .withFilename(fileName)
                .withDeliveryStatus(FileDeliveryStatusType.SCHEDULED_TO_DELIVER)
                .withServerUrl(serverUrl)
                .withCustomId(customId)
                .build();

        fileDeliveryRepository.persist(deliveryEntity);

        // Create JSM Message Object
        MessageModel messageModel = MessageModel.Builder.aSendFileMessageModel()
                .withEntityId(deliveryEntity.id)
                .withUsername(username)
                .withPassword(password)
                .build();

        // Send JSM File
        try {
            sendFileJMSProducer.produceSendFileMessage(messageModel, file);
        } catch (JMSException e) {
            LOG.error(e);

            deliveryEntity.deliveryStatus = FileDeliveryStatusType.SCHEDULED_TO_DELIVER_FAILED;
            fileDeliveryRepository.persist(deliveryEntity);
        }

        return deliveryEntity;
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

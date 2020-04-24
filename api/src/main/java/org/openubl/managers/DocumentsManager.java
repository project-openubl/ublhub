/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openubl.managers;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.openubl.exceptions.InvalidXMLFileException;
import org.openubl.exceptions.StorageException;
import org.openubl.exceptions.UnsupportedDocumentTypeException;
import org.openubl.models.DocumentType;
import org.openubl.models.FileDeliveryStatusType;
import org.openubl.models.FileType;
import org.openubl.models.jpa.FileDeliveryRepository;
import org.openubl.models.jpa.entities.FileDeliveryEntity;
import org.openubl.xml.ubl.XmlContentModel;
import org.openubl.xml.ubl.XmlContentProvider;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class DocumentsManager {

    private static final Logger LOG = Logger.getLogger(DocumentsManager.class);

    public static final Pattern FACTURA_SERIE_REGEX = Pattern.compile("^[F|f].*$");
    public static final Pattern BOLETA_SERIE_REGEX = Pattern.compile("^[B|b].*$");

    @ConfigProperty(name = "openubl.jms.delay")
    Long messageDelay;

    @ConfigProperty(name = "openubl.jms.sendFileQueue")
    String sendFileQueue;

    @ConfigProperty(name = "openubl.sunat.url1")
    String sunatUrl1;

    @Inject
    FilesManager filesManager;

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    XmlContentProvider xmlContentProvider;

    @Inject
    FileDeliveryRepository fileDeliveryRepository;


    /**
     * @param file file to be sent to SUNAT
     * @return true if file was scheduled to be send
     */
    @Transactional
    public FileDeliveryEntity createFileDeliveryAndSchedule(
            byte[] file, String username, String password, String customId
    ) throws InvalidXMLFileException, UnsupportedDocumentTypeException, StorageException {
        // Read file
        XmlContentModel xmlContentModel;
        try {
            xmlContentModel = xmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new InvalidXMLFileException(e);
        }

        // Check document type is supported
        Optional<DocumentType> documentTypeOptional = DocumentType.valueFromDocumentType(xmlContentModel.getDocumentType());
        if (documentTypeOptional.isEmpty()) {
            throw new UnsupportedDocumentTypeException(xmlContentModel.getDocumentType() + " is not supported yet");
        }

        DocumentType documentType = documentTypeOptional.get();
        String serverUrl = getServerUrl(documentType);
        String fileNameWithoutExtension = getFileNameWithoutExtension(documentType, xmlContentModel.getRuc(), xmlContentModel.getDocumentID());

        // Save XML File
        String fileID = filesManager.uploadFile(file, fileNameWithoutExtension + ".xml" ,FileType.XML);
        if (fileID == null) {
            throw new StorageException("Could not save xml file in storage");
        }

        // Create Entity in DB
        FileDeliveryEntity deliveryEntity = FileDeliveryEntity.Builder.aFileDeliveryEntity()
                .withFileID(fileID)
                .withFilename(fileNameWithoutExtension)
                .withRuc(xmlContentModel.getRuc())
                .withDocumentID(xmlContentModel.getDocumentID())
                .withDocumentType(documentType)
                .withDeliveryStatus(FileDeliveryStatusType.SCHEDULED_TO_DELIVER)
                .withServerUrl(serverUrl)
                .withSunatUsername(username)
                .withSunatPassword(password)
                .withCustomId(customId)
                .build();

        fileDeliveryRepository.persist(deliveryEntity);

        // Send JSM File
        produceMessage(deliveryEntity);

        // return result
        return deliveryEntity;
    }

    private void produceMessage(FileDeliveryEntity deliveryEntity) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(messageDelay);

            Queue queue = context.createQueue(sendFileQueue);
            Message message = context.createTextMessage(deliveryEntity.id.toString());
            producer.send(queue, message);
        } finally {
            LOG.debug(deliveryEntity + " was sent to:" + sendFileQueue);
        }
    }

    private String getServerUrl(DocumentType documentType) {
        return sunatUrl1;
    }

    private String getFileNameWithoutExtension(DocumentType type, String ruc, String documentID) {
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

                return MessageFormat.format("{0}-{1}-{2}", ruc, codigoDocumento, documentID);
            case CREDIT_NOTE:
                codigoDocumento = "07";
                return MessageFormat.format("{0}-{1}-{2}", ruc, codigoDocumento, documentID);
            case DEBIT_NOTE:
                codigoDocumento = "08";
                return MessageFormat.format("{0}-{1}-{2}", ruc, codigoDocumento, documentID);
            case VOIDED_DOCUMENT:
            case SUMMARY_DOCUMENT:
                return MessageFormat.format("{0}-{1}", ruc, documentID);
            default:
                throw new IllegalStateException("Invalid type of UBL Document, can not extract Serie-Numero to create fileName");
        }
    }

}

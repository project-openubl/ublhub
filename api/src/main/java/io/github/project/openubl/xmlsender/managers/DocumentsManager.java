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
package io.github.project.openubl.xmlsender.managers;

import io.github.project.openubl.xmlsender.events.EventProvider;
import io.github.project.openubl.xmlsender.events.EventProviderLiteral;
import io.github.project.openubl.xmlsender.files.FilesManager;
import io.github.project.openubl.xmlsender.models.DocumentEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import io.github.project.openubl.xmlsender.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xmlsender.exceptions.StorageException;
import io.github.project.openubl.xmlsender.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xmlsender.models.DocumentType;
import io.github.project.openubl.xmlsender.models.DeliveryStatusType;
import io.github.project.openubl.xmlsender.models.FileType;
import io.github.project.openubl.xmlsender.models.jpa.DocumentRepository;
import io.github.project.openubl.xmlsender.models.jpa.entities.DocumentEntity;
import io.github.project.openubl.xmlsender.xml.ubl.XmlContentModel;
import io.github.project.openubl.xmlsender.xml.ubl.XmlContentProvider;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.regex.Pattern;

@Transactional
@ApplicationScoped
public class DocumentsManager {

    private static final Logger LOG = Logger.getLogger(DocumentsManager.class);

    public static final Pattern FACTURA_SERIE_REGEX = Pattern.compile("^[F|f].*$");
    public static final Pattern BOLETA_SERIE_REGEX = Pattern.compile("^[B|b].*$");

    @ConfigProperty(name = "openubl.sunat.url1")
    String sunatUrl1;

    @ConfigProperty(name = "openubl.event-manager")
    EventProvider.Type eventManager;

    @Inject
    FilesManager filesManager;

    @Inject
    XmlContentProvider xmlContentProvider;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    Event<DocumentEvent.Created> documentCreatedEvent;

    public DocumentEntity createDocumentAndScheduleDelivery(
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
        String deliveryURL = getDeliveryURL(documentType);
        String fileNameWithoutExtension = getFileNameWithoutExtension(documentType, xmlContentModel.getRuc(), xmlContentModel.getDocumentID());

        // Save XML File
        String fileID = filesManager.createFile(file, FileType.getFilename(fileNameWithoutExtension, FileType.XML), FileType.XML);
        if (fileID == null) {
            throw new StorageException("Could not save xml file in storage");
        }

        // Create Entity in DB
        DocumentEntity documentEntity = DocumentEntity.Builder.aDocumentEntity()
                .withFileID(fileID)
                .withFilenameWithoutExtension(fileNameWithoutExtension)
                .withRuc(xmlContentModel.getRuc())
                .withDocumentID(xmlContentModel.getDocumentID())
                .withDocumentType(documentType)
                .withDeliveryStatus(DeliveryStatusType.SCHEDULED_TO_DELIVER)
                .withDeliveryURL(deliveryURL)
                .withSunatUsername(username)
                .withSunatPassword(password)
                .withCustomId(customId)
                .build();

        documentRepository.persist(documentEntity);

        // Fire Event
        documentCreatedEvent
                .select(new EventProviderLiteral(eventManager))
                .fire(() -> documentEntity.id);

        // return result
        return documentEntity;
    }

    private String getDeliveryURL(DocumentType documentType) {
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

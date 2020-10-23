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
package io.github.project.openubl.xsender.core.managers;

import io.github.project.openubl.xsender.core.events.EventProvider;
import io.github.project.openubl.xsender.core.events.EventProviderLiteral;
import io.github.project.openubl.xsender.core.files.FilesManager;
import io.github.project.openubl.xsender.core.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xmlsenderws.webservices.models.DeliveryURLType;
import io.github.project.openubl.xmlsenderws.webservices.utils.UBLUtils;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentProvider;
import io.github.project.openubl.xsender.core.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xsender.core.exceptions.StorageException;
import io.github.project.openubl.xsender.core.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xsender.core.models.DeliveryStatusType;
import io.github.project.openubl.xsender.core.models.DocumentEvent;
import io.github.project.openubl.xsender.core.models.FileType;
import io.github.project.openubl.xsender.core.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.UBLDocumentEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

@Transactional
@ApplicationScoped
public class DocumentsManager {

    private static final Logger LOG = Logger.getLogger(DocumentsManager.class);

    @ConfigProperty(name = "openubl.event-manager")
    EventProvider.Type eventManager;

    @Inject
    FilesManager filesManager;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    Event<DocumentEvent.Created> documentCreatedEvent;

    public UBLDocumentEntity createDocumentAndScheduleDelivery(
            CompanyEntity companyEntity, byte[] xmlFile
    ) throws InvalidXMLFileException, UnsupportedDocumentTypeException, StorageException {
        // Read file
        XmlContentModel xmlContentModel;
        try {
            xmlContentModel = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(xmlFile));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new InvalidXMLFileException(e);
        }

        // Check document type is supported
        Optional<DocumentType> documentTypeOptional = DocumentType.valueFromDocumentType(xmlContentModel.getDocumentType());
        if (documentTypeOptional.isEmpty()) {
            throw new UnsupportedDocumentTypeException(xmlContentModel.getDocumentType() + " is not supported yet");
        }

        DocumentType documentType = documentTypeOptional.get();

        String fileNameWithoutExtension = UBLUtils.getFileNameWithoutExtension(documentType, xmlContentModel.getRuc(), xmlContentModel.getDocumentID())
                .orElseThrow(() -> new IllegalStateException("Invalid type of UBL Document, can not extract fileName"));
        DeliveryURLType deliveryURLType = UBLUtils.getDeliveryURLType(documentType, xmlContentModel)
                .orElseThrow(() -> new IllegalStateException("Invalid type of UBL Document, can not extract deliveryType"));

        // Save XML File
        String fileID = filesManager.createFile(xmlFile, FileType.getFilename(fileNameWithoutExtension, FileType.XML), FileType.XML);
        if (fileID == null) {
            throw new StorageException("Could not save xml file in storage");
        }

        // Create Entity in DB
        UBLDocumentEntity documentEntity = UBLDocumentEntity.Builder.anUBLDocumentEntity()
                .withStorageFile(fileID)
                .withFilename(fileNameWithoutExtension)
                .withRuc(xmlContentModel.getRuc())
                .withDocumentID(xmlContentModel.getDocumentID())
                .withDocumentType(documentType)
                .withDeliveryType(deliveryURLType)
                .withDeliveryStatus(DeliveryStatusType.SCHEDULED_TO_DELIVER)
                .withCompany(companyEntity)
                .build();

        documentRepository.persist(documentEntity);

        // Fire Event
        documentCreatedEvent
                .select(new EventProviderLiteral(eventManager))
                .fire(documentEntity::getId);

        // return result
        return documentEntity;
    }

}

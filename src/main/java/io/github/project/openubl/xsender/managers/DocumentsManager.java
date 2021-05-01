/*
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
package io.github.project.openubl.xsender.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.outbox.quarkus.ExportedEvent;
import io.github.project.openubl.xsender.exceptions.StorageException;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.kafka.producers.UBLDocumentCreatedEventProducer;
import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class DocumentsManager {

    @Inject
    FilesManager filesManager;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Inject
    ObjectMapper objectMapper;

    public UBLDocumentEntity createDocumentAndScheduleDelivery(CompanyEntity companyEntity, byte[] xmlFile) throws StorageException {
        // Save file in Storage

        String fileID = filesManager.createFile(xmlFile, FileType.getFilename(UUID.randomUUID().toString(), FileType.XML), FileType.XML);
        if (fileID == null) {
            throw new StorageException("Could not save xml file in storage");
        }

        // Create Entity

        UBLDocumentEntity documentEntity = UBLDocumentEntity.Builder.anUBLDocumentEntity()
                .withId(UUID.randomUUID().toString())
                .withStorageFile(fileID)
                .withDeliveryStatus(DeliveryStatusType.IN_PROGRESS)
                .withCompany(companyEntity)
                .withCreatedOn(new Date())
                .build();

        documentRepository.persist(documentEntity);

//        try {
//            SunatCredentialsEntity credentials = documentEntity.getCompany().getSunatCredentials();
//            SunatUrlsEntity sunatUrls = documentEntity.getCompany().getSunatUrls();
//
//            UBLDocumentSunatEventRepresentation eventRep = new UBLDocumentSunatEventRepresentation();
//            eventRep.setId(documentEntity.getId());
//            eventRep.setStorageFile(documentEntity.getStorageFile());
//
//            eventRep.setSunatUsername(credentials.getSunatUsername());
//            eventRep.setSunatPassword(credentials.getSunatPassword());
//            eventRep.setSunatUrlFactura(sunatUrls.getSunatUrlFactura());
//            eventRep.setSunatUrlGuiaRemision(sunatUrls.getSunatUrlGuiaRemision());
//            eventRep.setSunatUrlPercepcionRetencion(sunatUrls.getSunatUrlPercepcionRetencion());
//
//            String eventPayload = objectMapper.writeValueAsString(eventRep);
//            event.fire(new UBLDocumentCreatedEventProducer(companyEntity.getId(), eventPayload));
//        } catch (JsonProcessingException e) {
//            throw new IllegalStateException(e);
//        }

        // Result

        return documentEntity;
    }

}

/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * <p>
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.eclipse.org/legal/epl-2.0/
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.managers;

import io.github.project.openubl.xsender.avro.DocumentKafka;
import io.github.project.openubl.xsender.exceptions.StorageException;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
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
    @Channel("read-documents")
    Emitter<DocumentKafka> documentsProducer;

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
                .withDeliveryStatus(DeliveryStatusType.SCHEDULED_TO_DELIVER)
                .withCompany(companyEntity)
                .withCreatedOn(new Date())
                .build();

        documentRepository.persist(documentEntity);

        // Broadcast to kafka

        DocumentKafka xmlFileKafka = DocumentKafka.newBuilder()
                .setId(documentEntity.getId())
                .setRetry(0)
                .build();

        documentsProducer.send(xmlFileKafka);

        // Result
        return documentEntity;
    }

}

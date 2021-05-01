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
package io.github.project.openubl.xsender.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.outbox.quarkus.ExportedEvent;
import io.github.project.openubl.xsender.exceptions.StorageException;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.idm.*;
import io.github.project.openubl.xsender.kafka.idm.CompanyCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.idm.UBLDocumentCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityEventProducer;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.kafka.utils.EventEntityToRepresentation;
import io.github.project.openubl.xsender.managers.CompanyManager;
import io.github.project.openubl.xsender.managers.DocumentsManager;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Transactional
@ApplicationScoped
public class DefaultCompanyResource implements CompanyResource {

    private static final Logger LOG = Logger.getLogger(DefaultCompanyResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    // Managers

    @Inject
    CompanyManager companyManager;

    @Inject
    DocumentsManager documentsManager;

    @Inject
    FilesManager filesManager;

    // Events

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public CompanyRepresentation getCompany(String company) {
        CompanyEntity organizationEntity = companyRepository.findByNameAndOwner(company, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        return EntityToRepresentation.toRepresentation(organizationEntity);
    }

    @Override
    public CompanyRepresentation updateCompany(String company, CompanyRepresentation rep) {
        CompanyEntity companyEntity = companyRepository.findByNameAndOwner(company, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity updatedCompanyEntity = companyManager.updateCompany(rep, companyEntity);

        try {
            CompanyCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(companyEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.company, EventType.UPDATED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }

        return EntityToRepresentation.toRepresentation(updatedCompanyEntity);
    }

    @Override
    public void deleteCompany(String company) {
        CompanyEntity companyEntity = companyRepository.findByNameAndOwner(company, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        companyRepository.deleteById(companyEntity.getId());

        try {
            CompanyCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(companyEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.company, EventType.DELETED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }
    }

    @Override
    public void updateCompanySUNATCredentials(String company, SunatCredentialsRepresentation rep) {
        CompanyEntity companyEntity = companyRepository.findByNameAndOwner(company, userIdentity.getUsername()).orElseThrow(NoClassDefFoundError::new);
        companyManager.updateCorporateCredentials(rep, companyEntity);

        try {
            CompanyCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(companyEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.company, EventType.UPDATED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }
    }

    @Override
    public PageRepresentation<DocumentRepresentation> listDocuments(@NotNull String company, String filterText, Integer offset, Integer limit, List<String> sortBy) {
        CompanyEntity companyEntity = companyRepository.findByNameAndOwner(company, userIdentity.getUsername()).orElseThrow(NotFoundException::new);

        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, UBLDocumentRepository.SORT_BY_FIELDS);

        PageModel<UBLDocumentEntity> pageModel;
        if (filterText != null && !filterText.trim().isEmpty()) {
            pageModel = documentRepository.list(companyEntity, filterText, pageBean, sortBeans);
        } else {
            pageModel = documentRepository.list(companyEntity, pageBean, sortBeans);
        }

        return EntityToRepresentation.toRepresentation(pageModel, EntityToRepresentation::toRepresentation);
    }

    @Override
    public Response createDocument(@NotNull String company, MultipartFormDataInput input) {
        CompanyEntity companyEntity = companyRepository.findByNameAndOwner(company, userIdentity.getUsername()).orElseThrow(NotFoundException::new);

        //

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<InputPart> fileInputParts = uploadForm.get("file");

        if (fileInputParts == null) {
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        byte[] xmlFile = null;

        try {
            for (InputPart inputPart : fileInputParts) {
                InputStream fileInputStream = inputPart.getBody(InputStream.class, null);
                xmlFile = IOUtils.toByteArray(fileInputStream);
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not extract required data from upload/form");
        }

        if (xmlFile == null || xmlFile.length == 0) {
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is empty");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        UBLDocumentEntity documentEntity;
        try {
            documentEntity = documentsManager.createDocumentAndScheduleDelivery(companyEntity, xmlFile);

            try {
                UBLDocumentCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(documentEntity);
                String eventPayload = objectMapper.writeValueAsString(eventRep);
                event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.document, EventType.CREATED, eventPayload));
            } catch (JsonProcessingException e) {
                LOG.error(e);
            }
        } catch (StorageException e) {
            LOG.error(e);
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

        return Response.status(Response.Status.OK)
                .entity(EntityToRepresentation.toRepresentation(documentEntity))
                .build();
    }

    @Override
    public DocumentRepresentation getDocument(@NotNull String company, @NotNull String documentId) {
        return null;
    }

    @Override
    public Response getDocumentFile(@NotNull String company, @NotNull String documentId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            throw new NotFoundException();
        }

        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());
        return Response.ok(file)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentEntity.getDocumentID() + ".xml" + "\""
                )
                .build();
    }

    @Override
    public String getDocumentFileLink(@NotNull String company, @NotNull String documentId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            throw new NotFoundException();
        }

        return filesManager.getFileLink(documentEntity.getStorageFile());
    }

    @Override
    public Response getDocumentCDR(@NotNull String company, @NotNull String documentId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            throw new NotFoundException();
        }
        if (documentEntity.getStorageCdr() == null) {
            throw new NotFoundException();
        }

        byte[] file = filesManager.getFileAsBytesWithoutUnzipping(documentEntity.getStorageCdr());
        return Response.ok(file)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentEntity.getDocumentType() + ".zip" + "\""
                )
                .build();
    }

    @Override
    public String getDocumentCDRLink(@NotNull String company, @NotNull String documentId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        if (documentEntity == null) {
            throw new NotFoundException();
        }
        if (documentEntity.getStorageCdr() == null) {
            throw new NotFoundException();
        }

        return filesManager.getFileLink(documentEntity.getStorageCdr());
    }

}


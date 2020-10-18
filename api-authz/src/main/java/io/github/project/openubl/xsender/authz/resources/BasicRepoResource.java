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
package io.github.project.openubl.xsender.authz.resources;

import io.github.project.openubl.xsender.core.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xsender.core.exceptions.StorageException;
import io.github.project.openubl.xsender.core.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xsender.core.files.FilesManager;
import io.github.project.openubl.xsender.core.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.core.idm.ErrorRepresentation;
import io.github.project.openubl.xsender.core.idm.RepositoryRepresentation;
import io.github.project.openubl.xsender.core.managers.DocumentsManager;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.RepoRepository;
import io.github.project.openubl.xsender.core.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.github.project.openubl.xsender.core.models.jpa.entities.RepositoryEntity;
import io.github.project.openubl.xsender.core.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.core.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.core.resources.RepoResource;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Transactional
@ApplicationScoped
public class BasicRepoResource implements RepoResource {

    private static final Logger LOG = Logger.getLogger(BasicRepoResource.class);

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    RepoRepository repoRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    DocumentsManager documentsManager;

    @Inject
    FilesManager filesManager;

    public RepositoryRepresentation getRepository(String owner, String repo) {
        return new RepositoryRepresentation();
    }

    public RepositoryRepresentation updateRepository(String owner, String repo, RepositoryRepresentation rep) {
        return new RepositoryRepresentation();
    }

    public void deleteRepository(String owner, String repo) {

    }

    public Response createDocument(String owner, String repo, MultipartFormDataInput input) {
        OrganizationEntity organizationEntity = organizationRepository.findByName(owner).orElseThrow(NotFoundException::new);
        RepositoryEntity repositoryEntity = repoRepository.findByName(organizationEntity, repo).orElseThrow(NotFoundException::new);

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
            documentEntity = documentsManager.createDocumentAndScheduleDelivery(repositoryEntity, xmlFile);
        } catch (InvalidXMLFileException e) {
            LOG.error(e);
            ErrorRepresentation error = new ErrorRepresentation("Form[file] is not a valid XML file or is corrupted");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (UnsupportedDocumentTypeException e) {
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (StorageException e) {
            LOG.error(e);
            ErrorRepresentation error = new ErrorRepresentation(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

        return Response.status(Response.Status.OK)
                .entity(EntityToRepresentation.toRepresentation(documentEntity))
                .build();
    }

    public List<DocumentRepresentation> listDocuments(String owner, String repo) {
        return new ArrayList<>();
    }

    public DocumentRepresentation getDocument(String owner, String repo) {
        return new DocumentRepresentation();
    }

    public Response getDocumentFile(String owner, String repo, String docId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(docId);
        if (documentEntity == null) {
            throw new NotFoundException();
        }

        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());
        return Response.ok(file)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentEntity.getFilename() + ".xml" + "\""
                )
                .build();
    }

    public String getDocumentFileLink(String owner, String repo, String docId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(docId);
        if (documentEntity == null) {
            throw new NotFoundException();
        }

        return filesManager.getFileLink(documentEntity.getStorageFile());
    }

    public Response getDocumentCDR(String owner, String repo, String docId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(docId);
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
                        "attachment; filename=\"" + documentEntity.getFilename() + ".zip" + "\""
                )
                .build();
    }

    public String getDocumentCDRLink(String owner, String repo, String docId) {
        UBLDocumentEntity documentEntity = documentRepository.findById(docId);
        if (documentEntity == null) {
            throw new NotFoundException();
        }
        if (documentEntity.getStorageCdr() == null) {
            throw new NotFoundException();
        }

        return filesManager.getFileLink(documentEntity.getStorageCdr());
    }
}


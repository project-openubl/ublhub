/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.resources;

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

import io.github.project.openubl.ublhub.dto.DocumentDto;
import io.github.project.openubl.ublhub.dto.DocumentInputDto;
import io.github.project.openubl.ublhub.dto.ErrorDto;
import io.github.project.openubl.ublhub.dto.PageDto;
import io.github.project.openubl.ublhub.files.FilesManager;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.DocumentMapper;
import io.github.project.openubl.ublhub.models.FilterDocumentBean;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SearchBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.resources.exceptions.NoCertificateToSignFoundException;
import io.github.project.openubl.ublhub.resources.utils.ResourceUtils;
import io.github.project.openubl.ublhub.resources.validation.JSONValidatorManager;
import io.github.project.openubl.ublhub.scheduler.SchedulerManager;
import io.github.project.openubl.ublhub.ubl.builder.xmlgenerator.XMLGeneratorManager;
import io.github.project.openubl.ublhub.ubl.builder.xmlgenerator.XMLResult;
import io.github.project.openubl.xbuilder.signature.XMLSigner;
import io.github.project.openubl.xbuilder.signature.XmlSignatureHelper;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.company;
import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.project;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class DocumentResource {

    private static final Logger LOG = Logger.getLogger(DocumentResource.class);

    @Inject
    FilesManager filesManager;

    @Inject
    SchedulerManager schedulerManager;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    XMLGeneratorManager xmlGeneratorManager;

    @Inject
    KeyManager keystore;

    @Inject
    JSONValidatorManager jsonManager;

    @Inject
    DocumentMapper documentMapper;

    Function<DocumentDto, RestResponse<DocumentDto>> documentDtoCreatedResponse = (dto) -> RestResponse.ResponseBuilder
            .<DocumentDto>create(RestResponse.Status.CREATED)
            .entity(dto)
            .build();
    Function<DocumentDto, RestResponse<DocumentDto>> documentDtoSuccessResponse = (dto) -> RestResponse.ResponseBuilder
            .<DocumentDto>create(RestResponse.Status.OK)
            .entity(dto)
            .build();
    Supplier<RestResponse<DocumentDto>> documentDtoNotFoundResponse = () -> RestResponse.ResponseBuilder
            .<DocumentDto>create(RestResponse.Status.NOT_FOUND)
            .build();
    Supplier<RestResponse<DocumentDto>> documentDtoBadRequestResponse = () -> RestResponse.ResponseBuilder
            .<DocumentDto>create(RestResponse.Status.BAD_REQUEST)
            .build();

    public static class UploadFormData {
        @RestForm("file")
        public FileUpload file;
    }

    private ComponentOwner getOwner(String companyId) {
        return ComponentOwner.builder()
                .type(company)
                .id(companyId)
                .build();
    }

    public Document createAndSignXML(ProjectEntity projectEntity, DocumentInputDto inputDto) throws NoCertificateToSignFoundException, MarshalException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, ParserConfigurationException, XMLSignatureException, SAXException {
        XMLResult xmlResult = xmlGeneratorManager.createXMLString(projectEntity, inputDto);
        String algorithm = inputDto.getSpec().getSignature() != null ? inputDto.getSpec().getSignature().getAlgorithm() : Algorithm.RS256;

        KeyWrapper keyWrapper = null;

        CompanyEntity companyEntity = companyRepository.findByRuc(projectEntity.getId(), xmlResult.getRuc());
        if (companyEntity != null) {
            ComponentOwner companyOwner = ComponentOwner.builder()
                    .id(companyEntity.getId())
                    .type(company)
                    .build();
            keyWrapper = keystore.getActiveKeyWithoutFallback(companyOwner, KeyUse.SIG, algorithm);
        }

        if (keyWrapper == null) {
            ComponentOwner projectOwner = ComponentOwner.builder()
                    .id(projectEntity.getId())
                    .type(project)
                    .build();
            keyWrapper = keystore.getActiveKeyWithoutFallback(projectOwner, KeyUse.SIG, algorithm);
        }

        if (keyWrapper == null) {
            throw new NoCertificateToSignFoundException("Could not find a key to sign neither in project or company level");
        }

        KeyManager.ActiveRsaKey rsaKey = KeyManager.ActiveRsaKey.builder()
                .kid(keyWrapper.getKid())
                .privateKey((PrivateKey) keyWrapper.getPrivateKey())
                .publicKey((PublicKey) keyWrapper.getPublicKey())
                .certificate(keyWrapper.getCertificate())
                .build();

        return XMLSigner.signXML(xmlResult.getXml(), "OPENUBL", rsaKey.getCertificate(), rsaKey.getPrivateKey());
    }

    public String saveXML(Document xml) throws Exception {
        byte[] bytes = XmlSignatureHelper.getBytesFromDocument(xml);
        return filesManager.createFile(bytes, true);
    }

    public UBLDocumentEntity createAndScheduleSend(ProjectEntity projectEntity, String fileSavedId) {
        UBLDocumentEntity documentEntity = new UBLDocumentEntity();
        documentEntity.setId(UUID.randomUUID().toString());
        documentEntity.setXmlFileId(fileSavedId);
        documentEntity.setProjectId(projectEntity.getId());
        documentEntity.persist();

        schedulerManager.sendDocumentToSUNAT(documentEntity);
        return documentEntity;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{projectId}/upload/document")
    public RestResponse<DocumentDto> uploadXML(
            @PathParam("projectId") @NotNull String projectId,
            @MultipartForm UploadFormData formData
    ) throws FileNotFoundException {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return documentDtoNotFoundResponse.get();
        }

        String fileId = filesManager.createFile(formData.file.uploadedFile().toFile(), true);
        UBLDocumentEntity entity = createAndScheduleSend(projectEntity, fileId);
        DocumentDto dto = documentMapper.toDto(entity);

        return documentDtoCreatedResponse.apply(dto);
    }

    @POST
    @Path("/{projectId}/documents")
    public RestResponse<DocumentDto> createDocument(
            @PathParam("projectId") @NotNull String projectId,
            @NotNull JsonObject jsonObject
    ) throws Exception {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return documentDtoNotFoundResponse.get();
        }

        Boolean isValid = jsonManager.validateJsonObject(jsonObject);
        if (!isValid) {
            return documentDtoBadRequestResponse.get();
        }

        DocumentInputDto documentInputDto = jsonManager.getDocumentInputDtoFromJsonObject(jsonObject);
        Document document;
        try {
            document = createAndSignXML(projectEntity, documentInputDto);
        } catch (NoCertificateToSignFoundException | MarshalException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IOException | ParserConfigurationException | XMLSignatureException | SAXException e) {
            return RestResponse.ResponseBuilder
                    .<DocumentDto>create(RestResponse.Status.BAD_REQUEST)
                    .build();
        }

        String documentId = saveXML(document);
        UBLDocumentEntity documentEntity = createAndScheduleSend(projectEntity, documentId);

        DocumentDto documentDto = documentMapper.toDto(documentEntity);
        return documentDtoCreatedResponse.apply(documentDto);
    }

    @POST
    @Path("/{projectId}/enrich-document")
    public RestResponse<?> enrichDocuments(
            @PathParam("projectId") @NotNull String projectId,
            @NotNull JsonObject jsonObject
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return documentDtoNotFoundResponse.get();
        }

        Boolean isValid = jsonManager.validateJsonObject(jsonObject);
        if (!isValid) {
            return RestResponse.ResponseBuilder
                    .<String>create(RestResponse.Status.BAD_REQUEST)
                    .entity("Invalid document")
                    .build();
        }

        DocumentInputDto documentInputDto = jsonManager.getDocumentInputDtoFromJsonObject(jsonObject);

        try {
            Object inputDocument = xmlGeneratorManager.enrichDocument(documentInputDto);

            return RestResponse.ResponseBuilder
                    .create(RestResponse.Status.OK)
                    .entity(inputDocument)
                    .build();
        } catch (Throwable e) {
            String message = e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : e.getCause().getMessage();
            ErrorDto errorDto = ErrorDto.builder()
                    .message(message)
                    .build();
            return RestResponse.ResponseBuilder
                    .create(RestResponse.Status.BAD_REQUEST)
                    .entity(errorDto)
                    .build();
        }
    }

    @POST
    @Path("/{projectId}/render-document")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public RestResponse<String> renderDocument(
            @PathParam("projectId") @NotNull String projectId,
            @NotNull JsonObject jsonObject
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return RestResponse.ResponseBuilder
                    .<String>create(RestResponse.Status.NOT_FOUND)
                    .build();
        }

        Boolean isValid = jsonManager.validateJsonObject(jsonObject);
        if (!isValid) {
            return RestResponse.ResponseBuilder
                    .<String>create(RestResponse.Status.OK)
                    .entity("Invalid document, it does not comply with schema")
                    .build();
        }

        DocumentInputDto documentInputDto = jsonManager.getDocumentInputDtoFromJsonObject(jsonObject);

        String result;
        try {
            result = xmlGeneratorManager.renderDocument(documentInputDto);
        } catch (Throwable e) {
            result = e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : e.getCause().getMessage();
        }

        return RestResponse.ResponseBuilder
                .<String>create(RestResponse.Status.OK)
                .entity(result)
                .build();
    }

    @GET
    @Path("/{projectId}/documents/{documentId}")
    public RestResponse<DocumentDto> getDocument(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("documentId") @NotNull String documentId
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(projectId, documentId);
        if (documentEntity == null) {
            return documentDtoNotFoundResponse.get();
        }

        DocumentDto dto = documentMapper.toDto(documentEntity);
        return documentDtoSuccessResponse.apply(dto);
    }

    @GET
    @Path("/{projectId}/documents/{documentId}/xml")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentXMLFile(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("documentId") @NotNull String documentId,
            @QueryParam("unzip") @DefaultValue("true") boolean unzip
    ) {
        String mediaType = !unzip ? "application/zip" : MediaType.APPLICATION_XML;
        String fileExtension = !unzip ? ".zip" : ".xml";

        UBLDocumentEntity documentEntity = documentRepository.findById(projectId, documentId);
        if (documentEntity == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        String fileName = documentEntity.getXmlData() != null && documentEntity.getXmlData().getSerieNumero() != null ?
                documentEntity.getXmlData().getSerieNumero() :
                UUID.randomUUID().toString();

        byte[] bytes = unzip ?
                filesManager.getFileAsBytesAfterUnzip(documentEntity.getXmlFileId()) :
                filesManager.getFileAsBytesWithoutUnzipping(documentEntity.getXmlFileId());

        return Response
                .ok(bytes, mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + fileExtension + "\"")
                .build();
    }

    @GET
    @Path("/{projectId}/documents/{documentId}/cdr")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentCdrFile(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("documentId") @NotNull String documentId,
            @QueryParam("unzip") @DefaultValue("true") boolean unzip
    ) {
        String mediaType = !unzip ? "application/zip" : MediaType.APPLICATION_XML;
        String fileExtension = !unzip ? ".zip" : ".xml";

        UBLDocumentEntity documentEntity = documentRepository.findById(projectId, documentId);
        if (documentEntity == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        String fileName = documentEntity.getXmlData() != null && documentEntity.getXmlData().getSerieNumero() != null ?
                documentEntity.getXmlData().getSerieNumero() :
                UUID.randomUUID().toString();

        byte[] bytes = unzip ?
                filesManager.getFileAsBytesAfterUnzip(documentEntity.getCdrFileId()) :
                filesManager.getFileAsBytesWithoutUnzipping(documentEntity.getCdrFileId());

        return Response
                .ok(bytes, mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "_cdr" + fileExtension + "\"")
                .build();
    }

    @GET
    @Path("/{projectId}/documents")
    public RestResponse<PageDto<DocumentDto>> getDocuments(
            @PathParam("projectId") @NotNull String projectId,
            @QueryParam("ruc") List<String> ruc,
            @QueryParam("documentType") List<String> documentType,
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("created:desc") List<String> sortBy
    ) {
        Function<PageDto<DocumentDto>, RestResponse<PageDto<DocumentDto>>> successResponse = (dto) -> RestResponse.ResponseBuilder
                .<PageDto<DocumentDto>>create(RestResponse.Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<PageDto<DocumentDto>>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<PageDto<DocumentDto>>create(RestResponse.Status.NOT_FOUND)
                .build();

        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, UBLDocumentRepository.SORT_BY_FIELDS);

        FilterDocumentBean filters = FilterDocumentBean.builder()
                .ruc(ruc)
                .documentType(documentType)
                .build();

        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return notFoundResponse.get();
        }

        SearchBean<UBLDocumentEntity> searchResult;
        if (filterText != null && !filterText.trim().isEmpty()) {
            searchResult = documentRepository.list(projectEntity, filterText, filters, pageBean, sortBeans);
        } else {
            searchResult = documentRepository.list(projectEntity, filters, pageBean, sortBeans);
        }

        Long count = searchResult.count();
        List<DocumentDto> items = searchResult.list().stream()
                .map(entity -> documentMapper.toDto(entity))
                .collect(Collectors.toList());

        PageDto<DocumentDto> pageDto = PageDto.<DocumentDto>builder()
                .count(count)
                .items(items)
                .build();
        return successResponse.apply(pageDto);
    }

//    @Transactional(Transactional.TxType.NOT_SUPPORTED)
//    @POST
//    @Path("/{documentId}/retry-send")
//    public Response resend(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("documentId") @NotNull String documentId
//    ) {
//        try {
//            transaction.begin();
//
//            NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//            UBLDocumentEntity documentEntity = documentRepository.findById(namespaceEntity, documentId).orElseThrow(NotFoundException::new);
//
//            documentEntity.setInProgress(true);
//            documentEntity.setError(null);
//            documentEntity.setScheduledDelivery(null);
//
//            // Commit transaction
//            transaction.commit();
//        } catch (NotSupportedException | SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
//            LOG.error(e);
//            try {
//                transaction.rollback();
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//            } catch (SystemException systemException) {
//                LOG.error(systemException);
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//            }
//        }
//
//        // Event: new document has been created
//        documentEventManager.fire(new DocumentEvent(documentId, namespaceId));
//
//        // Send file
//        Message<String> message = Message.of(documentId)
//                .withNack(throwable -> messageSenderManager.handleDocumentMessageError(documentId, throwable));
//        messageSenderManager.sendToDocumentQueue(message);
//
//        return Response.status(Response.Status.OK)
//                .build();
//    }

}



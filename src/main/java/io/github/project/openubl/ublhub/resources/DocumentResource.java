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
import io.github.project.openubl.ublhub.dto.PageDto;
import io.github.project.openubl.ublhub.files.FilesMutiny;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.DocumentMapper;
import io.github.project.openubl.ublhub.models.FilterDocumentBean;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.resources.exceptions.AbstractBadRequestException;
import io.github.project.openubl.ublhub.resources.exceptions.NoCertificateToSignFoundException;
import io.github.project.openubl.ublhub.resources.utils.ResourceUtils;
import io.github.project.openubl.ublhub.resources.validation.JSONValidatorManager;
import io.github.project.openubl.ublhub.scheduler.SchedulerManager;
import io.github.project.openubl.ublhub.ubl.builder.xmlgenerator.XMLGeneratorManager;
import io.github.project.openubl.ublhub.ubl.builder.xmlgenerator.XMLResult;
import io.github.project.openubl.xbuilder.signature.XMLSigner;
import io.github.project.openubl.xbuilder.signature.XmlSignatureHelper;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.company;
import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.project;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class DocumentResource {

    private static final Logger LOG = Logger.getLogger(DocumentResource.class);

    @Inject
    FilesMutiny filesMutiny;

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

    public Uni<Document> createAndSignXML(ProjectEntity projectEntity, DocumentInputDto inputDto) {
        Uni<XMLResult> xmlUni = xmlGeneratorManager.createXMLString(projectEntity, inputDto);
        String algorithm = inputDto.getSpec().getSignature() != null ? inputDto.getSpec().getSignature().getAlgorithm() : Algorithm.RS256;

        return xmlUni.chain(xmlResult -> {
                    ComponentOwner projectOwner = ComponentOwner.builder()
                            .id(projectEntity.getId())
                            .type(project)
                            .build();
                    Uni<Optional<KeyWrapper>> projectKeyUni = keystore.getActiveKeyWithoutFallback(projectOwner, KeyUse.SIG, algorithm);

                    Uni<Optional<KeyWrapper>> companyKeyUni = companyRepository
                            .findByRuc(projectEntity.getId(), xmlResult.getRuc())
                            .onItem().ifNotNull().transformToUni(companyEntity -> {
                                ComponentOwner companyOwner = ComponentOwner.builder()
                                        .id(companyEntity.getId())
                                        .type(company)
                                        .build();
                                return keystore.getActiveKeyWithoutFallback(companyOwner, KeyUse.SIG, algorithm);
                            })
                            .onItem().ifNull().continueWith(Optional::empty);

                    return Uni.combine().all().unis(projectKeyUni, companyKeyUni)
                            .asTuple()
                            .chain(keys -> Uni.createFrom().<KeyWrapper>emitter(emitter -> {
                                Optional<KeyWrapper> projectKey = keys.getItem1();
                                Optional<KeyWrapper> companyKey = keys.getItem2();

                                if (companyKey.isPresent()) {
                                    emitter.complete(companyKey.get());
                                } else if (projectKey.isPresent()) {
                                    emitter.complete(projectKey.get());
                                } else {
                                    emitter.fail(new NoCertificateToSignFoundException("Could not find a key to sign neither in project or company level"));
                                }
                            }))
                            .map(keyWrapper -> KeyManager.ActiveRsaKey.builder()
                                    .kid(keyWrapper.getKid())
                                    .privateKey((PrivateKey) keyWrapper.getPrivateKey())
                                    .publicKey((PublicKey) keyWrapper.getPublicKey())
                                    .certificate(keyWrapper.getCertificate())
                                    .build()
                            )
                            .chain(rsaKey -> Uni.createFrom().emitter(emitter -> {
                                try {
                                    Document signedXML = XMLSigner.signXML(xmlResult.getXml(), "OPENUBL", rsaKey.getCertificate(), rsaKey.getPrivateKey());
                                    emitter.complete(signedXML);
                                } catch (Exception e) {
                                    emitter.fail(e);
                                }
                            }));
                }
        );
    }

    public Uni<String> saveXML(Document xml) {
        return Uni.createFrom()
                .<byte[]>emitter(emitter -> {
                    try {
                        byte[] bytes = XmlSignatureHelper.getBytesFromDocument(xml);
                        emitter.complete(bytes);
                    } catch (Exception e) {
                        emitter.fail(e);
                    }
                })
                .chain(bytes -> filesMutiny.createFile(bytes, true));
    }

    public Uni<UBLDocumentEntity> createAndScheduleSend(ProjectEntity projectEntity, String fileSavedId) {
        return Uni.createFrom().item(fileSavedId)
                .chain(xmlFileId -> {
                    UBLDocumentEntity documentEntity = new UBLDocumentEntity();
                    documentEntity.setId(UUID.randomUUID().toString());
                    documentEntity.setXmlFileId(xmlFileId);
                    documentEntity.setProjectId(projectEntity.getId());
                    return documentEntity.<UBLDocumentEntity>persist();
                })

                // Schedule send
                .chain(documentEntity -> schedulerManager
                        .sendDocumentToSUNAT(documentEntity)
                        .map(unused -> documentEntity)
                );
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{projectId}/upload/document")
    public Uni<RestResponse<DocumentDto>> uploadXML(
            @PathParam("projectId") @NotNull String projectId,
            @MultipartForm UploadFormData formData
    ) {
        Uni<RestResponse<DocumentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> filesMutiny
                        .createFile(formData.file.uploadedFile().toFile(), true)
                        .chain(fileId -> createAndScheduleSend(projectEntity, fileId))
                        .map(entity -> documentMapper.toDto(entity))
                        .map(documentDtoCreatedResponse)
                )
                .onItem().ifNull().continueWith(documentDtoNotFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @POST
    @Path("/{projectId}/documents")
    public Uni<RestResponse<DocumentDto>> createDocument(
            @PathParam("projectId") @NotNull String projectId,
            @NotNull JsonObject jsonObject
    ) {
        Uni<RestResponse<DocumentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> {
                    Boolean isValid = jsonManager.validateJsonObject(jsonObject);
                    if (!isValid) {
                        return Uni.createFrom().item(documentDtoBadRequestResponse);
                    }

                    DocumentInputDto documentInputDto = jsonManager.getDocumentInputDtoFromJsonObject(jsonObject);
                    return createAndSignXML(projectEntity, documentInputDto)
                            .chain(this::saveXML)
                            .chain(documentId -> createAndScheduleSend(projectEntity, documentId))
                            .map(entity -> documentMapper.toDto(entity))
                            .map(dto -> documentDtoCreatedResponse.apply(dto));
                })
                .onItem().ifNull().continueWith(documentDtoNotFoundResponse);

        return Panache.withTransaction(() -> restResponseUni)
                .onFailure(AbstractBadRequestException.class).recoverWithItem(() -> documentDtoBadRequestResponse.get());
    }

    @GET
    @Path("/{projectId}/documents/{documentId}")
    public Uni<RestResponse<DocumentDto>> getDocument(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("documentId") @NotNull String documentId
    ) {
        Uni<RestResponse<DocumentDto>> restResponseUni = documentRepository.findById(projectId, documentId)
                .onItem().ifNotNull().transform(entity -> {
                    DocumentDto dto = documentMapper.toDto(entity);
                    return documentDtoSuccessResponse.apply(dto);
                })
                .onItem().ifNull().continueWith(documentDtoNotFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @GET
    @Path("/{projectId}/documents/{documentId}/xml")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Uni<Response> getDocumentXMLFile(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("documentId") @NotNull String documentId,
            @QueryParam("unzip") @DefaultValue("true") boolean unzip
    ) {
        String mediaType = !unzip ? "application/zip" : MediaType.APPLICATION_XML;
        String fileExtension = !unzip ? ".zip" : ".xml";

        Uni<Response> restResponseUni = documentRepository.findById(projectId, documentId)
                .onItem().ifNotNull().transformToUni((documentEntity) -> {
                    String fileName = documentEntity.getXmlData() != null && documentEntity.getXmlData().getSerieNumero() != null ?
                            documentEntity.getXmlData().getSerieNumero() :
                            UUID.randomUUID().toString();

                    Uni<byte[]> fileUni = unzip ?
                            filesMutiny.getFileAsBytesAfterUnzip(documentEntity.getXmlFileId()) :
                            filesMutiny.getFileAsBytesWithoutUnzipping(documentEntity.getXmlFileId());
                    return fileUni.map(bytes -> Response
                            .ok(bytes, mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + fileExtension + "\"")
                            .build());
                })
                .onItem().ifNull().continueWith(() -> Response
                        .status(Response.Status.NOT_FOUND)
                        .build()
                );

        return Panache.withTransaction(() -> restResponseUni);
    }

    @GET
    @Path("/{projectId}/documents/{documentId}/cdr")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Uni<Response> getDocumentCdrFile(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("documentId") @NotNull String documentId,
            @QueryParam("unzip") @DefaultValue("true") boolean unzip
    ) {
        String mediaType = !unzip ? "application/zip" : MediaType.APPLICATION_XML;
        String fileExtension = !unzip ? ".zip" : ".xml";

        Uni<Response> restResponseUni = documentRepository.findById(projectId, documentId)
                .onItem().ifNotNull().transformToUni((documentEntity) -> {
                    String fileName = documentEntity.getXmlData() != null && documentEntity.getXmlData().getSerieNumero() != null ?
                            documentEntity.getXmlData().getSerieNumero() :
                            UUID.randomUUID().toString();

                    Uni<byte[]> fileUni = unzip ?
                            filesMutiny.getFileAsBytesAfterUnzip(documentEntity.getCdrFileId()) :
                            filesMutiny.getFileAsBytesWithoutUnzipping(documentEntity.getCdrFileId());
                    return fileUni.map(bytes -> Response
                            .ok(bytes, mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "_cdr" + fileExtension + "\"")
                            .build());
                })
                .onItem().ifNull().continueWith(() -> Response
                        .status(Response.Status.NOT_FOUND)
                        .build()
                );

        return Panache.withTransaction(() -> restResponseUni);
    }

    @GET
    @Path("/{projectId}/documents")
    public Uni<RestResponse<PageDto<DocumentDto>>> getDocuments(
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

        Uni<RestResponse<PageDto<DocumentDto>>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> {
                    UniAndGroup2<List<UBLDocumentEntity>, Long> searchResult;
                    if (filterText != null && !filterText.trim().isEmpty()) {
                        searchResult = documentRepository.list(projectEntity, filterText, filters, pageBean, sortBeans);
                    } else {
                        searchResult = documentRepository.list(projectEntity, filters, pageBean, sortBeans);
                    }
                    return searchResult.asTuple()
                            .map(objects -> {
                                Long count = objects.getItem2();
                                List<DocumentDto> items = objects.getItem1().stream()
                                        .map(entity -> documentMapper.toDto(entity))
                                        .collect(Collectors.toList());

                                return PageDto.<DocumentDto>builder()
                                        .count(count)
                                        .items(items)
                                        .build();
                            })
                            .map(successResponse);
                })
                .onItem().ifNull().continueWith(notFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
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



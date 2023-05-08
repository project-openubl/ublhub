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

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import io.github.project.openubl.ublhub.documents.DocumentImportResult;
import io.github.project.openubl.ublhub.documents.DocumentRoute;
import io.github.project.openubl.ublhub.dto.DocumentDto;
import io.github.project.openubl.ublhub.dto.PageDto;
import io.github.project.openubl.ublhub.files.FilesManager;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.DocumentMapper;
import io.github.project.openubl.ublhub.models.*;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.qute.DbTemplateLocator;
import io.github.project.openubl.ublhub.resources.utils.ResourceUtils;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.*;
import io.github.project.openubl.xbuilder.content.jaxb.models.*;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import io.github.project.openubl.xsender.files.xml.XmlContentProvider;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import org.apache.camel.ProducerTemplate;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.mapstruct.factory.Mappers;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class DocumentResource {

    private static final Logger LOG = Logger.getLogger(DocumentResource.class);

    @Inject
    Engine engine;

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    FilesManager filesManager;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

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

    private ComponentOwner getOwner(String project, String ruc) {
        return ComponentOwner.builder()
                .project(project)
                .ruc(ruc)
                .build();
    }

    private RestResponse<DocumentDto> mapDocumentImportResult(String project, DocumentImportResult importResult) {
        if (importResult.getErrorMessage() != null) {
            return documentDtoBadRequestResponse.get();
        } else {
            QuarkusTransaction.begin();

            UBLDocumentEntity documentEntity = documentRepository.findById(project, importResult.getDocumentId());
            DocumentDto documentDto = documentMapper.toDto(documentEntity);

            QuarkusTransaction.commit();
            return documentDtoCreatedResponse.apply(documentDto);
        }
    }

    @Transactional(Transactional.TxType.NEVER)
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{project}/upload/document")
    public RestResponse<DocumentDto> uploadXML(
            @PathParam("project") @NotNull String project,
            @MultipartForm UploadFormData formData
    ) {
        File file = formData.file.uploadedFile().toFile();

        Map<String, Object> headers = Map.of(DocumentRoute.DOCUMENT_PROJECT, project);
        DocumentImportResult importResult = producerTemplate.requestBodyAndHeaders("direct:import-xml", file, headers, DocumentImportResult.class);

        return mapDocumentImportResult(project, importResult);
    }

    @Transactional(Transactional.TxType.NEVER)
    @POST
    @Path("/{project}/documents")
    public RestResponse<DocumentDto> createDocument(
            @PathParam("project") @NotNull String project,
            @NotNull JsonObject jsonObject
    ) {
        Map<String, Object> headers = Map.of(DocumentRoute.DOCUMENT_PROJECT, project);
        DocumentImportResult importResult = producerTemplate.requestBodyAndHeaders("direct:import-json", jsonObject, headers, DocumentImportResult.class);

        return mapDocumentImportResult(project, importResult);
    }

    @POST
    @Path("/{project}/enrich-document")
    public RestResponse<?> enrichDocuments(
            @PathParam("project") @NotNull String project,
            @NotNull JsonObject jsonObject
    ) {
        Map<String, Object> headers = Map.of(DocumentRoute.DOCUMENT_PROJECT, project);
        Object result = producerTemplate.requestBodyAndHeaders("direct:enrich-json", jsonObject, headers, Object.class);

        if (result instanceof DocumentImportResult importResult) {
            return RestResponse.ResponseBuilder
                    .create(RestResponse.Status.BAD_REQUEST)
                    .entity(importResult.getErrorMessage())
                    .build();
        } else {
            return RestResponse.ResponseBuilder
                    .ok()
                    .entity(result)
                    .build();
        }
    }

    @POST
    @Path("/{project}/render-document")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public RestResponse<String> renderDocument(
            @PathParam("project") @NotNull String project,
            @NotNull JsonObject jsonObject
    ) {
        Map<String, Object> headers = Map.of(DocumentRoute.DOCUMENT_PROJECT, project);
        Object result = producerTemplate.requestBodyAndHeaders("direct:render-json", jsonObject, headers, Object.class);

        if (result instanceof DocumentImportResult importResult) {
            return RestResponse.ResponseBuilder
                    .<String>create(RestResponse.Status.BAD_REQUEST)
                    .entity(importResult.getErrorMessage())
                    .build();
        } else if (result instanceof String xmlString) {
            return RestResponse.ResponseBuilder
                    .<String>ok()
                    .entity(xmlString)
                    .build();
        } else {
           throw new IllegalStateException("Unexpected result");
        }
    }

    @GET
    @Path("/{project}/documents/{documentId}")
    public RestResponse<DocumentDto> getDocument(
            @PathParam("project") @NotNull String project,
            @PathParam("documentId") @NotNull Long documentId
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(project, documentId);
        if (documentEntity == null) {
            return documentDtoNotFoundResponse.get();
        }

        DocumentDto dto = documentMapper.toDto(documentEntity);
        return documentDtoSuccessResponse.apply(dto);
    }

    @GET
    @Path("/{project}/documents/{documentId}/xml")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentXMLFile(
            @PathParam("project") @NotNull String project,
            @PathParam("documentId") @NotNull Long documentId,
            @QueryParam("unzip") @DefaultValue("true") boolean unzip
    ) {
        String mediaType = !unzip ? "application/zip" : MediaType.APPLICATION_XML;
        String fileExtension = !unzip ? ".zip" : ".xml";

        UBLDocumentEntity documentEntity = documentRepository.findById(project, documentId);
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
    @Path("/{project}/documents/{documentId}/cdr")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentCdrFile(
            @PathParam("project") @NotNull String project,
            @PathParam("documentId") @NotNull Long documentId,
            @QueryParam("unzip") @DefaultValue("true") boolean unzip
    ) {
        String mediaType = !unzip ? "application/zip" : MediaType.APPLICATION_XML;
        String fileExtension = !unzip ? ".zip" : ".xml";

        UBLDocumentEntity documentEntity = documentRepository.findById(project, documentId);
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
    @Path("/{project}/documents/{documentId}/print")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentCdrFile(
            @PathParam("project") @NotNull String project,
            @PathParam("documentId") @NotNull Long documentId
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(project, documentId);
        if (documentEntity == null || documentEntity.getXmlFileId() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String xmlFileId = documentEntity.getXmlFileId();
        byte[] xmlBytes = filesManager.getFileAsBytesAfterUnzip(xmlFileId);

        String documentType;
        String ruc;
        String serieNumero;
        if (documentEntity.getXmlData() != null) {
            documentType = documentEntity.getXmlData().getTipoDocumento();
            ruc = documentEntity.getXmlData().getRuc();
            serieNumero = documentEntity.getXmlData().getSerieNumero();
        } else {
            try {
                XmlContent xmlContent = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(xmlBytes));
                documentType = xmlContent.getDocumentType();
                ruc = xmlContent.getRuc();
                serieNumero = xmlContent.getDocumentID();
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new RuntimeException(e);
            }
        }

        // Read XML
        Object inputData;
        try {
            InputStream targetStream = new ByteArrayInputStream(xmlBytes);
            switch (documentType) {
                case "Invoice" -> {
                    InvoiceMapper mapper = Mappers.getMapper(InvoiceMapper.class);
                    XMLInvoice pojo = (XMLInvoice) JAXBContext.newInstance(XMLInvoice.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "CreditNote" -> {
                    CreditNoteMapper mapper = Mappers.getMapper(CreditNoteMapper.class);
                    XMLCreditNote pojo = (XMLCreditNote) JAXBContext.newInstance(XMLCreditNote.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "DebitNote" -> {
                    DebitNoteMapper mapper = Mappers.getMapper(DebitNoteMapper.class);
                    XMLDebitNote pojo = (XMLDebitNote) JAXBContext.newInstance(XMLDebitNote.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "VoidedDocuments" -> {
                    VoidedDocumentsMapper mapper = Mappers.getMapper(VoidedDocumentsMapper.class);
                    XMLVoidedDocuments pojo = (XMLVoidedDocuments) JAXBContext.newInstance(XMLVoidedDocuments.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "SummaryDocuments" -> {
                    SummaryDocumentsMapper mapper = Mappers.getMapper(SummaryDocumentsMapper.class);
                    XMLSummaryDocuments pojo = (XMLSummaryDocuments) JAXBContext.newInstance(XMLSummaryDocuments.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "Perception" -> {
                    PerceptionMapper mapper = Mappers.getMapper(PerceptionMapper.class);
                    XMLPercepcion pojo = (XMLPercepcion) JAXBContext.newInstance(XMLPercepcion.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "Retention" -> {
                    RetentionMapper mapper = Mappers.getMapper(RetentionMapper.class);
                    XMLRetention pojo = (XMLRetention) JAXBContext.newInstance(XMLRetention.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "DespatchAdvice" -> {
                    DespatchAdviceMapper mapper = Mappers.getMapper(DespatchAdviceMapper.class);
                    XMLDespatchAdvice pojo = (XMLDespatchAdvice) JAXBContext.newInstance(XMLDespatchAdvice.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                default -> {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

        // Search template
        Template template = null;
        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity != null) {
            ComponentOwner companyOwner = ComponentOwner.builder()
                    .project(project)
                    .ruc(ruc)
                    .build();
            String templateName = DbTemplateLocator.encodeTemplateName(companyOwner, TemplateType.PRINT.name(), documentType);
            template = engine.getTemplate(templateName);
        }
        if (template == null) {
            ComponentOwner projectOwner = ComponentOwner.builder()
                    .project(project)
                    .build();
            String templateName = DbTemplateLocator.encodeTemplateName(projectOwner, TemplateType.PRINT.name(), documentType);
            template = engine.getTemplate(templateName);
        }
        if (template == null) {
            String templateName = "itext/" + documentType + ".html";
            template = engine.getTemplate(templateName);
        }
        if (template == null) {
            throw new NotFoundException("Could not find a valid template for the document");
        }

        // Get logo
        String logoBase64 = null;
        if (companyEntity != null && companyEntity.getLogoFileId() != null) {
            byte[] logoBytes = filesManager.getFileAsBytesAfterUnzip(companyEntity.getLogoFileId());
            logoBase64 = Base64.getEncoder().withoutPadding().encodeToString(logoBytes);
        }

        // Render HTML
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("logo", logoBase64);

        String htmlDocument = template
                .data(
                        "input", inputData,
                        "metadata", metadata
                )
                .render();

        // Generate response
        ConverterProperties converterProperties = new ConverterProperties();
        try {
            String s = Thread.currentThread().getContextClassLoader().getResource("templates/itext/").toURI().toString();
            converterProperties.setBaseUri(s);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlDocument, output, converterProperties);

        String filename = serieNumero + ".pdf";
        return Response.ok(output.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .build();
    }

    @GET
    @Path("/{project}/documents")
    public RestResponse<PageDto<DocumentDto>> getDocuments(
            @PathParam("project") @NotNull String project,
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

        ProjectEntity projectEntity = projectRepository.findById(project);
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



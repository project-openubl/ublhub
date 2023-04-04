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

import com.itextpdf.html2pdf.HtmlConverter;
import io.github.project.openubl.ublhub.files.FilesManager;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.qute.DbTemplateLocator;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.CreditNoteMapper;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.DebitNoteMapper;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.DespatchAdviceMapper;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.InvoiceMapper;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.PerceptionMapper;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.RetentionMapper;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.SummaryDocumentsMapper;
import io.github.project.openubl.xbuilder.content.jaxb.mappers.VoidedDocumentsMapper;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLCreditNote;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLDebitNote;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLDespatchAdvice;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLInvoice;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLPercepcion;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLRetention;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLSummaryDocuments;
import io.github.project.openubl.xbuilder.content.jaxb.models.XMLVoidedDocuments;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import org.mapstruct.factory.Mappers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.company;
import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.project;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class DocumentFileResource {

    @Inject
    Engine engine;

    @Inject
    FilesManager filesManager;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    Unmarshaller unmarshaller;

    @GET
    @Path("/{projectId}/document-files/{documentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getDocumentFile(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("documentId") @NotNull Long documentId,
            @QueryParam("requestedFile") @DefaultValue("ubl") String requestedFile,
            @QueryParam("requestedFormat") @DefaultValue("zip") String requestedFormat
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(projectId, documentId);
        if (documentEntity == null || documentEntity.getXmlFileId() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String fileId;
        if (requestedFile.equals("ubl")) {
            fileId = documentEntity.getXmlFileId();
        } else {
            fileId = documentEntity.getCdrFileId();
        }

        boolean isZipFormatRequested = requestedFormat.equals("zip");

        byte[] bytes;
        if (isZipFormatRequested) {
            bytes = filesManager.getFileAsBytesWithoutUnzipping(fileId);
        } else {
            bytes = filesManager.getFileAsBytesAfterUnzip(fileId);
        }

        String filename = documentEntity.getXmlData().getSerieNumero() + (isZipFormatRequested ? ".zip" : ".xml");
        String mediaType = isZipFormatRequested ? "application/zip" : MediaType.APPLICATION_XML;

        return Response.ok(bytes, mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .build();
    }

    @GET
    @Path("/{projectId}/printed-document/{documentId}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
    public Response getPrintedDocumentFile(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("documentId") @NotNull Long documentId,
            @QueryParam("requestedFormat") @DefaultValue("pdf") String requestedFormat
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(projectId, documentId);
        if (documentEntity == null || documentEntity.getXmlFileId() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        org.apache.cxf.ws.addressing.EndpointReferenceType a;
        javax.xml.ws.wsaddressing.W3CEndpointReference b;

        String fileId = documentEntity.getXmlFileId();
        byte[] xmlBytes = filesManager.getFileAsBytesAfterUnzip(fileId);

        // Read XML
        Object inputData;
        try {
            InputStream targetStream = new ByteArrayInputStream(xmlBytes);
            switch (documentEntity.getXmlData().getTipoDocumento()) {
                case "Invoice" -> {
                    InvoiceMapper mapper = Mappers.getMapper(InvoiceMapper.class);
//                    XMLInvoice pojo = (XMLInvoice) unmarshaller.unmarshal(targetStream);
                    XMLInvoice pojo = (XMLInvoice) JAXBContext.newInstance(XMLInvoice.class)
                            .createUnmarshaller()
                            .unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "CreditNote" -> {
                    CreditNoteMapper mapper = Mappers.getMapper(CreditNoteMapper.class);
                    XMLCreditNote pojo = (XMLCreditNote) unmarshaller.unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "DebitNote" -> {
                    DebitNoteMapper mapper = Mappers.getMapper(DebitNoteMapper.class);
                    XMLDebitNote pojo = (XMLDebitNote) unmarshaller.unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "VoidedDocuments" -> {
                    VoidedDocumentsMapper mapper = Mappers.getMapper(VoidedDocumentsMapper.class);
                    XMLVoidedDocuments pojo = (XMLVoidedDocuments) unmarshaller.unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "SummaryDocuments" -> {
                    SummaryDocumentsMapper mapper = Mappers.getMapper(SummaryDocumentsMapper.class);
                    XMLSummaryDocuments pojo = (XMLSummaryDocuments) unmarshaller.unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "Perception" -> {
                    PerceptionMapper mapper = Mappers.getMapper(PerceptionMapper.class);
                    XMLPercepcion pojo = (XMLPercepcion) unmarshaller.unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "Retention" -> {
                    RetentionMapper mapper = Mappers.getMapper(RetentionMapper.class);
                    XMLRetention pojo = (XMLRetention) unmarshaller.unmarshal(targetStream);
                    inputData = mapper.map(pojo);
                }
                case "DespatchAdvice" -> {
                    DespatchAdviceMapper mapper = Mappers.getMapper(DespatchAdviceMapper.class);
                    XMLDespatchAdvice pojo = (XMLDespatchAdvice) unmarshaller.unmarshal(targetStream);
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
        CompanyEntity companyEntity = companyRepository.findByRuc(projectId, documentEntity.getXmlData().getRuc());
        if (companyEntity != null) {
            ComponentOwner companyOwner = ComponentOwner.builder()
                    .id(companyEntity.getId())
                    .type(company)
                    .build();
            String templateName = DbTemplateLocator.encodeTemplateName(companyOwner, "pdf", documentEntity.getXmlData().getTipoDocumento());
            template = engine.getTemplate(templateName);
        }
        if (template == null) {
            ComponentOwner projectOwner = ComponentOwner.builder()
                    .id(projectId)
                    .type(project)
                    .build();
            String templateName = DbTemplateLocator.encodeTemplateName(projectOwner, "pdf", documentEntity.getXmlData().getTipoDocumento());
            template = engine.getTemplate(templateName);
        }
        if (template == null) {
            String templateName = "itext/" + documentEntity.getXmlData().getTipoDocumento() + ".html";
            template = engine.getTemplate(templateName);
        }
        if (template == null) {
            throw new NotFoundException("Could not find a valid template for the document");
        }

        // Render HTML
        String htmlDocument = template.data(
                "input", inputData,
                "metadata", Map.of("logo", "hello")
        ).render();

        // Generate response
        boolean isPdfRequired = requestedFormat.equals("pdf");
        if (isPdfRequired) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlDocument, output);

            String filename = documentEntity.getXmlData().getSerieNumero() + "pdf";
            return Response.ok(output.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .build();
        } else {
            String filename = documentEntity.getXmlData().getSerieNumero() + ".html";
            return Response.ok(htmlDocument, MediaType.TEXT_HTML)
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .build();
        }
    }

}



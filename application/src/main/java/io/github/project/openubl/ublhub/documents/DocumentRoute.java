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
package io.github.project.openubl.ublhub.documents;

import io.github.project.openubl.ublhub.documents.exceptions.NoCertificateToSignFoundException;
import io.github.project.openubl.ublhub.documents.exceptions.NoUBLXMLFileCompliantException;
import io.github.project.openubl.ublhub.documents.exceptions.ProjectNotFoundException;
import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;
import io.github.project.openubl.xbuilder.content.models.standard.general.CreditNote;
import io.github.project.openubl.xbuilder.content.models.standard.general.DebitNote;
import io.github.project.openubl.xbuilder.content.models.standard.general.Invoice;
import io.github.project.openubl.xbuilder.content.models.standard.guia.DespatchAdvice;
import io.github.project.openubl.xbuilder.content.models.sunat.baja.VoidedDocuments;
import io.github.project.openubl.xbuilder.content.models.sunat.percepcionretencion.Perception;
import io.github.project.openubl.xbuilder.content.models.sunat.percepcionretencion.Retention;
import io.github.project.openubl.xbuilder.content.models.sunat.resumen.SummaryDocuments;
import io.github.project.openubl.xsender.Constants;
import io.github.project.openubl.xsender.camel.utils.CamelData;
import io.github.project.openubl.xsender.camel.utils.CamelUtils;
import io.github.project.openubl.xsender.company.CompanyCredentials;
import io.github.project.openubl.xsender.company.CompanyURLs;
import io.github.project.openubl.xsender.files.BillServiceFileAnalyzer;
import io.github.project.openubl.xsender.files.BillServiceXMLFileAnalyzer;
import io.github.project.openubl.xsender.files.ZipFile;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import io.github.project.openubl.xsender.models.Sunat;
import io.github.project.openubl.xsender.models.SunatResponse;
import io.github.project.openubl.xsender.sunat.BillServiceDestination;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.builder.Namespaces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.xml.sax.SAXParseException;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import java.util.Optional;

import static io.github.project.openubl.xsender.camel.utils.CamelUtils.getBillServiceCamelData;

@ApplicationScoped
public class DocumentRoute extends RouteBuilder {

    public static final String DOCUMENT_KIND = "kind";
    public static final String DOCUMENT_PROJECT = "project";
    public static final String DOCUMENT_RUC = "ruc";
    public static final String DOCUMENT_XML_DATA = "documentXmlData";
    public static final String DOCUMENT_ID = "documentId";
    public static final String DOCUMENT_FILE = "documentFile";
    public static final String DOCUMENT_FILE_ID = "documentFileId";
    public static final String DOCUMENT_SUNAT_DATA = "documentSunatData";

    public static final String SUNAT_RESPONSE = "sunatResponse";
    public static final String SUNAT_TICKET = "sunatTicket";

    Namespaces ns = new Namespaces("ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2")
            .add("ds", "http://www.w3.org/2000/09/xmldsig#");

    @ConfigProperty(name = "openubl.storage.type")
    String storageType;

    @ConfigProperty(name = "openubl.ublhub.scheduler.type")
    String schedulerType;

    @Override
    public void configure() throws Exception {
        // Requires body=java.json.JsonObject + Optional DOCUMENT_PROJECT
        from("direct:import-json")
                .id("import-json")
                .to("direct:render-json")
                .to("direct:import-xml")
                .setBody(exchange -> DocumentImportResult.builder()
                        .documentId(exchange.getIn().getHeader(DOCUMENT_ID, Long.class))
                        .build()
                );

        from("direct:render-json")
                .id("render-json")
                .to("direct:enrich-json")
                .bean("documentBean", "render");

        from("direct:enrich-json")
                .id("enrich-json")
                // Validate Json
                .marshal().json(JsonLibrary.Jsonb, JsonObject.class)
                .to("json-validator:schemas/DocumentInputDto-schema.json")
                .onException(ValidationException.class)
                    .setBody(exchange -> DocumentImportResult.builder()
                            .errorMessage("JSON does not match the required schema")
                            .build()
                    )
                    .handled(true)
                    .log(LoggingLevel.DEBUG, "File does not match Schema")
                .end()

                .unmarshal().json(JsonLibrary.Jsonb, JsonObject.class)

                // Get Project
                .choice()
                    .when(header(DOCUMENT_PROJECT).isNull())
                        .process(exchange -> {
                            JsonObject json = exchange.getIn().getBody(JsonObject.class);
                            String project = json.getJsonObject("metadata").getString("project");
                            if (project != null) {
                                exchange.getIn().setHeader(DOCUMENT_PROJECT, project);
                            }
                        })
                    .endChoice()
                .end()

                // Extract document spec
                .process(exchange -> {
                    JsonObject json = exchange.getIn().getBody(JsonObject.class);

                    String kind = json.getString(DOCUMENT_KIND);
                    JsonObject document = json.getJsonObject("spec").getJsonObject("document");

                    exchange.getIn().setHeader(DOCUMENT_KIND, kind);
                    exchange.getIn().setBody(document);
                })

                // Unmarshal to XBuilder POJO
                .choice()
                    .when(header(DOCUMENT_KIND).isEqualTo("Invoice"))
                        .marshal().json(JsonLibrary.Jsonb, Invoice.class)
                        .unmarshal().json(JsonLibrary.Jackson, Invoice.class)
                        .process(exchange -> {
                            Invoice input = exchange.getIn().getBody(Invoice.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                    .when(header(DOCUMENT_KIND).isEqualTo("CreditNote"))
                        .marshal().json(JsonLibrary.Jsonb, CreditNote.class)
                        .unmarshal().json(JsonLibrary.Jackson, CreditNote.class)
                        .process(exchange -> {
                            CreditNote input = exchange.getIn().getBody(CreditNote.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                    .when(header(DOCUMENT_KIND).isEqualTo("DebitNote"))
                        .marshal().json(JsonLibrary.Jsonb, DebitNote.class)
                        .unmarshal().json(JsonLibrary.Jackson, DebitNote.class)
                        .process(exchange -> {
                            DebitNote input = exchange.getIn().getBody(DebitNote.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                    .when(header(DOCUMENT_KIND).isEqualTo("VoidedDocuments"))
                        .marshal().json(JsonLibrary.Jsonb, VoidedDocuments.class)
                        .unmarshal().json(JsonLibrary.Jackson, VoidedDocuments.class)
                        .process(exchange -> {
                            VoidedDocuments input = exchange.getIn().getBody(VoidedDocuments.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                    .when(header(DOCUMENT_KIND).isEqualTo("SummaryDocuments"))
                        .marshal().json(JsonLibrary.Jsonb, SummaryDocuments.class)
                        .unmarshal().json(JsonLibrary.Jackson, SummaryDocuments.class)
                        .process(exchange -> {
                            SummaryDocuments input = exchange.getIn().getBody(SummaryDocuments.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                    .when(header(DOCUMENT_KIND).isEqualTo("Perception"))
                        .marshal().json(JsonLibrary.Jsonb, Perception.class)
                        .unmarshal().json(JsonLibrary.Jackson, Perception.class)
                        .process(exchange -> {
                            Perception input = exchange.getIn().getBody(Perception.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                    .when(header(DOCUMENT_KIND).isEqualTo("Retention"))
                        .marshal().json(JsonLibrary.Jsonb, Retention.class)
                        .unmarshal().json(JsonLibrary.Jackson, Retention.class)
                        .process(exchange -> {
                            Retention input = exchange.getIn().getBody(Retention.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                    .when(header(DOCUMENT_KIND).isEqualTo("DespatchAdvice"))
                        .marshal().json(JsonLibrary.Jsonb, DespatchAdvice.class)
                        .unmarshal().json(JsonLibrary.Jackson, DespatchAdvice.class)
                        .process(exchange -> {
                            DespatchAdvice input = exchange.getIn().getBody(DespatchAdvice.class);
                            exchange.getIn().setHeader(DOCUMENT_RUC, input.getProveedor().getRuc());
                        })
                    .endChoice()
                .end()
                .bean("documentBean", "enrich");

        // Requires body=org.w3c.dom.Document, DOCUMENT_PROJECT
        from("direct:import-xml")
                .id("import-xml")
                .bean("documentBean", "validateProject")
                .onException(ProjectNotFoundException.class)
                    .setBody(exchange -> DocumentImportResult.builder()
                            .errorMessage("Project not found")
                            .build()
                    )
                    .handled(true)
                    .log(LoggingLevel.ERROR, "Project ${in.headers.project} not found")
                .end()

                .choice()
                    .when(xpath("count(//ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/ds:Signature)", Integer.class, ns).isEqualTo(0))
                        .choice()
                            .when(header(DOCUMENT_RUC).isNull())
                                .setHeader(DOCUMENT_FILE, body())
                                .bean("documentBean", "generateXmlData")
                                .process(exchange -> {
                                    XmlContent xmlContent = exchange.getIn().getHeader(DOCUMENT_XML_DATA, XmlContent.class);
                                    exchange.getIn().setHeader(DOCUMENT_RUC, xmlContent.getRuc());
                                })
                            .otherwise()
                                .log(LoggingLevel.DEBUG, "Ruc already present")
                        .bean("documentBean", "sign")
                    .endChoice()
                    .otherwise()
                        .log(LoggingLevel.DEBUG, "Document signed already")
                .end()
                .onException(NoUBLXMLFileCompliantException.class)
                    .setBody(exchange -> DocumentImportResult.builder()
                            .errorMessage("No valid UBL XML file")
                            .build()
                    )
                    .handled(true)
                .end()
                .onException(NoCertificateToSignFoundException.class)
                    .setBody(exchange -> DocumentImportResult.builder()
                            .errorMessage("No certificate to sign found")
                            .build()
                    )
                    .handled(true)
                .end()
                .onException(SAXParseException.class)
                    .setBody(exchange -> DocumentImportResult.builder()
                            .errorMessage("XML could not be parsed")
                            .build()
                    )
                    .handled(true)
                .end()

                .setHeader("shouldZipFile", constant(true))
                .enrich("direct:" + storageType + "-save-file", (oldExchange, newExchange) -> {
                    String documentFileId = newExchange.getIn().getBody(String.class);
                    oldExchange.getIn().setHeader(DOCUMENT_FILE_ID, documentFileId);
                    return oldExchange;
                })
                .bean("documentBean", "create")
                .setBody(exchange -> exchange.getIn().getHeader(DOCUMENT_ID, Long.class))
                .to("seda:send-xml?waitForTaskToComplete=Never")
                .setBody(exchange -> DocumentImportResult.builder()
                        .documentId(exchange.getIn().getHeader(DOCUMENT_ID, Long.class))
                        .build()
                );

        // Requires body=DOCUMENT_ID
        from("seda:send-xml")
                .id("send-xml")
                .setHeader(DOCUMENT_ID, body())
                .bean("documentBean", "fetchDocument")

                .setBody(header(DocumentRoute.DOCUMENT_FILE_ID))
                .setHeader("shouldUnzip", constant(true))
                .enrich("direct:" + storageType + "-get-file", (oldExchange, newExchange) -> {
                    oldExchange.getIn().setHeader(DOCUMENT_FILE, newExchange.getIn().getBody());
                    return oldExchange;
                })

                .choice()
                    .when(header(DOCUMENT_XML_DATA).isNull())
                        .bean("documentBean", "generateXmlData")
                    .endChoice()
                .end()
                .bean("documentBean", "saveXmlData")
                .onException(NoUBLXMLFileCompliantException.class)
                    .setBody(exchange -> DocumentImportResult.builder()
                            .errorMessage("No valid UBL XML file")
                            .build()
                    )
                    .handled(true)
                .end()

                .bean("documentBean", "getSunatData")
                .process(exchange -> {
                    byte[] documentFile = exchange.getIn().getHeader(DOCUMENT_FILE, byte[].class);
                    SunatEntity documentSunatData = exchange.getIn().getHeader(DOCUMENT_SUNAT_DATA, SunatEntity.class);

                    CompanyURLs urls = CompanyURLs.builder()
                            .invoice(documentSunatData.getSunatUrlFactura())
                            .perceptionRetention(documentSunatData.getSunatUrlPercepcionRetencion())
                            .despatch(documentSunatData.getSunatUrlGuiaRemision())
                            .build();
                    CompanyCredentials credentials = CompanyCredentials.builder()
                            .username(documentSunatData.getSunatUsername())
                            .password(documentSunatData.getSunatPassword())
                            .build();

                    BillServiceFileAnalyzer fileAnalyzer = new BillServiceXMLFileAnalyzer(documentFile, urls);

                    ZipFile zipFile = fileAnalyzer.getZipFile();
                    BillServiceDestination fileDestination = fileAnalyzer.getSendFileDestination();
                    CamelData camelFileData = getBillServiceCamelData(zipFile, fileDestination, credentials);

                    exchange.getIn().setBody(camelFileData.getBody());
                    camelFileData.getHeaders().forEach((k, v) -> exchange.getIn().setHeader(k, v));
                })
                .to(Constants.XSENDER_BILL_SERVICE_URI)

                .process(exchange -> {
                    SunatResponse sunatResponse = exchange.getIn().getBody(SunatResponse.class);

                    byte[] cdrFile = Optional.ofNullable(sunatResponse)
                            .flatMap(response -> Optional.ofNullable(response.getSunat()))
                            .map(Sunat::getCdr)
                            .orElse(null);
                    String ticket = Optional.ofNullable(sunatResponse)
                            .flatMap(response -> Optional.ofNullable(response.getSunat()))
                            .map(Sunat::getTicket)
                            .orElse(null);

                    exchange.getIn().setHeader(SUNAT_RESPONSE, sunatResponse);
                    exchange.getIn().setHeader(SUNAT_TICKET, ticket);
                    exchange.getIn().setBody(cdrFile);
                })

                .bean("documentBean", "saveSunatResponse")
                .choice()
                    .when(body().isNotNull())
                        .setHeader("shouldZipFile", constant(false))
                        .to("direct:" + storageType + "-save-file")
                        .bean("documentBean", "saveCdr")
                    .endChoice()
                .end()
                .choice()
                    .when(header(SUNAT_TICKET).isNotNull())
                        .setBody(header(DOCUMENT_ID))
                        .to("direct:verify-ticket")
                    .endChoice()
                .end();

        from("direct:verify-ticket")
                .id("verify-ticket")
                .setHeader(DOCUMENT_ID, body())
                .bean("documentBean", "fetchDocument")
                .bean("documentBean", "getSunatData")
                .enrich("direct:" + storageType + "-get-file", (oldExchange, newExchange) -> {
                    oldExchange.getIn().setHeader(DOCUMENT_FILE, newExchange.getIn().getBody());
                    return oldExchange;
                })
                .process(exchange -> {
                    String ticket = exchange.getIn().getHeader(SUNAT_TICKET, String.class);

                    byte[] documentFile = exchange.getIn().getHeader(DOCUMENT_FILE, byte[].class);
                    SunatEntity documentSunatData = exchange.getIn().getHeader(DOCUMENT_SUNAT_DATA, SunatEntity.class);

                    CompanyURLs urls = CompanyURLs.builder()
                            .invoice(documentSunatData.getSunatUrlFactura())
                            .perceptionRetention(documentSunatData.getSunatUrlPercepcionRetencion())
                            .despatch(documentSunatData.getSunatUrlGuiaRemision())
                            .build();
                    CompanyCredentials credentials = CompanyCredentials.builder()
                            .username(documentSunatData.getSunatUsername())
                            .password(documentSunatData.getSunatPassword())
                            .build();

                    BillServiceFileAnalyzer fileAnalyzer = new BillServiceXMLFileAnalyzer(documentFile, urls);

                    BillServiceDestination ticketDestination = fileAnalyzer.getVerifyTicketDestination();
                    CamelData camelTicketData = CamelUtils.getBillServiceCamelData(ticket, ticketDestination, credentials);

                    exchange.getIn().setBody(camelTicketData.getBody());
                    camelTicketData.getHeaders().forEach((k, v) -> exchange.getIn().setHeader(k, v));
                })
                .bean("documentBean", "saveSunatResponse")
                .choice()
                    .when(body().isNotNull())
                        .setHeader("shouldZipFile", constant(false))
                        .to("direct:" + storageType + "-save-file")
                        .bean("documentBean", "saveCdr")
                    .endChoice()
                .end();
    }

}

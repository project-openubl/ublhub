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
package io.github.project.openubl.ublhub.ubl.sender;

import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ConnectToSUNATException;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ReadXMLFileContentException;
import io.github.project.openubl.xsender.Constants;
import io.github.project.openubl.xsender.camel.utils.CamelData;
import io.github.project.openubl.xsender.camel.utils.CamelUtils;
import io.github.project.openubl.xsender.company.CompanyCredentials;
import io.github.project.openubl.xsender.company.CompanyURLs;
import io.github.project.openubl.xsender.files.BillServiceFileAnalyzer;
import io.github.project.openubl.xsender.files.BillServiceXMLFileAnalyzer;
import io.github.project.openubl.xsender.files.ZipFile;
import io.github.project.openubl.xsender.files.exceptions.UnsupportedXMLFileException;
import io.github.project.openubl.xsender.files.xml.DocumentType;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import io.github.project.openubl.xsender.files.xml.XmlContentProvider;
import io.github.project.openubl.xsender.models.Metadata;
import io.github.project.openubl.xsender.models.Status;
import io.github.project.openubl.xsender.models.SunatResponse;
import io.github.project.openubl.xsender.sunat.BillServiceDestination;
import org.apache.camel.ProducerTemplate;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.github.project.openubl.xsender.camel.utils.CamelUtils.getBillServiceCamelData;

@Dependent
public class XMLSenderManager {

    private static final Logger LOGGER = Logger.getLogger(XMLSenderManager.class);

    @Inject
    CompanyRepository companyRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProducerTemplate producerTemplate;

    static final List<String> validDocumentTypes = Arrays.asList(
            DocumentType.INVOICE,
            DocumentType.CREDIT_NOTE,
            DocumentType.DEBIT_NOTE,
            DocumentType.VOIDED_DOCUMENT,
            DocumentType.SUMMARY_DOCUMENT,
            DocumentType.PERCEPTION,
            DocumentType.RETENTION,
            DocumentType.DESPATCH_ADVICE
    );

    public XmlContent getXMLContent(byte[] file) throws ReadXMLFileContentException {
        if (file == null) {
            throw new ReadXMLFileContentException("Null files can not be read");
        }

        try {
            XmlContent content = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));
            boolean isValidDocumentType = validDocumentTypes.stream().anyMatch(s -> s.equals(content.getDocumentType()));
            if (isValidDocumentType) {
                return content;
            } else {
                throw new ReadXMLFileContentException("Invalid document type=" + content.getDocumentType());
            }
        } catch (Throwable e) {
            LOGGER.error(e);
            throw new ReadXMLFileContentException(e);
        }
    }

    public XMLSenderConfig getXSenderConfig(Long projectId, String ruc) {
        CompanyEntity companyEntity = companyRepository.findByRuc(projectId, ruc);

        SunatEntity sunatEntity = null;
        if (companyEntity != null) {
            sunatEntity = companyEntity.getSunat();
        }
        if (sunatEntity == null) {
            sunatEntity = projectRepository.findById(projectId).getSunat();
        }

        return XMLSenderConfig.builder()
                .facturaUrl(sunatEntity.getSunatUrlFactura())
                .guiaRemisionUrl(sunatEntity.getSunatUrlGuiaRemision())
                .percepcionRetencionUrl(sunatEntity.getSunatUrlPercepcionRetencion())
                .username(sunatEntity.getSunatUsername())
                .password(sunatEntity.getSunatPassword())
                .build();
    }

    public SunatResponse sendToSUNAT(byte[] file, XMLSenderConfig wsConfig) throws ConnectToSUNATException {
        CompanyURLs urls = CompanyURLs.builder()
                .invoice(wsConfig.getFacturaUrl())
                .perceptionRetention(wsConfig.getPercepcionRetencionUrl())
                .despatch(wsConfig.getGuiaRemisionUrl())
                .build();
        CompanyCredentials credentials = CompanyCredentials.builder()
                .username(wsConfig.getUsername())
                .password(wsConfig.getPassword())
                .build();

        try {
            BillServiceFileAnalyzer fileAnalyzer = new BillServiceXMLFileAnalyzer(file, urls);

            ZipFile zipFile = fileAnalyzer.getZipFile();
            BillServiceDestination fileDestination = fileAnalyzer.getSendFileDestination();
            CamelData camelFileData = getBillServiceCamelData(zipFile, fileDestination, credentials);

            return producerTemplate
                    .requestBodyAndHeaders(Constants.XSENDER_BILL_SERVICE_URI, camelFileData.getBody(), camelFileData.getHeaders(), SunatResponse.class);
        } catch (ParserConfigurationException | IOException | UnsupportedXMLFileException | SAXException e) {
            return SunatResponse.builder()
                    .status(Status.RECHAZADO)
                    .metadata(Metadata.builder()
                            .description(e.getMessage())
                            .build()
                    )
                    .build();
        } catch (Throwable e) {
            // Should retry
            throw new ConnectToSUNATException("Could not send file");
        }
    }

    public SunatResponse verifyTicketAtSUNAT(
            String ticket,
            XmlContent xmlContent,
            XMLSenderConfig wsConfig
    ) throws ConnectToSUNATException {
        CompanyURLs urls = CompanyURLs.builder()
                .invoice(wsConfig.getFacturaUrl())
                .perceptionRetention(wsConfig.getPercepcionRetencionUrl())
                .despatch(wsConfig.getGuiaRemisionUrl())
                .build();
        CompanyCredentials credentials = CompanyCredentials.builder()
                .username(wsConfig.getUsername())
                .password(wsConfig.getPassword())
                .build();
        try {
            BillServiceDestination ticketDestination = BillServiceXMLFileAnalyzer.getTicketDeliveryTarget(urls, xmlContent).orElseThrow(IllegalStateException::new);
            CamelData camelTicketData = CamelUtils.getBillServiceCamelData(ticket, ticketDestination, credentials);

            return producerTemplate
                    .requestBodyAndHeaders(Constants.XSENDER_BILL_SERVICE_URI, camelTicketData.getBody(), camelTicketData.getHeaders(), SunatResponse.class);
        } catch (Throwable e) {
            throw new ConnectToSUNATException("Could not verify ticket");
        }
    }
}

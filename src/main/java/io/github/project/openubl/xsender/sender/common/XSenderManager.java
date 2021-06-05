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
package io.github.project.openubl.xsender.sender.common;

import io.github.project.openubl.xmlsenderws.webservices.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.ValidationWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.SmartBillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.custom.CustomSmartBillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentProvider;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class XSenderManager {

    private static final Logger LOG = Logger.getLogger(XSenderManager.class);

    static final int MAX_STRING = 250;

    @Inject
    FilesManager filesManager;

    @Inject
    CompanyRepository companyRepository;

    public void validateFileEnrich(UBLDocumentEntity documentEntity, byte[] file) {
        XmlContentModel fileContent;
        boolean isFileValid;
        try {
            fileContent = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));
            isFileValid = DocumentType.valueFromDocumentType(fileContent.getDocumentType()).isPresent();

            documentEntity.setRuc(fileContent.getRuc());
            documentEntity.setDocumentID(fileContent.getDocumentID());
            documentEntity.setDocumentType(fileContent.getDocumentType());
            documentEntity.setVoidedLineDocumentTypeCode(fileContent.getVoidedLineDocumentTypeCode());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            isFileValid = false;
        }

        documentEntity.setFileValid(isFileValid);

        if (!isFileValid) {
            documentEntity.setError(ErrorType.INVALID_FILE);
        }
    }

    public void sendFileEnrich(UBLDocumentEntity documentEntity, byte[] file) throws WSException {
        // Select company
        CompanyEntity companyEntity = companyRepository.findByRuc(documentEntity.getNamespace(), documentEntity.getRuc()).orElse(null);
        if (companyEntity == null) {
            documentEntity.setError(ErrorType.NS_COMPANY_NOT_FOUND);
            return;
        }

        // Send file
        BillServiceModel billServiceModel;
        try {
            XSenderBillServiceConfig billServiceConfig = new XSenderBillServiceConfig(
                    companyEntity.getSunatUrls().getSunatUrlFactura(),
                    companyEntity.getSunatUrls().getSunatUrlGuiaRemision(),
                    companyEntity.getSunatUrls().getSunatUrlPercepcionRetencion()
            );
            SmartBillServiceModel smartBillServiceModel = CustomSmartBillServiceManager.send(
                    file,
                    companyEntity.getSunatCredentials().getSunatUsername(),
                    companyEntity.getSunatCredentials().getSunatPassword(),
                    billServiceConfig
            );

            billServiceModel = smartBillServiceModel.getBillServiceModel();
        } catch (ValidationWebServiceException e) {
            // The error code in the
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            documentEntity.setError(ErrorType.INVALID_FILE);
            LOG.error(e);
            return;
        } catch (Throwable e) {
            throw new WSException(e);
        }

        // Save CDR
        String cdrID = null;
        if (billServiceModel != null && billServiceModel.getCdr() != null) {
            cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);
        }

        documentEntity.setStorageCdr(cdrID);

        // Set billService data
        if (billServiceModel != null) {
            documentEntity.setSunatStatus(billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null);
            documentEntity.setSunatCode(billServiceModel.getCode());
            documentEntity.setSunatDescription(billServiceModel.getDescription());
            documentEntity.setSunatTicket(billServiceModel.getTicket());
            documentEntity.setSunatNotes(billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null);
        }
    }

    public void checkTicket(UBLDocumentEntity documentEntity) throws WSException {
        XmlContentModel xmlContentModel = new XmlContentModel();
        xmlContentModel.setRuc(documentEntity.getRuc());
        xmlContentModel.setDocumentType(documentEntity.getDocumentType());
        xmlContentModel.setDocumentID(documentEntity.getDocumentID());
        xmlContentModel.setVoidedLineDocumentTypeCode(documentEntity.getVoidedLineDocumentTypeCode());

        // Select company
        CompanyEntity companyEntity = companyRepository.findByRuc(documentEntity.getNamespace(), documentEntity.getRuc()).orElse(null);
        if (companyEntity == null) {
            documentEntity.setError(ErrorType.NS_COMPANY_NOT_FOUND);
            return;
        }

        // If there is ticket then verify it
        BillServiceModel billServiceModel;
        try {
            XSenderBillServiceConfig billServiceConfig = new XSenderBillServiceConfig(
                    companyEntity.getSunatUrls().getSunatUrlFactura(),
                    companyEntity.getSunatUrls().getSunatUrlPercepcionRetencion(),
                    companyEntity.getSunatUrls().getSunatUrlGuiaRemision()
            );

            billServiceModel = CustomSmartBillServiceManager.getStatus(
                    documentEntity.getSunatTicket(),
                    xmlContentModel,
                    companyEntity.getSunatCredentials().getSunatUsername(),
                    companyEntity.getSunatCredentials().getSunatPassword(),
                    billServiceConfig
            );
        } catch (ValidationWebServiceException e) {
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
        }  catch (Throwable e) {
            throw new WSException(e);
        }

        // Save CDR
        String cdrID = null;
        if (billServiceModel != null && billServiceModel.getCdr() != null) {
            cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);
        }

        documentEntity.setStorageCdr(cdrID);

        // Set billService data
        if (billServiceModel != null) {
            documentEntity.setSunatStatus(billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null);
            documentEntity.setSunatCode(billServiceModel.getCode());
            documentEntity.setSunatDescription(billServiceModel.getDescription());
            documentEntity.setSunatTicket(billServiceModel.getTicket());
            documentEntity.setSunatNotes(billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null);
        }
    }

}

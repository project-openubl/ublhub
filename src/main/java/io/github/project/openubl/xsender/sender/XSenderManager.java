package io.github.project.openubl.xsender.sender;

import io.github.project.openubl.xmlsenderws.webservices.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnknownWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.ValidationWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.SmartBillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.custom.CustomSmartBillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentProvider;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class XSenderManager {

    private static final Logger LOG = Logger.getLogger(XSenderManager.class);

    static final int MAX_STRING = 250;

    public static final String INVALID_FILE_MSG = "Documento no soportado";
    public static final String RUC_IN_COMPANY_NOT_FOUND = "No se pudo encontrar una empresa para el RUC especificado";

    @Inject
    FilesManager filesManager;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    public boolean validateFile(String id) {
        UBLDocumentEntity documentEntity = documentRepository.findById(id);

        // Read XML file
        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());

        XmlContentModel fileContent = null;
        boolean isFileValid;
        String fileValidationError = null;
        try {
            fileContent = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));

            Optional<DocumentType> documentTypeOptional = DocumentType.valueFromDocumentType(fileContent.getDocumentType());
            if (documentTypeOptional.isPresent()) {
                isFileValid = true;
            } else {
                isFileValid = false;
                fileValidationError = INVALID_FILE_MSG;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            isFileValid = false;
            fileValidationError = StringUtils.abbreviate(e.getMessage(), MAX_STRING);
        }

        if (fileContent != null) {
            documentEntity.setRuc(fileContent.getRuc());
            documentEntity.setDocumentID(fileContent.getDocumentID());
            documentEntity.setDocumentType(fileContent.getDocumentType());
            documentEntity.setVoidedLineDocumentTypeCode(fileContent.getVoidedLineDocumentTypeCode());
        }

        documentEntity.setFileValid(isFileValid);
        documentEntity.setFileValidationError(fileValidationError);

        if (!isFileValid) {
            documentEntity.setInProgress(false);
            documentEntity.setError(INVALID_FILE_MSG);
            documentRepository.persist(documentEntity);
            return false;
        }

        // Save
        documentRepository.persist(documentEntity);
        return true;
    }

    public Optional<String> sendFile(String id) throws WSNotAvailableException {
        UBLDocumentEntity documentEntity = documentRepository.findById(id);

        // Select company
        CompanyEntity companyEntity = companyRepository.findByRuc(documentEntity.getNamespace(), documentEntity.getRuc()).orElse(null);
        if (companyEntity == null) {
            documentEntity.setInProgress(false);
            documentEntity.setError(RUC_IN_COMPANY_NOT_FOUND);
            documentRepository.persist(documentEntity);

            LOG.warn("Could not find a company with RUC:" + documentEntity.getRuc() + " and can not continue.");
            return Optional.empty();
        }

        // Fetch file
        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());

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
        } catch (UnknownWebServiceException e) {
            throw new WSNotAvailableException();
        } catch (ValidationWebServiceException e) {
            // The error code in the
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            documentEntity.setInProgress(false);
            documentEntity.setError(StringUtils.abbreviate(e.getMessage(), MAX_STRING));
            documentRepository.persist(documentEntity);

            LOG.error(e);
            return Optional.empty();
        }

        // Save CDR
        String cdrID = null;
        if (billServiceModel != null && billServiceModel.getCdr() != null) {
            cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);
        }

        documentEntity.setStorageCdr(cdrID);

        // Set billService data
        String ticket = null;
        if (billServiceModel != null) {
            documentEntity.setSunatStatus(billServiceModel.getStatus() != null ? billServiceModel.getStatus().toString() : null);
            documentEntity.setSunatCode(billServiceModel.getCode());
            documentEntity.setSunatDescription(billServiceModel.getDescription());
            documentEntity.setSunatTicket(billServiceModel.getTicket());
            documentEntity.setSunatNotes(billServiceModel.getNotes() != null ? new HashSet<>(billServiceModel.getNotes()) : null);

            ticket = billServiceModel.getTicket();
        }

        if (ticket == null) {
            documentEntity.setInProgress(false);
            documentRepository.persist(documentEntity);
        }

        return Optional.ofNullable(ticket);
    }

    public void checkTicket(String id) throws WSNotAvailableException {
        UBLDocumentEntity documentEntity = documentRepository.findById(id);

        XmlContentModel xmlContentModel = new XmlContentModel();
        xmlContentModel.setRuc(documentEntity.getRuc());
        xmlContentModel.setDocumentType(documentEntity.getDocumentType());
        xmlContentModel.setDocumentID(documentEntity.getDocumentID());
        xmlContentModel.setVoidedLineDocumentTypeCode(documentEntity.getVoidedLineDocumentTypeCode());

        // Select company
        CompanyEntity companyEntity = companyRepository.findByRuc(documentEntity.getNamespace(), documentEntity.getRuc()).orElse(null);
        if (companyEntity == null) {
            documentEntity.setInProgress(false);
            documentEntity.setError(RUC_IN_COMPANY_NOT_FOUND);
            documentRepository.persist(documentEntity);

            LOG.warn("Could not find a company with RUC:" + documentEntity.getRuc() + " and can not continue.");
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
        } catch (UnknownWebServiceException e) {
            throw new WSNotAvailableException();
        } catch (ValidationWebServiceException e) {
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
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

        documentEntity.setInProgress(false);
        documentRepository.persist(documentEntity);
    }

}

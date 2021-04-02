package io.github.project.openubl.xsender.kafka.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.github.project.openubl.xsender.kafka.idm.BillServiceContentRepresentation;
import io.github.project.openubl.xsender.kafka.idm.UBLDocumentSunatEventRepresentation;
import io.github.project.openubl.xsender.kafka.idm.UBLRetry;
import io.github.project.openubl.xsender.models.AppBillServiceConfig;
import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
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

@ApplicationScoped
public class DocumentEvents {

    private static final Logger LOG = Logger.getLogger(DocumentEvents.class);

    static final int MAX_STRING = 250;

    @Inject
    FilesManager filesManager;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    UBLDocumentRepository ublDocumentRepository;

    private BillServiceContentRepresentation toRepresentation(BillServiceModel model) {
        BillServiceContentRepresentation rep = new BillServiceContentRepresentation();

        rep.setStatus(model.getStatus() != null ? model.getStatus().toString() : null);
        rep.setCode(model.getCode());
        rep.setDescription(model.getDescription());
        rep.setTicket(model.getTicket());
        rep.setNotes(model.getNotes());

        return rep;
    }

    @Incoming("sunat-document")
    @Outgoing("sunat-document-content")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public UBLDocumentSunatEventRepresentation readDocumentContent(KafkaRecord<String, String> record) {
        UBLDocumentSunatEventRepresentation eventRep;

        try {
            String payload = record.getPayload();
            String unescapedPayload = objectMapper.readValue(payload, String.class);
            eventRep = objectMapper.readValue(unescapedPayload, UBLDocumentSunatEventRepresentation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        byte[] file = filesManager.getFileAsBytesAfterUnzip(eventRep.getStorageFile());

        XmlContentModel fileContent = null;
        boolean isFileContentValid;
        String fileContentValidationMessage;
        try {
            fileContent = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));

            Optional<DocumentType> documentTypeOptional = DocumentType.valueFromDocumentType(fileContent.getDocumentType());
            if (documentTypeOptional.isPresent()) {
                isFileContentValid = true;
                fileContentValidationMessage = "Content is valid";
            } else {
                isFileContentValid = false;
                fileContentValidationMessage = "Not supported document:" + fileContent.getDocumentType();
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            isFileContentValid = false;
            fileContentValidationMessage = StringUtils.abbreviate(e.getMessage(), MAX_STRING);
        }

        // Enrich response
        eventRep.setFileContent(fileContent);
        eventRep.setFileContentValid(isFileContentValid);
        eventRep.setFileContentValidationMessage(fileContentValidationMessage);

        return eventRep;
    }

    @Incoming("sunat-document-content-incoming")
    @Outgoing("sunat-document-billservice")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public UBLDocumentSunatEventRepresentation sendDocumentToSunat(KafkaRecord<String, String> record) {
        UBLDocumentSunatEventRepresentation eventRep;

        try {
            String payload = record.getPayload();
            eventRep = objectMapper.readValue(payload, UBLDocumentSunatEventRepresentation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        // If file is not valid do not do anything
        if (!eventRep.getFileContentValid()) {
            return eventRep;
        }

        // If file is valid then send it to SUNAT
        BillServiceModel billServiceModel = null;
        boolean shouldRetry = false;
        String cdrID = null;

        try {
            byte[] file = filesManager.getFileAsBytesAfterUnzip(eventRep.getStorageFile());

            AppBillServiceConfig billServiceConfig = new AppBillServiceConfig(
                    eventRep.getSunatUrlFactura(),
                    eventRep.getSunatUrlPercepcionRetencion(),
                    eventRep.getSunatUrlGuiaRemision()
            );
            SmartBillServiceModel smartBillServiceModel = CustomSmartBillServiceManager.send(
                    file, eventRep.getSunatUsername(), eventRep.getSunatPassword(), billServiceConfig
            );

            billServiceModel = smartBillServiceModel.getBillServiceModel();
            if (billServiceModel.getCdr() != null) {
                cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);
            }
        } catch (UnknownWebServiceException e) {
            // Could not send file due to outage of SUNAT services
            shouldRetry = true;
        } catch (ValidationWebServiceException e) {
            // The error code in the
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            LOG.error(e);
            throw new IllegalStateException(e);
        }

        // Return result
        if (shouldRetry) {
            eventRep.setRetry(UBLRetry.BILL_SERVICE);
        }

        if (billServiceModel != null) {
            BillServiceContentRepresentation billServiceContent = toRepresentation(billServiceModel);
            billServiceContent.setCdrStorageId(cdrID);

            eventRep.setBillServiceContent(billServiceContent);
        }

        return eventRep;
    }

    @Incoming("sunat-document-billservice-incoming")
    @Outgoing("sunat-document-persistence")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public UBLDocumentSunatEventRepresentation checkTicket(KafkaRecord<String, String> record) {
        UBLDocumentSunatEventRepresentation eventRep;

        try {
            String payload = record.getPayload();
            eventRep = objectMapper.readValue(payload, UBLDocumentSunatEventRepresentation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        // If file has no tickets then don't do anything
        if (eventRep.getBillServiceContent() == null || eventRep.getBillServiceContent().getTicket() == null) {
            return eventRep;
        }

        // If there is ticket then verify it
        BillServiceModel billServiceModel = null;
        boolean shouldRetry = false;
        String cdrID = null;

        try {
            AppBillServiceConfig billServiceConfig = new AppBillServiceConfig(
                    eventRep.getSunatUrlFactura(),
                    eventRep.getSunatUrlPercepcionRetencion(),
                    eventRep.getSunatUrlGuiaRemision()
            );

            billServiceModel = CustomSmartBillServiceManager.getStatus(
                    eventRep.getBillServiceContent().getTicket(),
                    eventRep.getFileContent(),
                    eventRep.getSunatUsername(),
                    eventRep.getSunatPassword(),
                    billServiceConfig
            );

            if (billServiceModel.getCdr() != null) {
                cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);
            }
        } catch (UnknownWebServiceException e) {
            shouldRetry = true;
        } catch (ValidationWebServiceException e) {
            billServiceModel = new BillServiceModel();
            billServiceModel.setCode(e.getSUNATErrorCode());
            billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
            billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);
        }

        // Return result
        if (shouldRetry) {
            eventRep.setRetry(UBLRetry.VERIFY_TICKET);
        }

        if (billServiceModel != null) {
            BillServiceContentRepresentation billServiceContent = toRepresentation(billServiceModel);

            billServiceContent.setCdrStorageId(cdrID);
            eventRep.setBillServiceContent(billServiceContent);
        }

        return eventRep;
    }

    @Blocking
    @Transactional
    @Incoming("sunat-document-persistence-incoming")
    public void persistEvent(String payload) {
        UBLDocumentSunatEventRepresentation eventRep;

        try {
//            String payload = record.getPayload();
            eventRep = objectMapper.readValue(payload, UBLDocumentSunatEventRepresentation.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        UBLDocumentEntity ublDocumentEntity = ublDocumentRepository.findById(eventRep.getId());

        // XML File content validation
        ublDocumentEntity.setValid(eventRep.getFileContentValid());
        ublDocumentEntity.setValidationError(eventRep.getFileContentValidationMessage());

        // XML content
        XmlContentModel xmlContentModel = eventRep.getFileContent();
        if (xmlContentModel != null) {
            ublDocumentEntity.setRuc(xmlContentModel.getRuc());
            ublDocumentEntity.setDocumentID(xmlContentModel.getDocumentID());
            ublDocumentEntity.setDocumentType(xmlContentModel.getDocumentType());
            ublDocumentEntity.setVoidedLineDocumentTypeCode(xmlContentModel.getVoidedLineDocumentTypeCode());
        }

        // SUNAT response
        BillServiceContentRepresentation billServiceContent = eventRep.getBillServiceContent();
        if (billServiceContent != null) {
            ublDocumentEntity.setSunatStatus(billServiceContent.getStatus());
            ublDocumentEntity.setSunatCode(billServiceContent.getCode());
            ublDocumentEntity.setSunatDescription(billServiceContent.getDescription());
            ublDocumentEntity.setSunatTicket(billServiceContent.getTicket());
            ublDocumentEntity.setStorageCdr(billServiceContent.getCdrStorageId());
            ublDocumentEntity.setSunatNotes(new HashSet<>(billServiceContent.getNotes()));
        }

        // delivery status

        // Retries

        // Save
        ublDocumentRepository.persist(ublDocumentEntity);
    }
}

package io.github.project.openubl.xsender.kafka;

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
import io.github.project.openubl.xsender.avro.DocumentKafka;
import io.github.project.openubl.xsender.files.FilesManager;
import io.github.project.openubl.xsender.models.AppBillServiceConfig;
import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.github.project.openubl.xsender.models.EventStatusType;
import io.github.project.openubl.xsender.models.FileType;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEventEntity;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DocumentEvents {

    private static final Logger LOG = Logger.getLogger(DocumentEvents.class);

    static final int MAX_STRING = 250;

    @Inject
    @Channel("send-documents")
    Emitter<DocumentKafka> sendDocumentsEmitter;

    @Inject
    @Channel("send-documents1")
    Emitter<DocumentKafka> document5MinutesEmitter;

    @Inject
    @Channel("send-documents2")
    Emitter<DocumentKafka> document1HourEmitter;

    @Inject
    @Channel("send-documents3")
    Emitter<DocumentKafka> document24HourEmitter;

    @Inject
    @Channel("check-ticket")
    Emitter<DocumentKafka> checkDocumentTicketEmitter;

    @Inject
    @Channel("error-ticket")
    Emitter<DocumentKafka> errorDocumentTicketEmitter;

    @Inject
    FilesManager filesManager;

    @Inject
    UBLDocumentRepository ublDocumentRepository;

    @Transactional
    @Blocking
    @Incoming("incoming-read-documents")
    public void readDocuments(DocumentKafka event) {
        UBLDocumentEntity documentEntity = ublDocumentRepository.findById(event.getId());
        UBLDocumentEventEntity.Builder eventBuilder = UBLDocumentEventEntity.Builder.anUBLDocumentEventEntity()
                .withId(UUID.randomUUID().toString())
                .withUblDocument(documentEntity)
                .withCreatedOn(new Date());

        boolean sendToSunat = false;

        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());

        XmlContentModel xmlContentModel;
        try {
            xmlContentModel = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));

            documentEntity.setDocumentType(xmlContentModel.getDocumentType());
            documentEntity.setDocumentID(xmlContentModel.getDocumentID());
            documentEntity.setRuc(xmlContentModel.getRuc());
            documentEntity.setVoidedLineDocumentTypeCode(xmlContentModel.getVoidedLineDocumentTypeCode());

            Optional<DocumentType> documentTypeOptional = DocumentType.valueFromDocumentType(xmlContentModel.getDocumentType());
            if (documentTypeOptional.isEmpty()) {
                documentEntity.setValid(false);
                documentEntity.setValidationError("Not supported document");
                documentEntity.setDeliveryStatus(DeliveryStatusType.CANCELLED_BY_SYSTEM);

                // Event
                eventBuilder.withStatus(EventStatusType.danger)
                        .withDescription("DocumentType not supported: " + xmlContentModel.getDocumentType());
            } else {
                documentEntity.setValid(true);

                // Event
                eventBuilder.withStatus(EventStatusType.info)
                        .withDescription("Read document content successfully");

                sendToSunat = true;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            documentEntity.setValid(false);
            documentEntity.setValidationError(StringUtils.abbreviate(e.getMessage(), MAX_STRING));
            documentEntity.setDeliveryStatus(DeliveryStatusType.CANCELLED_BY_SYSTEM);

            // Event
            eventBuilder.withStatus(EventStatusType.danger)
                    .withDescription(StringUtils.abbreviate(e.getMessage(), MAX_STRING));
        }

        UBLDocumentEventEntity documentEvent = eventBuilder.build();
        documentEntity.persist();
        documentEvent.persist();

        if (sendToSunat) {
            sendDocumentsEmitter.send(event);
        }
    }

    @Transactional
    @Blocking
    @Incoming("incoming-send-documents")
    public void sendDocuments(DocumentKafka event) {
        try {
            processDocument(event);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            LOG.error("Exception was not expected in this step");
        }
    }

    @Transactional
    @Blocking
    @Incoming("incoming-send-documents1")
    public void sendDocuments1(DocumentKafka event) {
        try {
            processDocument(event);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            LOG.error("Exception was not expected in this step");
        }
    }

    @Transactional
    @Blocking
    @Incoming("incoming-send-documents2")
    public void sendDocuments2(DocumentKafka event) {
        try {
            processDocument(event);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            LOG.error("Exception was not expected in this step");
        }
    }

    @Transactional
    @Blocking
    @Incoming("incoming-send-documents3")
    public void sendDocuments3(DocumentKafka event) {
        try {
            processDocument(event);
        } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
            LOG.error("Exception was not expected in this step");
        }
    }

    @Transactional
    @Blocking
    @Incoming("incoming-check-ticket")
    public void checkTicket(DocumentKafka event) {
        UBLDocumentEntity documentEntity = ublDocumentRepository.findById(event.getId());
        UBLDocumentEventEntity.Builder eventBuilder = UBLDocumentEventEntity.Builder.anUBLDocumentEventEntity()
                .withId(UUID.randomUUID().toString())
                .withUblDocument(documentEntity)
                .withCreatedOn(new Date());

        SunatCredentialsEntity credentials = documentEntity.getCompany().getSunatCredentials();
        SunatUrlsEntity sunatUrls = documentEntity.getCompany().getSunatUrls();

        XmlContentModel xmlContentModel = XmlContentModel.Builder.aXmlContentModel()
                .withDocumentType(documentEntity.getDocumentType())
                .withDocumentID(documentEntity.getDocumentID())
                .withRuc(documentEntity.getRuc())
                .withVoidedLineDocumentTypeCode(documentEntity.getVoidedLineDocumentTypeCode())
                .build();

        try {
            AppBillServiceConfig billServiceConfig = new AppBillServiceConfig(sunatUrls.getSunatUrlFactura(), sunatUrls.getSunatUrlPercepcionRetencion(), sunatUrls.getSunatUrlGuiaRemision());

            BillServiceModel billServiceModel = CustomSmartBillServiceManager.getStatus(documentEntity.getSunatTicket(), xmlContentModel, credentials.getSunatUsername(), credentials.getSunatPassword(), billServiceConfig);
            enrichWithBillServiceModel(billServiceModel, documentEntity);

            // Event
            eventBuilder.withStatus(EventStatusType.success)
                    .withDescription("Ticket verified successfully");
        } catch (UnknownWebServiceException e) {
            // Event
            eventBuilder.withStatus(EventStatusType.danger)
                    .withDescription(StringUtils.abbreviate(e.getMessage(), MAX_STRING));

            errorDocumentTicketEmitter.send(event);
        } catch (ValidationWebServiceException e) {
            // Event
            eventBuilder.withStatus(EventStatusType.danger)
                    .withDescription(StringUtils.abbreviate(e.getMessage(), MAX_STRING));

            // Set error
            handleValidationWebServiceException(e, documentEntity);
        }

        UBLDocumentEventEntity documentEvent = eventBuilder.build();

        documentEntity.persist();
        documentEvent.persist();
    }

    private void processDocument(DocumentKafka event) throws InvalidXMLFileException, UnsupportedDocumentTypeException {
        UBLDocumentEntity documentEntity = ublDocumentRepository.findById(event.getId());
        UBLDocumentEventEntity.Builder eventBuilder = UBLDocumentEventEntity.Builder.anUBLDocumentEventEntity()
                .withId(UUID.randomUUID().toString())
                .withUblDocument(documentEntity)
                .withCreatedOn(new Date());

        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());
        SunatCredentialsEntity credentials = documentEntity.getCompany().getSunatCredentials();
        SunatUrlsEntity sunatUrls = documentEntity.getCompany().getSunatUrls();

        DocumentKafka retryEvent = null;
        Emitter<DocumentKafka> retryEmitter = null;

        try {
            AppBillServiceConfig billServiceConfig = new AppBillServiceConfig(sunatUrls.getSunatUrlFactura(), sunatUrls.getSunatUrlPercepcionRetencion(), sunatUrls.getSunatUrlGuiaRemision());

            SmartBillServiceModel smartBillServiceModel = CustomSmartBillServiceManager.send(file, credentials.getSunatUsername(), credentials.getSunatPassword(), billServiceConfig);
            BillServiceModel billServiceModel = smartBillServiceModel.getBillServiceModel();

            // Update documentEntity
            if (billServiceModel.getTicket() != null) {
                documentEntity.setSunatTicket(billServiceModel.getTicket());
                documentEntity.setDeliveryStatus(DeliveryStatusType.NEED_TO_CHECK_TICKET);

                // Schedule check ticket
                checkDocumentTicketEmitter.send(event);
            } else {
                if (billServiceModel.getCdr() != null) {
                    String cdrID = filesManager.createFile(billServiceModel.getCdr(), UUID.randomUUID().toString(), FileType.ZIP);

                    documentEntity.setStorageCdr(cdrID);
                }

                documentEntity.setDeliveryStatus(DeliveryStatusType.DELIVERED);
                enrichWithBillServiceModel(billServiceModel, documentEntity);
            }

            // Event
            eventBuilder.withStatus(EventStatusType.success)
                    .withDescription("Document sent successfully");
        } catch (UnknownWebServiceException e) {
            // Event
            eventBuilder.withStatus(EventStatusType.warning)
                    .withDescription(StringUtils.abbreviate(e.getMessage(), MAX_STRING));

            // Retry
            int retry = event.getRetry() + 1;
            documentEntity.setRetries(retry);

            retryEvent = DocumentKafka.newBuilder()
                    .setId(event.getId())
                    .setRetry(retry)
                    .build();

            Calendar calendar = Calendar.getInstance();
            if (event.getRetry() == 0) {
                calendar.add(Calendar.MINUTE, 5);
                documentEntity.setWillRetryOn(calendar.getTime());

                retryEmitter = document5MinutesEmitter;
            } else if (event.getRetry() == 1) {
                calendar.add(Calendar.HOUR, 1);
                documentEntity.setWillRetryOn(calendar.getTime());

                retryEmitter = document1HourEmitter;
            } else if (event.getRetry() == 2) {
                calendar.add(Calendar.HOUR, 24);
                documentEntity.setWillRetryOn(calendar.getTime());

                retryEmitter = document24HourEmitter;
            } else {
                documentEntity.setDeliveryStatus(DeliveryStatusType.COULD_NOT_BE_DELIVERED);
            }
        } catch (ValidationWebServiceException e) {
            // Event
            eventBuilder.withStatus(EventStatusType.danger)
                    .withDescription(StringUtils.abbreviate(e.getMessage(), MAX_STRING));

            // Set error
            handleValidationWebServiceException(e, documentEntity);
        }

        UBLDocumentEventEntity documentEvent = eventBuilder.build();

        documentEntity.persist();
        documentEvent.persist();

        if (retryEmitter != null && retryEvent != null) {
            retryEmitter.send(retryEvent);
        }
    }

    private void enrichWithBillServiceModel(BillServiceModel billServiceModel, UBLDocumentEntity documentEntity) {
        documentEntity.setSunatCode(billServiceModel.getCode());
        documentEntity.setSunatDescription(billServiceModel.getDescription());
        documentEntity.setSunatStatus(billServiceModel.getStatus().toString());
        documentEntity.setDeliveryStatus(DeliveryStatusType.DELIVERED);
    }

    private void handleValidationWebServiceException(ValidationWebServiceException e, UBLDocumentEntity documentEntity) {
        documentEntity.setSunatCode(e.getSUNATErrorCode());
        documentEntity.setSunatDescription(e.getSUNATErrorMessage(MAX_STRING));
        documentEntity.setSunatStatus(BillServiceModel.Status.RECHAZADO.toString());
        documentEntity.setDeliveryStatus(DeliveryStatusType.DELIVERED);
    }
}

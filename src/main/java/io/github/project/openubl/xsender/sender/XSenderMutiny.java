package io.github.project.openubl.xsender.sender;

import io.github.project.openubl.xmlsenderws.webservices.exceptions.InvalidXMLFileException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.UnsupportedDocumentTypeException;
import io.github.project.openubl.xmlsenderws.webservices.exceptions.ValidationWebServiceException;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.SmartBillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.custom.CustomBillServiceConfig;
import io.github.project.openubl.xmlsenderws.webservices.managers.smart.custom.CustomSmartBillServiceManager;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.DocumentType;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentProvider;
import io.github.project.openubl.xsender.exceptions.DocumentTypeNotSupportedException;
import io.github.project.openubl.xsender.exceptions.NoCompanyWithRucException;
import io.github.project.openubl.xsender.exceptions.ReadFileException;
import io.github.project.openubl.xsender.exceptions.SendFileToSUNATException;
import io.github.project.openubl.xsender.models.ErrorType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@ApplicationScoped
public class XSenderMutiny {

    static final int MAX_STRING = 250;

    @Inject
    CompanyRepository companyRepository;

    public Uni<XmlContentModel> getFileContent(byte[] file) {
        return Uni.createFrom().emitter(uniEmitter -> {
            try {
                XmlContentModel fileContent = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));
                boolean isValidDocumentType = DocumentType.valueFromDocumentType(fileContent.getDocumentType()).isPresent();
                if (isValidDocumentType) {
                    uniEmitter.complete(fileContent);
                } else {
                    uniEmitter.fail(new DocumentTypeNotSupportedException(fileContent.getDocumentType()));
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                uniEmitter.fail(new ReadFileException(e));
            }
        });
    }

    public Uni<XSenderRequiredData> getXSenderRequiredData(NamespaceEntity namespaceEntity, String ruc) {
        return Panache.withTransaction(() -> companyRepository.findByRuc(namespaceEntity, ruc))
                .onItem().ifNotNull().transform(companyEntity -> new XSenderRequiredData(companyEntity.sunatUrls, companyEntity.sunatCredentials))
                .onItem().ifNull().failWith(() -> new NoCompanyWithRucException("No company with ruc found"));
    }

    public Uni<BillServiceModel> sendFile(byte[] file, XSenderRequiredData xSenderRequiredData) {
        return Uni.createFrom().emitter(uniEmitter -> {
            CustomBillServiceConfig billServiceConfig = new CustomBillServiceConfig() {
                @Override
                public String getInvoiceAndNoteDeliveryURL() {
                    return xSenderRequiredData.getUrls().sunatUrlFactura;
                }

                @Override
                public String getPerceptionAndRetentionDeliveryURL() {
                    return xSenderRequiredData.getUrls().sunatUrlPercepcionRetencion;
                }

                @Override
                public String getDespatchAdviceDeliveryURL() {
                    return xSenderRequiredData.getUrls().sunatUrlGuiaRemision;
                }
            };

            try {
                SmartBillServiceModel smartBillServiceModel = CustomSmartBillServiceManager.send(
                        file,
                        xSenderRequiredData.getCredentials().sunatUsername,
                        xSenderRequiredData.getCredentials().sunatPassword,
                        billServiceConfig
                );

                uniEmitter.complete(smartBillServiceModel.getBillServiceModel());
            } catch (ValidationWebServiceException e) {
                // Should not retry
                BillServiceModel billServiceModel = new BillServiceModel();
                billServiceModel.setCode(e.getSUNATErrorCode());
                billServiceModel.setDescription(e.getSUNATErrorMessage(MAX_STRING));
                billServiceModel.setStatus(BillServiceModel.Status.RECHAZADO);

                uniEmitter.complete(billServiceModel);
            } catch (InvalidXMLFileException | UnsupportedDocumentTypeException e) {
                // Should not retry
                uniEmitter.fail(new DocumentTypeNotSupportedException(""));
            } catch (Throwable e) {
                // Should retry
                uniEmitter.fail(new SendFileToSUNATException(e));
            }
        });
    }

    public Uni<BillServiceModel> verifyTicket(String ticket, XmlContentModel xmlContentModel, XSenderRequiredData xSenderRequiredData) {
        return Uni.createFrom().emitter(uniEmitter -> {
            CustomBillServiceConfig billServiceConfig = new CustomBillServiceConfig() {
                @Override
                public String getInvoiceAndNoteDeliveryURL() {
                    return xSenderRequiredData.getUrls().sunatUrlFactura;
                }

                @Override
                public String getPerceptionAndRetentionDeliveryURL() {
                    return xSenderRequiredData.getUrls().sunatUrlPercepcionRetencion;
                }

                @Override
                public String getDespatchAdviceDeliveryURL() {
                    return xSenderRequiredData.getUrls().sunatUrlGuiaRemision;
                }
            };

            try {
                BillServiceModel billServiceModel = CustomSmartBillServiceManager.getStatus(
                        ticket,
                        xmlContentModel,
                        xSenderRequiredData.getCredentials().sunatUsername,
                        xSenderRequiredData.getCredentials().sunatPassword,
                        billServiceConfig
                );

                uniEmitter.complete(billServiceModel);
            } catch (Throwable e) {
                uniEmitter.fail(new SendFileToSUNATException(e));
            }
        });
    }
}

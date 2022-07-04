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
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ConnectToSUNATException;
import io.github.project.openubl.ublhub.ubl.sender.exceptions.ReadXMLFileContentException;
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
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;

@ApplicationScoped
public class XMLSenderManager {

    private static final Logger LOGGER = Logger.getLogger(XMLSenderManager.class);

    static final int MAX_STRING = 250;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    ProjectRepository projectRepository;

    public Uni<XmlContentModel> getXMLContent(byte[] file) {
        return Uni.createFrom().emitter(uniEmitter -> {
            if (file == null) {
                uniEmitter.fail(new ReadXMLFileContentException("Null files can not be read"));
                return;
            }

            try {
                XmlContentModel content = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));
                boolean isValidDocumentType = DocumentType.valueFromDocumentType(content.getDocumentType()).isPresent();
                if (isValidDocumentType) {
                    uniEmitter.complete(content);
                } else {
                    uniEmitter.fail(new ReadXMLFileContentException("Invalid document type=" + content.getDocumentType()));
                }
            } catch (Throwable e) {
                LOGGER.error(e);
                uniEmitter.fail(new ReadXMLFileContentException(e));
            }
        });
    }

    public Uni<XMLSenderConfig> getXSenderConfig(String namespaceId, String ruc) {
        return companyRepository.findByRuc(namespaceId, ruc)
                .onItem().ifNotNull().transform(companyEntity -> companyEntity.sunat)
                .onItem().ifNull().switchTo(() -> projectRepository
                        .findById(namespaceId)
                        .map(namespaceEntity -> namespaceEntity.sunat)
                )
                .map(sunatEntity -> XMLSenderConfigBuilder.aXMLSenderConfig()
                        .withFacturaUrl(sunatEntity.sunatUrlFactura)
                        .withGuiaRemisionUrl(sunatEntity.sunatUrlGuiaRemision)
                        .withPercepcionRetencionUrl(sunatEntity.sunatUrlPercepcionRetencion)
                        .withUsername(sunatEntity.sunatUsername)
                        .withPassword(sunatEntity.sunatPassword)
                        .build()
                );
    }

    public Uni<BillServiceModel> sendToSUNAT(byte[] file, XMLSenderConfig wsConfig) {
        return Uni.createFrom()
                .emitter(uniEmitter -> {
                    CustomBillServiceConfig billServiceConfig = new BillServiceConfig(wsConfig.getFacturaUrl(), wsConfig.getGuiaRemisionUrl(), wsConfig.getPercepcionRetencionUrl());

                    try {
                        SmartBillServiceModel smartBillServiceModel = CustomSmartBillServiceManager.send(file, wsConfig.getUsername(), wsConfig.getPassword(), billServiceConfig);
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
                        BillServiceModel billServiceModel = new BillServiceModel();
                        billServiceModel.setDescription("Invalid or unsupported file");

                        uniEmitter.complete(billServiceModel);
                    } catch (Throwable e) {
                        // Should retry
                        uniEmitter.fail(new ConnectToSUNATException("Could not send file"));
                    }
                });
    }

    public Uni<BillServiceModel> verifyTicketAtSUNAT(
            String ticket,
            XmlContentModel xmlContentModel,
            XMLSenderConfig wsConfig
    ) {
        return Uni.createFrom().emitter(uniEmitter -> {
            CustomBillServiceConfig billServiceConfig = new BillServiceConfig(wsConfig.getFacturaUrl(), wsConfig.getGuiaRemisionUrl(), wsConfig.getPercepcionRetencionUrl());
            try {
                BillServiceModel billServiceModel = CustomSmartBillServiceManager.getStatus(ticket, xmlContentModel, wsConfig.getUsername(), wsConfig.getPassword(), billServiceConfig);
                uniEmitter.complete(billServiceModel);
            } catch (Throwable e) {
                uniEmitter.fail(new ConnectToSUNATException("Could not verify ticket"));
            }
        });
    }
}

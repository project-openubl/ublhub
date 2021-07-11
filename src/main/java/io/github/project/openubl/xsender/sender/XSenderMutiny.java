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
import io.github.project.openubl.xsender.exceptions.NoCompanyWithRucException;
import io.github.project.openubl.xsender.exceptions.ReadFileException;
import io.github.project.openubl.xsender.exceptions.SendFileToSUNATException;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;

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
                    uniEmitter.fail(new ReadFileException(fileContent.getDocumentType()));
                }
            } catch (Throwable e) {
                uniEmitter.fail(new ReadFileException(null));
            }
        });
    }

    public Uni<WsConfigCache> getWsConfig(String namespaceId, String ruc) {
        return Panache.withTransaction(() -> companyRepository.findByRuc(namespaceId, ruc))
                .onItem().ifNotNull().transform(companyEntity -> WsConfigCacheBuilder.aWsConfigCache()
                        .withFacturaUrl(companyEntity.sunatUrls.sunatUrlFactura)
                        .withGuiaUrl(companyEntity.sunatUrls.sunatUrlGuiaRemision)
                        .withPercepcionRetencionUrl(companyEntity.sunatUrls.sunatUrlPercepcionRetencion)
                        .withUsername(companyEntity.sunatCredentials.sunatUsername)
                        .withPassword(companyEntity.sunatCredentials.sunatPassword)
                        .build()
                )
                .onItem().ifNull().failWith(() -> new NoCompanyWithRucException("No company with ruc found"));
    }

    public Uni<BillServiceModel> sendFile(byte[] file, WsConfigCache wsConfig) {
        return Uni.createFrom()
                .<BillServiceModel>emitter(uniEmitter -> {
                    CustomBillServiceConfig billServiceConfig = new CustomBillServiceConfig() {
                        @Override
                        public String getInvoiceAndNoteDeliveryURL() {
                            return wsConfig.getFacturaUrl();
                        }

                        @Override
                        public String getPerceptionAndRetentionDeliveryURL() {
                            return wsConfig.getPercepcionRetencionUrl();
                        }

                        @Override
                        public String getDespatchAdviceDeliveryURL() {
                            return wsConfig.getGuiaUrl();
                        }
                    };

                    try {
                        SmartBillServiceModel smartBillServiceModel = CustomSmartBillServiceManager.send(
                                file,
                                wsConfig.getUsername(),
                                wsConfig.getPassword(),
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
                        uniEmitter.fail(new ReadFileException(null));
                    } catch (Throwable e) {
                        // Should retry
                        uniEmitter.fail(new SendFileToSUNATException("Could not send file"));
                    }
                });
//                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public Uni<BillServiceModel> verifyTicket(
            String ticket,
            XmlContentModel xmlContentModel,
            WsConfigCache wsConfig
    ) {
        return Uni.createFrom().emitter(uniEmitter -> {
            CustomBillServiceConfig billServiceConfig = new CustomBillServiceConfig() {
                @Override
                public String getInvoiceAndNoteDeliveryURL() {
                    return wsConfig.getFacturaUrl();
                }

                @Override
                public String getPerceptionAndRetentionDeliveryURL() {
                    return wsConfig.getPercepcionRetencionUrl();
                }

                @Override
                public String getDespatchAdviceDeliveryURL() {
                    return wsConfig.getGuiaUrl();
                }
            };

            try {
                BillServiceModel billServiceModel = CustomSmartBillServiceManager.getStatus(
                        ticket,
                        xmlContentModel,
                        wsConfig.getUsername(),
                        wsConfig.getPassword(),
                        billServiceConfig
                );

                uniEmitter.complete(billServiceModel);
            } catch (Throwable e) {
                uniEmitter.fail(new SendFileToSUNATException("Could not verify ticket"));
            }
        });
    }
}

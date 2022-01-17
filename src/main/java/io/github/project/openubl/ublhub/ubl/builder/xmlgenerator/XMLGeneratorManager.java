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
package io.github.project.openubl.ublhub.ubl.builder.xmlgenerator;

import io.github.project.openubl.ublhub.idm.input.InputTemplateRepresentation;
import io.github.project.openubl.ublhub.idm.input.KindRepresentation;
import io.github.project.openubl.ublhub.idm.input.SpecRepresentation;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGenerator;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorManager;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import io.github.project.openubl.xmlbuilderlib.clock.SystemClock;
import io.github.project.openubl.xmlbuilderlib.config.Config;
import io.github.project.openubl.xmlbuilderlib.facade.DocumentManager;
import io.github.project.openubl.xmlbuilderlib.facade.DocumentWrapper;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog1;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.SummaryDocumentInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.VoidedDocumentInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.VoidedDocumentLineInputModel;
import io.github.project.openubl.xmlbuilderlib.models.output.standard.invoice.InvoiceOutputModel;
import io.github.project.openubl.xmlbuilderlib.models.output.standard.note.creditNote.CreditNoteOutputModel;
import io.github.project.openubl.xmlbuilderlib.models.output.standard.note.debitNote.DebitNoteOutputModel;
import io.github.project.openubl.xmlbuilderlib.models.output.sunat.SummaryDocumentOutputModel;
import io.github.project.openubl.xmlbuilderlib.models.output.sunat.VoidedDocumentOutputModel;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class XMLGeneratorManager {

    @Inject
    Config xBuilderConfig;

    @Inject
    SystemClock xBuilderSystemClock;

    @Inject
    IDGeneratorManager igGeneratorManager;

    public Uni<String> createXMLString(NamespaceEntity namespace, InputTemplateRepresentation inputTemplate) {
        KindRepresentation kind = inputTemplate.getKind();
        SpecRepresentation spec = inputTemplate.getSpec();
        JsonObject document = spec.getDocument();

        IDGeneratorType idGeneratorType = IDGeneratorType.none;
        if (spec.getIdGenerator() != null) {
            idGeneratorType = spec.getIdGenerator().getName();
        }

        IDGenerator idGenerator = igGeneratorManager.selectIDGenerator(idGeneratorType);
        switch (kind) {
            case Invoice: {
                return Uni.createFrom()
                        .item(document.mapTo(InvoiceInputModel.class))
                        .chain(input -> {
                            boolean isFactura = input.getSerie().toUpperCase().startsWith("F");
                            return idGenerator
                                    .generateInvoiceID(namespace, input.getProveedor().getRuc(), isFactura)
                                    .onItem().ifNotNull().invoke(id -> {
                                        input.setSerie(id.getSerie());
                                        input.setNumero(id.getNumero());
                                    })
                                    .map(id -> input);
                        })
                        .map(invoice -> {
                            DocumentWrapper<InvoiceOutputModel> xbuilderResult = DocumentManager.createXML(invoice, xBuilderConfig, xBuilderSystemClock);
                            return xbuilderResult.getXml();
                        });
            }
            case CreditNote: {
                return Uni.createFrom()
                        .item(document.mapTo(CreditNoteInputModel.class))
                        .chain(input -> {
                            boolean isComprobanteAfectadoFactura = input.getSerieNumeroComprobanteAfectado().toUpperCase().startsWith("F");
                            return idGenerator
                                    .generateCreditNoteID(namespace, input.getProveedor().getRuc(), isComprobanteAfectadoFactura)
                                    .onItem().ifNotNull().invoke(id -> {
                                        input.setSerie(id.getSerie());
                                        input.setNumero(id.getNumero());
                                    })
                                    .map(id -> input);
                        })
                        .map(invoice -> {
                            DocumentWrapper<CreditNoteOutputModel> xbuilderResult = DocumentManager.createXML(invoice, xBuilderConfig, xBuilderSystemClock);
                            return xbuilderResult.getXml();
                        });
            }
            case DebitNote: {
                return Uni.createFrom()
                        .item(document.mapTo(DebitNoteInputModel.class))
                        .chain(input -> {
                            boolean isComprobanteAfectadoFactura = input.getSerieNumeroComprobanteAfectado().toUpperCase().startsWith("F");
                            return idGenerator
                                    .generateDebitNoteID(namespace, input.getProveedor().getRuc(), isComprobanteAfectadoFactura)
                                    .onItem().ifNotNull().invoke(id -> {
                                        input.setSerie(id.getSerie());
                                        input.setNumero(id.getNumero());
                                    })
                                    .map(id -> input);
                        })
                        .map(invoice -> {
                            DocumentWrapper<DebitNoteOutputModel> xbuilderResult = DocumentManager.createXML(invoice, xBuilderConfig, xBuilderSystemClock);
                            return xbuilderResult.getXml();
                        });
            }
            case VoidedDocument: {
                return Uni.createFrom()
                        .item(document.mapTo(VoidedDocumentInputModel.class))
                        .chain(input -> {
                            VoidedDocumentLineInputModel comprobanteAfectado = input.getComprobante();
                            Catalog1 tipoComprobanteAfectado = Catalog.valueOfCode(Catalog1.class, comprobanteAfectado.getTipoComprobante()).orElseThrow(Catalog.invalidCatalogValue);
                            boolean isPercepcionRetencionOrGuia = tipoComprobanteAfectado.equals(Catalog1.PERCEPCION) || tipoComprobanteAfectado.equals(Catalog1.RETENCION) || tipoComprobanteAfectado.equals(Catalog1.GUIA_REMISION_REMITENTE);

                            return idGenerator
                                    .generateVoidedDocumentID(namespace, input.getProveedor().getRuc(), isPercepcionRetencionOrGuia)
                                    .onItem().ifNotNull().invoke(id -> {
//                                        input.setSerie(id.getSerie()); // Not yet supported by XBuilder
                                        input.setNumero(id.getNumero());
                                    })
                                    .map(id -> input);
                        })
                        .map(invoice -> {
                            DocumentWrapper<VoidedDocumentOutputModel> xbuilderResult = DocumentManager.createXML(invoice, xBuilderConfig, xBuilderSystemClock);
                            return xbuilderResult.getXml();
                        });
            }
            case SummaryDocument: {
                return Uni.createFrom()
                        .item(document.mapTo(SummaryDocumentInputModel.class))
                        .chain(input -> {
                            return idGenerator
                                    .generateSummaryDocumentID(namespace, input.getProveedor().getRuc())
                                    .onItem().ifNotNull().invoke(id -> {
//                                        input.setSerie(id.getSerie()); // Not yet supported by XBuilder
                                        input.setNumero(id.getNumero());
                                    })
                                    .map(id -> input);
                        })
                        .map(invoice -> {
                            DocumentWrapper<SummaryDocumentOutputModel> xbuilderResult = DocumentManager.createXML(invoice, xBuilderConfig, xBuilderSystemClock);
                            return xbuilderResult.getXml();
                        });
            }
            default:
                throw new IllegalStateException("Kind:" + kind + " not supported");
        }
    }

}

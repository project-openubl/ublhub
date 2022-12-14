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

import io.github.project.openubl.quarkus.xbuilder.XBuilder;
import io.github.project.openubl.ublhub.dto.DocumentInputDto;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.ID;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGenerator;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorManager;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import io.github.project.openubl.xbuilder.content.models.standard.general.CreditNote;
import io.github.project.openubl.xbuilder.content.models.standard.general.DebitNote;
import io.github.project.openubl.xbuilder.content.models.standard.general.Invoice;
import io.github.project.openubl.xbuilder.enricher.ContentEnricher;
import io.quarkus.qute.Template;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

@ApplicationScoped
public class XMLGeneratorManager {

    @Inject
    XBuilder xBuilder;

    @Inject
    IDGeneratorManager idGeneratorManager;

    public IDGenerator getIDGenerator(DocumentInputDto.ID id) {
        IDGeneratorType type = id != null ? id.getType() : IDGeneratorType.none;
        return idGeneratorManager.selectIDGenerator(type);
    }

    public Object enrichDocument(DocumentInputDto inputDto) {
        DocumentInputDto.Kind kind = inputDto.getKind();
        DocumentInputDto.Spec spec = inputDto.getSpec();
        JsonObject document = spec.getDocument();

        ContentEnricher enricher = new ContentEnricher(xBuilder.getDefaults(), LocalDate::now);

        switch (kind) {
            case Invoice: {
                Invoice invoice = document.mapTo(Invoice.class);
                enricher.enrich(invoice);
                return invoice;
            }
            case CreditNote: {
                CreditNote creditNote = document.mapTo(CreditNote.class);
                enricher.enrich(creditNote);
                return creditNote;
            }
            case DebitNote: {
                DebitNote debitNote = document.mapTo(DebitNote.class);
                enricher.enrich(debitNote);
                return debitNote;
            }
            default:
                throw new IllegalStateException("Document not supported for creating XML");
        }
    }

    public String renderDocument(DocumentInputDto inputDto) {
        DocumentInputDto.Kind kind = inputDto.getKind();
        DocumentInputDto.Spec spec = inputDto.getSpec();
        JsonObject document = spec.getDocument();

        switch (kind) {
            case Invoice: {
                Invoice invoice = document.mapTo(Invoice.class);
                Template template = xBuilder.getTemplate(XBuilder.Type.INVOICE);
                return template.data(invoice).render();
            }
            case CreditNote: {
                CreditNote creditNote = document.mapTo(CreditNote.class);
                Template template = xBuilder.getTemplate(XBuilder.Type.CREDIT_NOTE);
                return template.data(creditNote).render();
            }
            case DebitNote: {
                DebitNote debitNote = document.mapTo(DebitNote.class);
                Template template = xBuilder.getTemplate(XBuilder.Type.DEBIT_NOTE);
                return template.data(debitNote).render();
            }
            default:
                throw new IllegalStateException("Document not supported for creating XML");
        }
    }

    public XMLResult createXMLString(ProjectEntity projectEntity, DocumentInputDto inputDto) {
        DocumentInputDto.Kind kind = inputDto.getKind();
        DocumentInputDto.Spec spec = inputDto.getSpec();
        JsonObject document = spec.getDocument();

        IDGenerator idGenerator = getIDGenerator(spec.getId());
        Map<String, String> idConfig = spec.getId() != null && spec.getId().getConfig() != null ? spec.getId().getConfig() : Collections.emptyMap();

        switch (kind) {
            case Invoice: {
                Invoice invoice = document.mapTo(Invoice.class);
                String xml = getXML(projectEntity, invoice, idGenerator, idConfig);
                return XMLResult.builder()
                        .ruc(invoice.getProveedor().getRuc())
                        .xml(xml)
                        .build();
            }
            case CreditNote: {
                CreditNote creditNote = document.mapTo(CreditNote.class);
                String xml = getXML(projectEntity, creditNote, idGenerator, idConfig);
                return XMLResult.builder()
                        .ruc(creditNote.getProveedor().getRuc())
                        .xml(xml)
                        .build();
            }
            case DebitNote: {
                DebitNote debitNote = document.mapTo(DebitNote.class);
                String xml = getXML(projectEntity, debitNote, idGenerator, idConfig);
                return XMLResult.builder()
                        .ruc(debitNote.getProveedor().getRuc())
                        .xml(xml)
                        .build();
            }
            default:
                throw new IllegalStateException("Document not supported for creating XML");
        }
    }

    private String getXML(ProjectEntity projectEntity, Invoice invoice, IDGenerator idGenerator, Map<String, String> config) {
        ID id = idGenerator.generateInvoiceID(projectEntity, invoice.getProveedor().getRuc(), config);
        if (id != null) {
            invoice.setSerie(id.getSerie());
            invoice.setNumero(id.getNumero());
        }

        ContentEnricher enricher = new ContentEnricher(xBuilder.getDefaults(), LocalDate::now);
        enricher.enrich(invoice);

        Template template = xBuilder.getTemplate(XBuilder.Type.INVOICE);
        return template.data(invoice).render();
    }

    private String getXML(ProjectEntity projectEntity, CreditNote creditNote, IDGenerator idGenerator, Map<String, String> config) {
        boolean isFactura = creditNote.getComprobanteAfectadoSerieNumero().toUpperCase().startsWith("F");
        ID id = idGenerator.generateCreditNoteID(projectEntity, creditNote.getProveedor().getRuc(), isFactura, config);
        if (id != null) {
            creditNote.setSerie(id.getSerie());
            creditNote.setNumero(id.getNumero());
        }

        ContentEnricher enricher = new ContentEnricher(xBuilder.getDefaults(), LocalDate::now);
        enricher.enrich(creditNote);

        Template template = xBuilder.getTemplate(XBuilder.Type.CREDIT_NOTE);
        return template.data(creditNote).render();
    }

    private String getXML(ProjectEntity projectEntity, DebitNote debitNote, IDGenerator idGenerator, Map<String, String> config) {
        boolean isFactura = debitNote.getComprobanteAfectadoSerieNumero().toUpperCase().startsWith("F");
        ID id = idGenerator.generateDebitNoteID(projectEntity, debitNote.getProveedor().getRuc(), isFactura, config);
        if (id != null) {
            debitNote.setSerie(id.getSerie());
            debitNote.setNumero(id.getNumero());
        }

        ContentEnricher enricher = new ContentEnricher(xBuilder.getDefaults(), LocalDate::now);
        enricher.enrich(debitNote);

        Template template = xBuilder.getTemplate(XBuilder.Type.DEBIT_NOTE);
        return template.data(debitNote).render();
    }
}

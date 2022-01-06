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
package io.github.project.openubl.ublhub.builder;

import io.github.project.openubl.ublhub.idgenerator.IDGenerator;
import io.github.project.openubl.ublhub.idgenerator.IDGeneratorType;
import io.github.project.openubl.ublhub.idgenerator.IGGeneratorManager;
import io.github.project.openubl.ublhub.idm.input.InputTemplateRepresentation;
import io.github.project.openubl.ublhub.idm.input.KindRepresentation;
import io.github.project.openubl.ublhub.idm.input.SpecRepresentation;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xmlbuilderlib.clock.SystemClock;
import io.github.project.openubl.xmlbuilderlib.config.Config;
import io.github.project.openubl.xmlbuilderlib.facade.DocumentManager;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.SummaryDocumentInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.VoidedDocumentInputModel;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

@ApplicationScoped
public class XMLBuilderManager {

    @Inject
    Config xBuilderConfig;

    @Inject
    SystemClock xBuilderClock;

    @Inject
    IGGeneratorManager igGeneratorManager;

    public Uni<String> createXMLString(NamespaceEntity namespace, InputTemplateRepresentation inputTemplate, JsonObject document, boolean isPreview) {
        KindRepresentation kind = inputTemplate.getKind();
        SpecRepresentation spec = inputTemplate.getSpec();

        IDGeneratorType idGeneratorType = IDGeneratorType.none;
        Map<String, String> idGeneratorConfig = Collections.emptyMap();
        if (spec.getIdGenerator() != null) {
            idGeneratorType = spec.getIdGenerator().getName();
            idGeneratorConfig = spec.getIdGenerator().getConfig();
        }

        IDGenerator idGenerator = igGeneratorManager.selectIDGenerator(idGeneratorType);
        switch (kind) {
            case Invoice:
                InvoiceInputModel invoice = document.mapTo(InvoiceInputModel.class);

                return idGenerator
                        .enrichWithID(namespace, invoice, idGeneratorConfig, isPreview)
                        .map(input -> DocumentManager.createXML(input, xBuilderConfig, xBuilderClock).getXml());
            case CreditNote:
                CreditNoteInputModel creditNote = document.mapTo(CreditNoteInputModel.class);
                return idGenerator
                        .enrichWithID(namespace, creditNote, idGeneratorConfig, isPreview)
                        .map(input -> DocumentManager.createXML(input, xBuilderConfig, xBuilderClock).getXml());
            case DebitNote:
                DebitNoteInputModel debitNote = document.mapTo(DebitNoteInputModel.class);
                return idGenerator
                        .enrichWithID(namespace, debitNote, idGeneratorConfig, isPreview)
                        .map(input -> DocumentManager.createXML(input, xBuilderConfig, xBuilderClock).getXml());
            case VoidedDocument:
                VoidedDocumentInputModel voidedDocument = document.mapTo(VoidedDocumentInputModel.class);
                return idGenerator
                        .enrichWithID(namespace, voidedDocument, idGeneratorConfig, isPreview)
                        .map(input -> DocumentManager.createXML(input, xBuilderConfig, xBuilderClock).getXml());
            case SummaryDocument:
                SummaryDocumentInputModel summaryDocument = document.mapTo(SummaryDocumentInputModel.class);
                return idGenerator
                        .enrichWithID(namespace, summaryDocument, idGeneratorConfig, isPreview)
                        .map(input -> DocumentManager.createXML(input, xBuilderConfig, xBuilderClock).getXml());
            default:
                throw new IllegalStateException("Kind:" + kind + " not supported");
        }
    }

}

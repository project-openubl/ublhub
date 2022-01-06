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
package io.github.project.openubl.ublhub.idgenerator;

import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.SummaryDocumentInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.VoidedDocumentInputModel;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.smallrye.mutiny.Uni;

import java.util.Map;

public interface IDGenerator {

    Uni<InvoiceInputModel> enrichWithID(NamespaceEntity namespace, InvoiceInputModel invoice, Map<String, String> config, boolean isPreview);

    Uni<CreditNoteInputModel> enrichWithID(NamespaceEntity namespace, CreditNoteInputModel creditNote, Map<String, String> config, boolean isPreview);

    Uni<DebitNoteInputModel> enrichWithID(NamespaceEntity namespace, DebitNoteInputModel debitNote, Map<String, String> config, boolean isPreview);

    Uni<VoidedDocumentInputModel> enrichWithID(NamespaceEntity namespace, VoidedDocumentInputModel voidedDocument, Map<String, String> config, boolean isPreview);

    Uni<SummaryDocumentInputModel> enrichWithID(NamespaceEntity namespace, SummaryDocumentInputModel summaryDocument, Map<String, String> config, boolean isPreview);

}

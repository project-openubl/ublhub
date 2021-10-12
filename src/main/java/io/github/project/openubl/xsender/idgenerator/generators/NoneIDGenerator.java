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
package io.github.project.openubl.xsender.idgenerator.generators;

import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.SummaryDocumentInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.VoidedDocumentInputModel;
import io.github.project.openubl.xsender.idgenerator.IDGenerator;
import io.github.project.openubl.xsender.idgenerator.IDGeneratorProvider;
import io.github.project.openubl.xsender.idgenerator.IDGeneratorType;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
@IDGeneratorProvider(IDGeneratorType.none)
public class NoneIDGenerator implements IDGenerator {

    @Override
    public Uni<InvoiceInputModel> enrichWithID(NamespaceEntity namespace, InvoiceInputModel invoice, Map<String, String> config) {
        // Nothing to do
        return Uni.createFrom().item(invoice);
    }

    @Override
    public Uni<CreditNoteInputModel> enrichWithID(NamespaceEntity namespace, CreditNoteInputModel creditNote, Map<String, String> config) {
        // Nothing to do
        return Uni.createFrom().item(creditNote);
    }

    @Override
    public Uni<DebitNoteInputModel> enrichWithID(NamespaceEntity namespace, DebitNoteInputModel debitNote, Map<String, String> config) {
        // Nothing to do
        return Uni.createFrom().item(debitNote);
    }

    @Override
    public Uni<VoidedDocumentInputModel> enrichWithID(NamespaceEntity namespace, VoidedDocumentInputModel voidedDocument, Map<String, String> config) {
        // Nothing to do
        return Uni.createFrom().item(voidedDocument);
    }

    @Override
    public Uni<SummaryDocumentInputModel> enrichWithID(NamespaceEntity namespace, SummaryDocumentInputModel summaryDocument, Map<String, String> config) {
        // Nothing to do
        return Uni.createFrom().item(summaryDocument);
    }
}

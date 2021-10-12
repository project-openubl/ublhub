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
import io.github.project.openubl.xsender.models.jpa.GeneratedIDRepository;
import io.github.project.openubl.xsender.models.jpa.entities.GeneratedIDEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
@IDGeneratorProvider(IDGeneratorType.generated)
public class GeneratedIDGenerator implements IDGenerator {

    public static final String SERIE = "serie";
    public static final String NUMERO = "numero";

    @Inject
    GeneratedIDRepository generatedIDRepository;

    private Uni<GeneratedIDEntity> generateNextID(NamespaceEntity namespace, String ruc, String documentType, Map<String, String> config) {
        Map<String, String> generatorConfig = Objects.requireNonNullElseGet(config, HashMap::new);

        return generatedIDRepository.getCurrentID(namespace, ruc, documentType)
                .onItem().ifNull().continueWith(() -> generateFirstEntity(namespace, ruc, documentType, generatorConfig))
                .chain(generatedIDEntity -> {
                    if (generatedIDEntity.numero > 99_999_999) {
                        generatedIDEntity.serie++;
                        generatedIDEntity.numero = 1;
                    } else {
                        generatedIDEntity.numero++;
                    }

                    generatedIDEntity.serie = Integer.parseInt(generatorConfig.getOrDefault(SERIE, String.valueOf(generatedIDEntity.serie)));
                    generatedIDEntity.numero = Integer.parseInt(generatorConfig.getOrDefault(NUMERO, String.valueOf(generatedIDEntity.numero)));

                    return generatedIDEntity.persist();
                });
    }

    private GeneratedIDEntity generateFirstEntity(NamespaceEntity namespace, String ruc, String documentType, Map<String, String> config) {
        GeneratedIDEntity entity = new GeneratedIDEntity();

        entity.id = UUID.randomUUID().toString();
        entity.namespace = namespace;
        entity.ruc = ruc;
        entity.documentType = documentType;
        entity.serie = 1;
        entity.numero = 0;

        return entity;
    }

    @Override
    public Uni<InvoiceInputModel> enrichWithID(NamespaceEntity namespace, InvoiceInputModel invoice, Map<String, String> config) {
        return generateNextID(namespace, invoice.getProveedor().getRuc(), "Invoice", config)
                .map(generatedIDEntity -> {
                    invoice.setSerie("F" + StringUtils.leftPad(String.valueOf(generatedIDEntity.serie), 3, "0"));
                    invoice.setNumero(generatedIDEntity.numero);

                    return invoice;
                });
    }

    @Override
    public Uni<CreditNoteInputModel> enrichWithID(NamespaceEntity namespace, CreditNoteInputModel creditNote, Map<String, String> config) {
        String documentType;
        String idPrefix;
        if (creditNote.getSerieNumeroComprobanteAfectado().toUpperCase().startsWith("F")) {
            documentType = "CreditNote_Factura";
            idPrefix = "FC";
        } else {
            documentType = "CreditNote_Boleta";
            idPrefix = "BC";
        }

        return generateNextID(namespace, creditNote.getProveedor().getRuc(), documentType, config)
                .map(generatedIDEntity -> {
                    creditNote.setSerie(idPrefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.serie), 2));
                    creditNote.setNumero(generatedIDEntity.numero);

                    return creditNote;
                });
    }

    @Override
    public Uni<DebitNoteInputModel> enrichWithID(NamespaceEntity namespace, DebitNoteInputModel debitNote, Map<String, String> config) {
        String documentType;
        String idPrefix;
        if (debitNote.getSerieNumeroComprobanteAfectado().toUpperCase().startsWith("F")) {
            documentType = "DebitNote_Factura";
            idPrefix = "FD";
        } else {
            documentType = "DebitNote_Boleta";
            idPrefix = "BD";
        }

        return generateNextID(namespace, debitNote.getProveedor().getRuc(), documentType, config)
                .map(generatedIDEntity -> {
                    debitNote.setSerie(idPrefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.serie), 2));
                    debitNote.setNumero(generatedIDEntity.numero);

                    return debitNote;
                });
    }


    @Override
    public Uni<VoidedDocumentInputModel> enrichWithID(NamespaceEntity namespace, VoidedDocumentInputModel voidedDocument, Map<String, String> config) {
        return Uni.createFrom().item(voidedDocument);
    }

    @Override
    public Uni<SummaryDocumentInputModel> enrichWithID(NamespaceEntity namespace, SummaryDocumentInputModel summaryDocument, Map<String, String> config) {
        return Uni.createFrom().item(summaryDocument);
    }
}

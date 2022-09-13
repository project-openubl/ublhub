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
package io.github.project.openubl.ublhub.ubl.builder.idgenerator.impl;

import io.github.project.openubl.ublhub.models.jpa.GeneratedIDRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.GeneratedIDEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.ID;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGenerator;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorProvider;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
@IDGeneratorProvider(IDGeneratorType.generated)
public class GeneratedIDGenerator implements IDGenerator {

    public static final String SERIE_PROPERTY = "serie";
    public static final String NUMERO_PROPERTY = "numero";

    @ConfigProperty(name = "openubl.xbuilder.timezone")
    String timezone;

    @Inject
    Validator validator;

    @Inject
    GeneratedIDRepository generatedIDRepository;

    public static final String PROP_IS_FACTURA = "isFactura";
    public static final String PROP_MIN_SERIE = "minSerie";
    public static final String PROP_MIN_NUMERO = "minNumero";

    enum DocumentType {
        INVOICE_FACTURA_TYPE("Invoice_Factura", "F"),
        INVOICE_BOLETA_TYPE("Invoice_Boleta", "B"),
        CREDIT_NOTE_FOR_FACTURA_TYPE("CreditNote_Factura", "FC"),
        CREDIT_NOTE_FOR_BOLETA_TYPE("CreditNote_Boleta", "BC"),
        DEBIT_NOTE_FOR_FACTURA_TYPE("DebitNote_Factura", "FD"),
        DEBIT_NOTE_FOR_BOLETA_TYPE("DebitNote_Boleta", "BD"),
        VOIDED_GENERIC_TYPE("VoidedDocument", "RA"),
        VOIDED_DOCUMENT_PERCEPCION_RETENCION_GUIA_TYPE("VoidedDocument", "RR"),
        SUMMARY_DOCUMENT_TYPE("SummaryDocument", "RC");

        private final String name;
        private final String prefix;

        DocumentType(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        public String getName() {
            return name;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    private Uni<GeneratedIDEntity> generateNextID(ProjectEntity projectEntity, String ruc, String documentType, int minSerie, int minNumero) {
        return generatedIDRepository.getCurrentID(projectEntity, ruc, documentType)
                .onItem().ifNull().continueWith(() -> {
                    GeneratedIDEntity entity = new GeneratedIDEntity();

                    entity.setId(UUID.randomUUID().toString());
                    entity.setProjectId(projectEntity.getId());
                    entity.setRuc(ruc);
                    entity.setDocumentType(documentType);
                    entity.setSerie(minSerie);
                    entity.setNumero(minNumero);

                    return entity;
                })
                .chain(generatedIDEntity -> {
                    // Prepare min values
                    if (generatedIDEntity.getSerie() <= minSerie) {
                        generatedIDEntity.setSerie(minSerie);
                        if (generatedIDEntity.getNumero() <= minNumero) {
                            generatedIDEntity.setNumero(minNumero - 1);
                        }
                    }

                    if (generatedIDEntity.getNumero() > 99_999_999) {
                        generatedIDEntity.setSerie(generatedIDEntity.getSerie() + 1);
                        generatedIDEntity.setNumero(1);
                    } else {
                        generatedIDEntity.setNumero(generatedIDEntity.getNumero() + 1);
                    }

                    return generatedIDEntity.persist();
                });
    }

    private Uni<GeneratedIDEntity> generateNextIDVoidedAndSummaryDocument(ProjectEntity project, String ruc, String documentType) {
        return generatedIDRepository.getCurrentID(project, ruc, documentType)
                .onItem().ifNull().continueWith(() -> {
                    GeneratedIDEntity entity = new GeneratedIDEntity();

                    entity.setId(UUID.randomUUID().toString());
                    entity.setProjectId(project.getId());
                    entity.setRuc(ruc);
                    entity.setDocumentType(documentType);
                    entity.setSerie(Integer.parseInt(LocalDateTime
                            .now(ZoneId.of(timezone))
                            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    ));
                    entity.setNumero(0);

                    return entity;
                })
                .chain(generatedIDEntity -> {
                    int yyyyMMdd = Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

                    if (generatedIDEntity.getSerie() == yyyyMMdd) {
                        generatedIDEntity.setNumero(generatedIDEntity.getNumero() + 1);
                    } else {
                        generatedIDEntity.setSerie(yyyyMMdd);
                        generatedIDEntity.setNumero(1);
                    }

                    return generatedIDEntity.persist();
                });
    }

    @Override
    public Uni<ID> generateInvoiceID(ProjectEntity project, String ruc, Map<String, String> config) {
        boolean isFactura = Boolean.parseBoolean(config.getOrDefault(PROP_IS_FACTURA, "true"));
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType;
        if (isFactura) {
            documentType = DocumentType.INVOICE_FACTURA_TYPE;
        } else {
            documentType = DocumentType.INVOICE_BOLETA_TYPE;
        }

        return generateNextID(project, ruc, documentType.name, minSerie, minNumero)
                .map(generatedIDEntity -> {
                    String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 3, "0");
                    int numero = generatedIDEntity.getNumero();
                    return new ID(serie, numero);
                });
    }

    @Override
    public Uni<ID> generateCreditNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config) {
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType;
        if (isFactura) {
            documentType = DocumentType.CREDIT_NOTE_FOR_FACTURA_TYPE;
        } else {
            documentType = DocumentType.CREDIT_NOTE_FOR_BOLETA_TYPE;
        }

        return generateNextID(project, ruc, documentType.name, minSerie, minNumero)
                .map(generatedIDEntity -> {
                    String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 2, "0");
                    int numero = generatedIDEntity.getNumero();
                    return new ID(serie, numero);
                });
    }

    @Override
    public Uni<ID> generateDebitNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config) {
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType;
        if (isFactura) {
            documentType = DocumentType.DEBIT_NOTE_FOR_FACTURA_TYPE;
        } else {
            documentType = DocumentType.DEBIT_NOTE_FOR_BOLETA_TYPE;
        }

        return generateNextID(project, ruc, documentType.name, minSerie, minNumero)
                .map(generatedIDEntity -> {
                    String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 2, "0");
                    int numero = generatedIDEntity.getNumero();
                    return new ID(serie, numero);
                });
    }

    @Override
    public Uni<ID> generateVoidedDocumentID(ProjectEntity project, String ruc, boolean isPercepcionRetencionOrGuia) {
        DocumentType documentType;
        if (isPercepcionRetencionOrGuia) {
            documentType = DocumentType.VOIDED_DOCUMENT_PERCEPCION_RETENCION_GUIA_TYPE;
        } else {
            documentType = DocumentType.VOIDED_GENERIC_TYPE;
        }

        return generateNextIDVoidedAndSummaryDocument(project, ruc, documentType.name)
                .map(generatedIDEntity -> {
                    String serie = documentType.prefix + "-" + generatedIDEntity.getSerie();
                    int numero = generatedIDEntity.getNumero();
                    return new ID(serie, numero);
                });
    }

    @Override
    public Uni<ID> generateSummaryDocumentID(ProjectEntity project, String ruc) {
        DocumentType documentType = DocumentType.SUMMARY_DOCUMENT_TYPE;

        return generateNextIDVoidedAndSummaryDocument(project, ruc, documentType.name)
                .map(generatedIDEntity -> {
                    String serie = documentType.prefix + "-" + generatedIDEntity.getSerie();
                    int numero = generatedIDEntity.getNumero();
                    return new ID(serie, numero);
                });
    }

}

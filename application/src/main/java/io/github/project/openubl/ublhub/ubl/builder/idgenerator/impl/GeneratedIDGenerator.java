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

import com.github.f4b6a3.tsid.TsidFactory;
import io.github.project.openubl.ublhub.models.jpa.GeneratedIDRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.GeneratedIDEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.ID;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGenerator;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorProvider;
import io.github.project.openubl.ublhub.ubl.builder.idgenerator.IDGeneratorType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@ApplicationScoped
@IDGeneratorProvider(IDGeneratorType.generated)
public class GeneratedIDGenerator implements IDGenerator {

    public static final String SERIE_PROPERTY = "serie";
    public static final String NUMERO_PROPERTY = "numero";

    @ConfigProperty(name = "openubl.ublhub.timezone")
    String timezone;

    @Inject
    Validator validator;

    @Inject
    GeneratedIDRepository generatedIDRepository;

    @Inject
    TsidFactory tsidFactory;

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
        SUMMARY_DOCUMENT_TYPE("SummaryDocument", "RC"),
        PERCEPTION("Perception", "P"),
        RETENTION("Retention", "R");

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

    private GeneratedIDEntity generateNextID(ProjectEntity projectEntity, String ruc, String documentType, int minSerie, int minNumero) {
        GeneratedIDEntity entity = generatedIDRepository.getCurrentID(projectEntity, ruc, documentType);
        if (entity == null) {
            entity = new GeneratedIDEntity();

            entity.setId(tsidFactory.create().toLong());
            entity.setProjectId(projectEntity.getId());
            entity.setRuc(ruc);
            entity.setDocumentType(documentType);
            entity.setSerie(minSerie);
            entity.setNumero(minNumero);
        }

        // Prepare min values
        if (entity.getSerie() <= minSerie) {
            entity.setSerie(minSerie);
            if (entity.getNumero() <= minNumero) {
                entity.setNumero(minNumero - 1);
            }
        }

        if (entity.getNumero() > 99_999_999) {
            entity.setSerie(entity.getSerie() + 1);
            entity.setNumero(1);
        } else {
            entity.setNumero(entity.getNumero() + 1);
        }

        entity.persist();
        return entity;
    }

    private GeneratedIDEntity generateNextIDVoidedAndSummaryDocument(ProjectEntity project, String ruc, String documentType) {
        GeneratedIDEntity entity = generatedIDRepository.getCurrentID(project, ruc, documentType);
        if (entity == null) {
            entity = new GeneratedIDEntity();

            entity.setId(tsidFactory.create().toLong());
            entity.setProjectId(project.getId());
            entity.setRuc(ruc);
            entity.setDocumentType(documentType);
            entity.setSerie(Integer.parseInt(LocalDateTime
                    .now(ZoneId.of(timezone))
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            ));
            entity.setNumero(0);
        }

        int yyyyMMdd = Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        if (entity.getSerie() == yyyyMMdd) {
            entity.setNumero(entity.getNumero() + 1);
        } else {
            entity.setSerie(yyyyMMdd);
            entity.setNumero(1);
        }

        entity.persist();
        return entity;
    }

    @Override
    public ID generateInvoiceID(ProjectEntity project, String ruc, Map<String, String> config) {
        boolean isFactura = Boolean.parseBoolean(config.getOrDefault(PROP_IS_FACTURA, "true"));
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType;
        if (isFactura) {
            documentType = DocumentType.INVOICE_FACTURA_TYPE;
        } else {
            documentType = DocumentType.INVOICE_BOLETA_TYPE;
        }

        GeneratedIDEntity generatedIDEntity = generateNextID(project, ruc, documentType.name, minSerie, minNumero);

        String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 3, "0");
        int numero = generatedIDEntity.getNumero();
        return new ID(serie, numero);
    }

    @Override
    public ID generateCreditNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config) {
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType;
        if (isFactura) {
            documentType = DocumentType.CREDIT_NOTE_FOR_FACTURA_TYPE;
        } else {
            documentType = DocumentType.CREDIT_NOTE_FOR_BOLETA_TYPE;
        }

        GeneratedIDEntity generatedIDEntity = generateNextID(project, ruc, documentType.name, minSerie, minNumero);

        String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 2, "0");
        int numero = generatedIDEntity.getNumero();
        return new ID(serie, numero);
    }

    @Override
    public ID generateDebitNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config) {
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType;
        if (isFactura) {
            documentType = DocumentType.DEBIT_NOTE_FOR_FACTURA_TYPE;
        } else {
            documentType = DocumentType.DEBIT_NOTE_FOR_BOLETA_TYPE;
        }

        GeneratedIDEntity generatedIDEntity = generateNextID(project, ruc, documentType.name, minSerie, minNumero);

        String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 2, "0");
        int numero = generatedIDEntity.getNumero();
        return new ID(serie, numero);
    }

    @Override
    public ID generateVoidedDocumentID(ProjectEntity project, String ruc, boolean isPercepcionRetencionOrGuia) {
        DocumentType documentType;
        if (isPercepcionRetencionOrGuia) {
            documentType = DocumentType.VOIDED_DOCUMENT_PERCEPCION_RETENCION_GUIA_TYPE;
        } else {
            documentType = DocumentType.VOIDED_GENERIC_TYPE;
        }

        GeneratedIDEntity generatedIDEntity = generateNextIDVoidedAndSummaryDocument(project, ruc, documentType.name);

        String serie = documentType.prefix + "-" + generatedIDEntity.getSerie();
        int numero = generatedIDEntity.getNumero();
        return new ID(serie, numero);
    }

    @Override
    public ID generateSummaryDocumentID(ProjectEntity project, String ruc) {
        DocumentType documentType = DocumentType.SUMMARY_DOCUMENT_TYPE;

        GeneratedIDEntity generatedIDEntity = generateNextIDVoidedAndSummaryDocument(project, ruc, documentType.name);

        String serie = documentType.prefix + "-" + generatedIDEntity.getSerie();
        int numero = generatedIDEntity.getNumero();
        return new ID(serie, numero);
    }

    @Override
    public ID generatePerceptionID(ProjectEntity project, String ruc, Map<String, String> config) {
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType = DocumentType.PERCEPTION;
        GeneratedIDEntity generatedIDEntity = generateNextID(project, ruc, documentType.name, minSerie, minNumero);

        String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 3, "0");
        int numero = generatedIDEntity.getNumero();
        return new ID(serie, numero);
    }

    @Override
    public ID generateRetentionID(ProjectEntity project, String ruc, Map<String, String> config) {
        int minSerie = Integer.parseInt(config.getOrDefault(PROP_MIN_SERIE, "1"));
        int minNumero = Integer.parseInt(config.getOrDefault(PROP_MIN_NUMERO, "1"));

        DocumentType documentType = DocumentType.RETENTION;
        GeneratedIDEntity generatedIDEntity = generateNextID(project, ruc, documentType.name, minSerie, minNumero);

        String serie = documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.getSerie()), 3, "0");
        int numero = generatedIDEntity.getNumero();
        return new ID(serie, numero);
    }

}

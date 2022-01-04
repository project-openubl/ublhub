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
package io.github.project.openubl.ublhub.idgenerator.generators;

import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.SummaryDocumentInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.sunat.VoidedDocumentInputModel;
import io.github.project.openubl.ublhub.idgenerator.IDGenerator;
import io.github.project.openubl.ublhub.idgenerator.IDGeneratorProvider;
import io.github.project.openubl.ublhub.idgenerator.IDGeneratorType;
import io.github.project.openubl.ublhub.models.jpa.GeneratedIDRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.GeneratedIDEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    enum DocumentType {
        INVOICE_TYPE("Invoice", "F"),
        CREDIT_NOTE_FOR_FACTURA_TYPE("CreditNote_Factura", "FC"),
        CREDIT_NOTE_FOR_BOLETA_TYPE("CreditNote_Boleta", "BC"),
        DEBIT_NOTE_FOR_FACTURA_TYPE("DebitNote_Factura", "FD"),
        DEBIT_NOTE_FOR_BOLETA_TYPE("DebitNote_Boleta", "BD"),
        VOIDED_DOCUMENT_TYPE("VoidedDocument", ""),
        SUMMARY_DOCUMENT_TYPE("SummaryDocument", "");

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

    private Uni<GeneratedIDEntity> generateNextID(NamespaceEntity namespace, String ruc, String documentType, Map<String, String> config, boolean isPreview) {
        Map<String, String> generatorConfig = Objects.requireNonNullElseGet(config, HashMap::new);

        return generatedIDRepository.getCurrentID(namespace, ruc, documentType)
                .onItem().ifNull().continueWith(() -> {
                    GeneratedIDEntity entity = new GeneratedIDEntity();

                    entity.id = UUID.randomUUID().toString();
                    entity.namespace = namespace;
                    entity.ruc = ruc;
                    entity.documentType = documentType;
                    entity.serie = 1;
                    entity.numero = 0;

                    return entity;
                })
                .chain(generatedIDEntity -> {
                    if (generatedIDEntity.numero > 99_999_999) {
                        generatedIDEntity.serie++;
                        generatedIDEntity.numero = 1;
                    } else {
                        generatedIDEntity.numero++;
                    }

                    generatedIDEntity.serie = Integer.parseInt(generatorConfig.getOrDefault(SERIE_PROPERTY, String.valueOf(generatedIDEntity.serie)));
                    generatedIDEntity.numero = Integer.parseInt(generatorConfig.getOrDefault(NUMERO_PROPERTY, String.valueOf(generatedIDEntity.numero)));

                    // If is preview then no need to persist the data
                    if (isPreview) {
                        return Uni.createFrom().item(generatedIDEntity);
                    } else {
                        return generatedIDEntity.persist();
                    }
                });
    }

    private Uni<GeneratedIDEntity> generateNextIDVoidedAndSummaryDocument(NamespaceEntity namespace, String ruc, String documentType, Map<String, String> config, boolean isPreview) {
        return generatedIDRepository.getCurrentID(namespace, ruc, documentType)
                .onItem().ifNull().continueWith(() -> {
                    GeneratedIDEntity entity = new GeneratedIDEntity();

                    entity.id = UUID.randomUUID().toString();
                    entity.namespace = namespace;
                    entity.ruc = ruc;
                    entity.documentType = documentType;
                    entity.serie = Integer.parseInt(LocalDateTime
                            .now(ZoneId.of(timezone))
                            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    );
                    entity.numero = 0;

                    return entity;
                })
                .chain(generatedIDEntity -> {
                    int yyyyMMdd = Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

                    if (generatedIDEntity.serie == yyyyMMdd) {
                        generatedIDEntity.numero++;
                    } else {
                        generatedIDEntity.serie = yyyyMMdd;
                        generatedIDEntity.numero = 1;
                    }

                    // If is preview then no need to persist the data
                    if (isPreview) {
                        return Uni.createFrom().item(generatedIDEntity);
                    } else {
                        return generatedIDEntity.persist();
                    }
                });
    }

    @Override
    public Uni<InvoiceInputModel> enrichWithID(NamespaceEntity namespace, InvoiceInputModel invoice, Map<String, String> config, boolean isPreview) {
        return Uni.createFrom().item(invoice)
                .map(input -> {
                    input.setSerie("F001");
                    input.setNumero(1);
                    return input;
                })
                .chain(input -> Uni.createFrom().<InvoiceInputModel>emitter(uniEmitter -> {
                    Set<ConstraintViolation<InvoiceInputModel>> violations = validator.validate(input);
                    if (violations.isEmpty()) {
                        uniEmitter.complete(input);
                    } else {
                        uniEmitter.fail(new ConstraintViolationException(violations));
                    }
                }))
                .chain(input -> generateNextID(namespace, input.getProveedor().getRuc(), DocumentType.INVOICE_TYPE.name, config, isPreview)
                        .map(generatedIDEntity -> {
                            input.setSerie(DocumentType.INVOICE_TYPE.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.serie), 3, "0"));
                            input.setNumero(generatedIDEntity.numero);
                            return input;
                        })
                );
    }

    @Override
    public Uni<CreditNoteInputModel> enrichWithID(NamespaceEntity namespace, CreditNoteInputModel creditNote, Map<String, String> config, boolean isPreview) {
        return Uni.createFrom().<Void>emitter(uniEmitter -> {
                    if (creditNote.getSerieNumeroComprobanteAfectado() != null &&
                            creditNote.getProveedor() != null &&
                            creditNote.getProveedor().getRuc() != null
                    ) {
                        uniEmitter.complete(null);
                    } else {
                        uniEmitter.fail(new ConstraintViolationException("ProveedorRUC or SerieNumeroComprobanteAfectado invalid", new HashSet<>()));
                    }
                })
                .map(unused -> {
                    if (creditNote.getSerieNumeroComprobanteAfectado().toUpperCase().startsWith("F")) {
                        return DocumentType.CREDIT_NOTE_FOR_FACTURA_TYPE;
                    } else {
                        return DocumentType.CREDIT_NOTE_FOR_BOLETA_TYPE;
                    }
                })
                .chain(documentType -> Uni
                        .createFrom().<Void>emitter(uniEmitter -> {
                            // Set fake serie-numero for verifying hibernate-validator
                            creditNote.setSerie(documentType.prefix + "01");
                            creditNote.setNumero(1);

                            // Validate input
                            Set<ConstraintViolation<CreditNoteInputModel>> violations = validator.validate(creditNote);
                            if (violations.isEmpty()) {
                                uniEmitter.complete(null);
                            } else {
                                uniEmitter.fail(new ConstraintViolationException(violations));
                            }
                        })
                        .chain(unused -> generateNextID(namespace, creditNote.getProveedor().getRuc(), documentType.name, config, isPreview))
                        .map(generatedIDEntity -> {
                            creditNote.setSerie(documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.serie), 2, "0"));
                            creditNote.setNumero(generatedIDEntity.numero);
                            return creditNote;
                        })
                );
    }

    @Override
    public Uni<DebitNoteInputModel> enrichWithID(NamespaceEntity namespace, DebitNoteInputModel debitNote, Map<String, String> config, boolean isPreview) {
        return Uni.createFrom().<Void>emitter(uniEmitter -> {
                    if (debitNote.getSerieNumeroComprobanteAfectado() != null &&
                            debitNote.getProveedor() != null &&
                            debitNote.getProveedor().getRuc() != null
                    ) {
                        uniEmitter.complete(null);
                    } else {
                        uniEmitter.fail(new ConstraintViolationException("ProveedorRUC or SerieNumeroComprobanteAfectado invalid", new HashSet<>()));
                    }
                })
                .map(unused -> {
                    if (debitNote.getSerieNumeroComprobanteAfectado().toUpperCase().startsWith("F")) {
                        return DocumentType.DEBIT_NOTE_FOR_FACTURA_TYPE;
                    } else {
                        return DocumentType.DEBIT_NOTE_FOR_BOLETA_TYPE;
                    }
                })
                .chain(documentType -> Uni
                        .createFrom().<Void>emitter(uniEmitter -> {
                            // Set fake serie-numero for verifying hibernate-validator
                            debitNote.setSerie(documentType.prefix + "01");
                            debitNote.setNumero(1);

                            // Validate input
                            Set<ConstraintViolation<DebitNoteInputModel>> violations = validator.validate(debitNote);
                            if (violations.isEmpty()) {
                                uniEmitter.complete(null);
                            } else {
                                uniEmitter.fail(new ConstraintViolationException(violations));
                            }
                        })
                        .chain(unused -> generateNextID(namespace, debitNote.getProveedor().getRuc(), documentType.name, config, isPreview))
                        .map(generatedIDEntity -> {
                            debitNote.setSerie(documentType.prefix + StringUtils.leftPad(String.valueOf(generatedIDEntity.serie), 2, "0"));
                            debitNote.setNumero(generatedIDEntity.numero);
                            return debitNote;
                        })
                );
    }

    @Override
    public Uni<VoidedDocumentInputModel> enrichWithID(NamespaceEntity namespace, VoidedDocumentInputModel voidedDocument, Map<String, String> config, boolean isPreview) {
        return Uni.createFrom().item(voidedDocument)
                .map(input -> {
                    input.setNumero(1);
                    return input;
                })
                .chain(input -> Uni.createFrom().<VoidedDocumentInputModel>emitter(uniEmitter -> {
                    Set<ConstraintViolation<VoidedDocumentInputModel>> violations = validator.validate(input);
                    if (violations.isEmpty()) {
                        uniEmitter.complete(input);
                    } else {
                        uniEmitter.fail(new ConstraintViolationException(violations));
                    }
                }))
                .chain(input -> generateNextIDVoidedAndSummaryDocument(namespace, input.getProveedor().getRuc(), DocumentType.VOIDED_DOCUMENT_TYPE.name, config, isPreview)
                        .map(generatedIDEntity -> {
                            input.setNumero(generatedIDEntity.numero);
                            return input;
                        })
                );
    }

    @Override
    public Uni<SummaryDocumentInputModel> enrichWithID(NamespaceEntity namespace, SummaryDocumentInputModel summaryDocument, Map<String, String> config, boolean isPreview) {
        return Uni.createFrom().item(summaryDocument)
                .map(input -> {
                    input.setNumero(1);
                    return input;
                })
                .chain(input -> Uni.createFrom().<SummaryDocumentInputModel>emitter(uniEmitter -> {
                    Set<ConstraintViolation<SummaryDocumentInputModel>> violations = validator.validate(input);
                    if (violations.isEmpty()) {
                        uniEmitter.complete(input);
                    } else {
                        uniEmitter.fail(new ConstraintViolationException(violations));
                    }
                }))
                .chain(input -> generateNextIDVoidedAndSummaryDocument(namespace, input.getProveedor().getRuc(), DocumentType.SUMMARY_DOCUMENT_TYPE.name, config, isPreview)
                        .map(generatedIDEntity -> {
                            input.setNumero(generatedIDEntity.numero);
                            return input;
                        })
                );
    }

}

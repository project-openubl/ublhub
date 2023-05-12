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
package io.github.project.openubl.ublhub.documents.idgenerator.impl;

import io.github.project.openubl.ublhub.documents.idgenerator.ID;
import io.github.project.openubl.ublhub.documents.idgenerator.IDGenerator;
import io.github.project.openubl.ublhub.documents.idgenerator.IDGeneratorProvider;
import io.github.project.openubl.ublhub.documents.idgenerator.IDGeneratorType;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
@IDGeneratorProvider(IDGeneratorType.none)
public class NoneIDGenerator implements IDGenerator {

    @Override
    public ID generateInvoiceID(ProjectEntity project, String ruc, Map<String, String> config) {
        return null;
    }

    @Override
    public ID generateCreditNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config) {
        return null;
    }

    @Override
    public ID generateDebitNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config) {
        return null;
    }

    @Override
    public ID generateVoidedDocumentID(ProjectEntity project, String ruc, boolean isPercepcionRetencionOrGuia) {
        return null;
    }

    @Override
    public ID generateSummaryDocumentID(ProjectEntity project, String ruc) {
        return null;
    }

    @Override
    public ID generatePerceptionID(ProjectEntity project, String ruc, Map<String, String> config) {
        return null;
    }

    @Override
    public ID generateRetentionID(ProjectEntity project, String ruc, Map<String, String> config) {
        return null;
    }
}

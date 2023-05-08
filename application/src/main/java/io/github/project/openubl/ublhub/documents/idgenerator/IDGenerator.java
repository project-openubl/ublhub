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
package io.github.project.openubl.ublhub.documents.idgenerator;

import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;

import java.util.Map;

public interface IDGenerator {

    ID generateInvoiceID(ProjectEntity project, String ruc, Map<String, String> config);

    ID generateCreditNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config);

    ID generateDebitNoteID(ProjectEntity project, String ruc, boolean isFactura, Map<String, String> config);

    ID generateVoidedDocumentID(ProjectEntity project, String ruc, boolean isPercepcionRetencionOrGuia);

    ID generateSummaryDocumentID(ProjectEntity project, String ruc);

    ID generatePerceptionID(ProjectEntity project, String ruc, Map<String, String> config);

    ID generateRetentionID(ProjectEntity project, String ruc, Map<String, String> config);

}

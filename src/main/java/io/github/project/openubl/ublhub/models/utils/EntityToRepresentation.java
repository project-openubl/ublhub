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
package io.github.project.openubl.ublhub.models.utils;

import io.github.project.openubl.ublhub.dto.DocumentRepresentation;
import io.github.project.openubl.ublhub.dto.JobErrorRepresentation;
import io.github.project.openubl.ublhub.dto.PageRepresentation;
import io.github.project.openubl.ublhub.dto.SunatStatusRepresentation;
import io.github.project.openubl.ublhub.dto.XMLFileContentRepresentation;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityToRepresentation {
    private EntityToRepresentation() {
        // Just static methods
    }

    public static DocumentRepresentation toRepresentation(UBLDocumentEntity entity) {
        DocumentRepresentation rep = new DocumentRepresentation();

        rep.setId(entity.id);
        rep.setJobInProgress(entity.jobInProgress);

        rep.setCreated(entity.created.getTime());
        rep.setUpdated(entity.updated != null ? entity.updated.getTime() : null);

        // File
        if (entity.xmlFileContent != null) {
            XMLFileContentRepresentation xmlFileContentRep = new XMLFileContentRepresentation();
            rep.setXmlFileContent(xmlFileContentRep);

            xmlFileContentRep.setRuc(entity.xmlFileContent.ruc);
            xmlFileContentRep.setSerieNumero(entity.xmlFileContent.serieNumero);
            xmlFileContentRep.setTipoDocumento(entity.xmlFileContent.tipoDocumento);
        }

        // Sunat
        if (entity.sunatResponse != null) {
            SunatStatusRepresentation sunatResponseRep = new SunatStatusRepresentation();
            rep.setSunatResponse(sunatResponseRep);

            sunatResponseRep.setCode(entity.sunatResponse.code);
            sunatResponseRep.setTicket(entity.sunatResponse.ticket);
            sunatResponseRep.setStatus(entity.sunatResponse.status);
            sunatResponseRep.setDescription(entity.sunatResponse.description);
            sunatResponseRep.setNotes(new ArrayList<>(entity.sunatResponse.notes));
            sunatResponseRep.setHasCdr(entity.cdrFileId != null);
        }

        // Error
        if (entity.jobError != null) {
            JobErrorRepresentation errorJobRep = new JobErrorRepresentation();
            rep.setJobError(errorJobRep);

            errorJobRep.setDescription(entity.jobError.description);
            errorJobRep.setPhase(entity.jobError.phase);
            errorJobRep.setRecoveryAction(entity.jobError.recoveryAction);
            errorJobRep.setRecoveryActionCount(entity.jobError.recoveryActionCount);
        }

        return rep;
    }

    public static <T, R> PageRepresentation<R> toRepresentation(List<T> pageElements, Long totalElements, Function<T, R> mapper) {
        PageRepresentation<R> rep = new PageRepresentation<>();

        // Meta
        PageRepresentation.Meta repMeta = new PageRepresentation.Meta();
        rep.setMeta(repMeta);

        repMeta.setCount(totalElements);

        // Data
        rep.setItems(pageElements.stream()
                .map(mapper)
                .collect(Collectors.toList())
        );

        return rep;
    }

}

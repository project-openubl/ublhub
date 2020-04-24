/**
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
package io.github.project.openubl.xmlsender.models.utils;

import io.github.project.openubl.xmlsender.idm.DocumentRepresentation;
import io.github.project.openubl.xmlsender.models.jpa.entities.DocumentEntity;

public class EntityToRepresentation {
    private EntityToRepresentation() {
        // Just static methods
    }

    public static DocumentRepresentation toRepresentation(DocumentEntity documentEntity) {
        DocumentRepresentation rep = new DocumentRepresentation();

        rep.setId(documentEntity.id);

        rep.setCdrID(documentEntity.cdrID);
        rep.setFileID(documentEntity.fileID);
        rep.setDeliveryStatus(documentEntity.deliveryStatus.toString());
        rep.setCustomId(documentEntity.customId);

        //

        DocumentRepresentation.FileInfoRepresentation fileInfoRep = new DocumentRepresentation.FileInfoRepresentation();
        rep.setFileInfo(fileInfoRep);

        fileInfoRep.setRuc(documentEntity.ruc);
        fileInfoRep.setDeliveryURL(documentEntity.deliveryURL);
        fileInfoRep.setDocumentID(documentEntity.documentID);
        fileInfoRep.setDocumentType(documentEntity.documentType.getDocumentType());
        fileInfoRep.setFilename(documentEntity.filenameWithoutExtension);

        //

        DocumentRepresentation.SunatSecurityCredentialsRepresentation sunatCredentialsRep = new DocumentRepresentation.SunatSecurityCredentialsRepresentation();
        sunatCredentialsRep.setUsername(documentEntity.sunatUsername);
        sunatCredentialsRep.setPassword(documentEntity.sunatPassword != null ? "******" : null);

        //

        DocumentRepresentation.SunatStatusRepresentation sunatStatus = new DocumentRepresentation.SunatStatusRepresentation();
        sunatStatus.setCode(documentEntity.sunatCode);
        sunatStatus.setTicket(documentEntity.sunatTicket);
        sunatStatus.setStatus(documentEntity.sunatStatus);
        sunatStatus.setDescription(documentEntity.sunatDescription);

        return rep;
    }
}

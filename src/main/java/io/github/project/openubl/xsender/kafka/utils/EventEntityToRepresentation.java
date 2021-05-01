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
package io.github.project.openubl.xsender.kafka.utils;

import io.github.project.openubl.xsender.kafka.idm.CompanyCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.idm.UBLDocumentCUDEventRepresentation;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;

public class EventEntityToRepresentation {
    private EventEntityToRepresentation() {
        // Just static methods
    }

    public static CompanyCUDEventRepresentation toRepresentation(CompanyEntity entity) {
        CompanyCUDEventRepresentation rep = new CompanyCUDEventRepresentation();

        rep.setId(entity.getId());
        rep.setOwner(entity.getOwner());

        return rep;
    }

    public static UBLDocumentCUDEventRepresentation toRepresentation(UBLDocumentEntity entity) {
        UBLDocumentCUDEventRepresentation rep = new UBLDocumentCUDEventRepresentation();

        rep.setId(entity.getId());
        rep.setCompanyId(entity.getCompany().getId());

        return rep;
    }

}

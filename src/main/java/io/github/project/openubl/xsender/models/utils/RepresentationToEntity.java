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
package io.github.project.openubl.xsender.models.utils;

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;

public class RepresentationToEntity {

    public static CompanyEntity assign(CompanyEntity entity, CompanyRepresentation rep) {
        entity.ruc = rep.getRuc();
        entity.name = rep.getName();
        entity.description = rep.getDescription();

        if (entity.sunatUrls == null) {
            entity.sunatUrls = new SunatUrlsEntity();
        }
        if (entity.sunatCredentials == null) {
            entity.sunatCredentials = new SunatCredentialsEntity();
        }

        if (rep.getWebServices() != null) {
            assign(entity.sunatUrls, rep.getWebServices());
        }
        if (rep.getCredentials() != null) {
            assign(entity.sunatCredentials, rep.getCredentials());
        }

        return entity;
    }

    private static void assign(SunatUrlsEntity entity, SunatUrlsRepresentation rep) {
        if (rep.getFactura() != null) {
            entity.sunatUrlFactura = rep.getFactura();
        }
        if (rep.getGuia() != null) {
            entity.sunatUrlGuiaRemision = rep.getGuia();
        }
        if (rep.getRetenciones() != null) {
            entity.sunatUrlPercepcionRetencion = rep.getRetenciones();
        }
    }

    private static void assign(SunatCredentialsEntity entity, SunatCredentialsRepresentation rep) {
        if (rep.getUsername() != null) {
            entity.sunatUsername = rep.getUsername();
        }
        if (rep.getPassword() != null) {
            entity.sunatPassword = rep.getPassword();
        }
    }
}

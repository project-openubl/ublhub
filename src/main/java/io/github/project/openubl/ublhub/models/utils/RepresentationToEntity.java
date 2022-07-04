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

import io.github.project.openubl.ublhub.dto.CompanyRepresentation;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatCredentialsDto;
import io.github.project.openubl.ublhub.dto.SunatWebServicesDto;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;

public class RepresentationToEntity {

    public static ProjectEntity assign(ProjectEntity entity, ProjectDto rep) {
        if (rep.getName() != null) {
            entity.name = rep.getName();
        }
        if (rep.getDescription() != null) {
            entity.description = rep.getDescription();
        }

        if (rep.getSunatWebServices() != null) {
            assign(entity.sunat, rep.getSunatWebServices());
        }
        if (rep.getSunatCredentials() != null) {
            assign(entity.sunat, rep.getSunatCredentials());
        }

        return entity;
    }

    public static CompanyEntity assign(CompanyEntity entity, CompanyRepresentation rep) {
        entity.ruc = rep.getRuc();
        entity.name = rep.getName();
        entity.description = rep.getDescription();

        if (entity.sunat == null) {
            entity.sunat = new SunatEntity();
        }

        if (rep.getWebServices() != null) {
            assign(entity.sunat, rep.getWebServices());
        }
        if (rep.getCredentials() != null) {
            assign(entity.sunat, rep.getCredentials());
        }

        return entity;
    }

    private static void assign(SunatEntity entity, SunatWebServicesDto rep) {
        if (rep.getFactura() != null) {
            entity.sunatUrlFactura = rep.getFactura();
        }
        if (rep.getGuia() != null) {
            entity.sunatUrlGuiaRemision = rep.getGuia();
        }
        if (rep.getRetencion() != null) {
            entity.sunatUrlPercepcionRetencion = rep.getRetencion();
        }
    }

    private static void assign(SunatEntity entity, SunatCredentialsDto rep) {
        if (rep.getUsername() != null) {
            entity.sunatUsername = rep.getUsername();
        }
        if (rep.getPassword() != null) {
            entity.sunatPassword = rep.getPassword();
        }
    }
}

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
package io.github.project.openubl.xsender.basic.resources;

import io.github.project.openubl.xsender.core.idm.OrganizationRepresentation;
import io.github.project.openubl.xsender.core.idm.RepositoryRepresentation;
import io.github.project.openubl.xsender.core.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.core.managers.OrganizationManager;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.github.project.openubl.xsender.core.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.core.resources.OrgResource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

@Transactional
@ApplicationScoped
public class BasicOrgResource implements OrgResource {

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    OrganizationManager organizationManager;

    public OrganizationRepresentation getOrganization(String org) {
        OrganizationEntity organizationEntity = organizationRepository.findByName(org).orElseThrow(NotFoundException::new);
        return EntityToRepresentation.toRepresentation(organizationEntity);
    }

    public OrganizationRepresentation updateOrganization(String org, OrganizationRepresentation rep) {
        OrganizationEntity organizationEntity = organizationRepository.findByName(org).orElseThrow(NoClassDefFoundError::new);
        organizationEntity = organizationManager.updateCorporate(rep, organizationEntity);
        return EntityToRepresentation.toRepresentation(organizationEntity);
    }

    public void updateCorporateSUNATCredentials(String org, SunatCredentialsRepresentation rep) {
        OrganizationEntity organizationEntity = organizationRepository.findByName(org).orElseThrow(NoClassDefFoundError::new);
        organizationManager.updateCorporateCredentials(rep, organizationEntity);
    }

    public List<RepositoryRepresentation> listRepositories() {
        return new ArrayList<>();
    }

    public RepositoryRepresentation createRepositorie() {
        return new RepositoryRepresentation();
    }

}


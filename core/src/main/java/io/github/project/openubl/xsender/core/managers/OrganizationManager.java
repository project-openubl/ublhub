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
package io.github.project.openubl.xsender.core.managers;

import io.github.project.openubl.xsender.core.idm.OrganizationRepresentation;
import io.github.project.openubl.xsender.core.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.core.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.github.project.openubl.xsender.core.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.core.models.jpa.entities.SunatUrlsEntity;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class OrganizationManager {

    private static final Logger LOG = Logger.getLogger(OrganizationManager.class);

    @Inject
    OrganizationRepository organizationRepository;

    public OrganizationEntity createCorporate(OrganizationRepresentation rep) {
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(UUID.randomUUID().toString());

        organizationEntity.setName(rep.getName());

        if (rep.getSunatUrls() != null) {
            SunatUrlsEntity sunatUrlsEntity = new SunatUrlsEntity();
            organizationEntity.setSunatUrls(sunatUrlsEntity);

            updateSunatUrls(rep.getSunatUrls(), sunatUrlsEntity);
        }

        organizationRepository.persist(organizationEntity);
        return organizationEntity;
    }

    /**
     * Shouldn't update 'name'
     */
    public OrganizationEntity updateCorporate(OrganizationRepresentation rep, OrganizationEntity organizationEntity) {
        if (rep.getSunatUrls() != null) {
            if (organizationEntity.getSunatUrls() == null) {
                organizationEntity.setSunatUrls(new SunatUrlsEntity());
            }
            updateSunatUrls(rep.getSunatUrls(), organizationEntity.getSunatUrls());
        }

        organizationRepository.persist(organizationEntity);
        return organizationEntity;
    }

    private void updateSunatUrls(SunatUrlsRepresentation rep, SunatUrlsEntity entity) {
        if (rep.getFactura() != null) {
            entity.setSunatUrlFactura(rep.getFactura());
        }
        if (rep.getGuiaRemision() != null) {
            entity.setSunatUrlGuiaRemision(rep.getGuiaRemision());
        }
        if (rep.getPercepcionRetencion() != null) {
            entity.setSunatUrlPercepcionRetencion(rep.getPercepcionRetencion());
        }
    }

    public void updateCorporateCredentials(SunatCredentialsRepresentation rep, OrganizationEntity organizationEntity) {
        if (organizationEntity.getSunatCredentials() == null) {
            organizationEntity.setSunatCredentials(new SunatCredentialsEntity());
        }

        organizationEntity.getSunatCredentials().setSunatUsername(rep.getSunatUsername());
        organizationEntity.getSunatCredentials().setSunatPassword(rep.getSunatPassword());
    }
}

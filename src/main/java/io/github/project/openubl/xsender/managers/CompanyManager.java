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
package io.github.project.openubl.xsender.managers;

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class CompanyManager {

    private static final Logger LOG = Logger.getLogger(CompanyManager.class);

    @Inject
    CompanyRepository companyRepository;

    public CompanyEntity createCompany(String owner, CompanyRepresentation rep) {
        CompanyEntity companyEntity = new CompanyEntity();
        companyEntity.setId(UUID.randomUUID().toString());

        companyEntity.setOwner(owner);
        companyEntity.setName(rep.getName().toLowerCase());
        companyEntity.setDescription(rep.getDescription());
        companyEntity.setCreatedOn(new Date());

        if (rep.getWebServices() != null) {
            SunatUrlsEntity sunatUrlsEntity = new SunatUrlsEntity();
            companyEntity.setSunatUrls(sunatUrlsEntity);

            updateSunatUrls(rep.getWebServices(), sunatUrlsEntity);
        }

        if (rep.getCredentials() != null) {
            SunatCredentialsEntity sunatCredentialsEntity = new SunatCredentialsEntity();
            companyEntity.setSunatCredentials(sunatCredentialsEntity);

            updateCorporateCredentials(rep.getCredentials(), companyEntity);
        }

        companyRepository.persist(companyEntity);
        return companyEntity;
    }

    /**
     * Shouldn't update 'name'
     */
    public CompanyEntity updateCompany(CompanyRepresentation rep, CompanyEntity entity) {
        if (rep.getDescription() != null) {
            entity.setDescription(rep.getDescription());
        }

        if (rep.getWebServices() != null) {
            if (entity.getSunatUrls() == null) {
                entity.setSunatUrls(new SunatUrlsEntity());
            }
            updateSunatUrls(rep.getWebServices(), entity.getSunatUrls());
        }

        companyRepository.persist(entity);
        return entity;
    }

    private void updateSunatUrls(SunatUrlsRepresentation rep, SunatUrlsEntity entity) {
        if (rep.getFactura() != null) {
            entity.setSunatUrlFactura(rep.getFactura());
        }
        if (rep.getGuia() != null) {
            entity.setSunatUrlGuiaRemision(rep.getGuia());
        }
        if (rep.getRetenciones() != null) {
            entity.setSunatUrlPercepcionRetencion(rep.getRetenciones());
        }
    }

    public void updateCorporateCredentials(SunatCredentialsRepresentation rep, CompanyEntity companyEntity) {
        if (companyEntity.getSunatCredentials() == null) {
            companyEntity.setSunatCredentials(new SunatCredentialsEntity());
        }

        companyEntity.getSunatCredentials().setSunatUsername(rep.getUsername());
        companyEntity.getSunatCredentials().setSunatPassword(rep.getPassword());
    }
}

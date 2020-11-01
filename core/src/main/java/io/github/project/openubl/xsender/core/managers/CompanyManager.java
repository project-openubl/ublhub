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

import io.github.project.openubl.xsender.core.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.core.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.core.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.core.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.core.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.core.models.jpa.entities.SunatUrlsEntity;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
        companyEntity.setName(rep.getName());

        if (rep.getSunatWsUrls() != null) {
            SunatUrlsEntity sunatUrlsEntity = new SunatUrlsEntity();
            companyEntity.setSunatUrls(sunatUrlsEntity);

            updateSunatUrls(rep.getSunatWsUrls(), sunatUrlsEntity);
        }

        companyRepository.persist(companyEntity);
        return companyEntity;
    }

    /**
     * Shouldn't update 'name'
     */
    public CompanyEntity updateCompany(CompanyRepresentation rep, CompanyEntity CompanyEntity) {
        if (rep.getSunatWsUrls() != null) {
            if (CompanyEntity.getSunatUrls() == null) {
                CompanyEntity.setSunatUrls(new SunatUrlsEntity());
            }
            updateSunatUrls(rep.getSunatWsUrls(), CompanyEntity.getSunatUrls());
        }

        companyRepository.persist(CompanyEntity);
        return CompanyEntity;
    }

    private void updateSunatUrls(SunatUrlsRepresentation rep, SunatUrlsEntity entity) {
        if (rep.getFactura() != null) {
            entity.setSunatUrlFactura(rep.getFactura());
        }
        if (rep.getGuia() != null) {
            entity.setSunatUrlGuiaRemision(rep.getGuia());
        }
        if (rep.getRetencion() != null) {
            entity.setSunatUrlPercepcionRetencion(rep.getRetencion());
        }
    }

    public void updateCorporateCredentials(SunatCredentialsRepresentation rep, CompanyEntity CompanyEntity) {
        if (CompanyEntity.getSunatCredentials() == null) {
            CompanyEntity.setSunatCredentials(new SunatCredentialsEntity());
        }

        CompanyEntity.getSunatCredentials().setSunatUsername(rep.getUsername());
        CompanyEntity.getSunatCredentials().setSunatPassword(rep.getPassword());
    }
}

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
package io.github.project.openubl.xsender.basic.bootstrap;

import io.github.project.openubl.xsender.basic.Constants;
import io.github.project.openubl.xsender.core.models.OrganizationType;
import io.github.project.openubl.xsender.core.models.jpa.OrganizationRepository;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class MasterOrgBootstrap {

    private static final Logger logger = Logger.getLogger(MasterOrgBootstrap.class);

    @Inject
    OrganizationRepository organizationRepository;

    void onStart(@Observes StartupEvent ev) {
        logger.info("Server Bootstrap...");
        bootstrap();
    }

    @Transactional
    private void bootstrap() {
        // Create Default Organizations
        Optional<OrganizationEntity> masterOrg = organizationRepository.findByName(Constants.DEFAULT_USERNAME);
        if (masterOrg.isEmpty()) {
            createMasterOrganization();
        }
    }

    @Transactional
    private void createMasterOrganization() {
        logger.info("Initializing Admin Organization " + Constants.DEFAULT_USERNAME);

        OrganizationEntity organization = OrganizationEntity.Builder.anOrganizationEntity()
                .withId(Constants.DEFAULT_USERNAME)
                .withName(Constants.DEFAULT_USERNAME)
                .withOwner(Constants.DEFAULT_USERNAME)
                .withType(OrganizationType.USER)
                .build();

        organizationRepository.persist(organization);
        logger.info("Default key providers for Admin Organization " + organization.getName() + " have been created");
    }

}

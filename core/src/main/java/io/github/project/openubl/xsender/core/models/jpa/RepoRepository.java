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
package io.github.project.openubl.xsender.core.models.jpa;

import io.github.project.openubl.xsender.core.models.jpa.entities.RepositoryEntity;
import io.github.project.openubl.xsender.core.models.jpa.entities.OrganizationEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class RepoRepository implements PanacheRepositoryBase<RepositoryEntity, String> {

    public Optional<RepositoryEntity> findByName(OrganizationEntity corporate, String name) {
        return find("corporate = :corporate and name = :name", Parameters.with("corporate", "corporate").and("name", name)).firstResultOptional();
    }

}

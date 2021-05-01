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
package io.github.project.openubl.xsender.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.outbox.quarkus.ExportedEvent;
import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.kafka.idm.CompanyCUDEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityEventProducer;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.kafka.utils.EventEntityToRepresentation;
import io.github.project.openubl.xsender.managers.CompanyManager;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.List;

@Transactional
@ApplicationScoped
public class DefaultCurrentUserResource implements CurrentUserResource {

    private static final Logger LOG = Logger.getLogger(CompanyManager.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyManager companyManager;

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response createCompany(CompanyRepresentation rep) {
        if (companyRepository.findByName(rep.getName()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Name already taken")
                    .build();
        }

        CompanyEntity companyEntity = companyManager.createCompany(userIdentity.getUsername(), rep);

        try {
            CompanyCUDEventRepresentation eventRep = EventEntityToRepresentation.toRepresentation(companyEntity);
            String eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new EntityEventProducer(companyEntity.getId(), EntityType.company, EventType.CREATED, eventPayload));
        } catch (JsonProcessingException e) {
            LOG.error(e);
        }

        return Response.ok()
                .entity(EntityToRepresentation.toRepresentation(companyEntity))
                .build();
    }

    public PageRepresentation<CompanyRepresentation> getCompanies(
            String filterText,
            Integer offset,
            Integer limit,
            List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, CompanyRepository.SORT_BY_FIELDS);

        PageModel<CompanyEntity> pageModel;
        if (filterText != null && !filterText.trim().isEmpty()) {
            pageModel = CompanyRepository.list(userIdentity.getUsername(), filterText, pageBean, sortBeans);
        } else {
            pageModel = CompanyRepository.list(userIdentity.getUsername(), pageBean, sortBeans);
        }

        return EntityToRepresentation.toRepresentation(pageModel, EntityToRepresentation::toRepresentation);
    }

}


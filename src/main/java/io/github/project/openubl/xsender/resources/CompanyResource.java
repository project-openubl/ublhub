package io.github.project.openubl.xsender.resources;

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

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.managers.CompanyManager;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/namespaces/{namespaceId}/companies")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class CompanyResource {

    private static final Logger LOG = Logger.getLogger(CompanyResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyManager companyManager;

    @GET
    @Path("/")
    public PageRepresentation<CompanyRepresentation> getCompanies(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("createdOn:desc") List<String> sortBy
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);

        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, NamespaceRepository.SORT_BY_FIELDS);

        PageModel<CompanyEntity> pageModel;
        if (filterText != null && !filterText.trim().isEmpty()) {
            pageModel = companyRepository.list(namespaceEntity, filterText, pageBean, sortBeans);
        } else {
            pageModel = companyRepository.list(namespaceEntity, pageBean, sortBeans);
        }

        return EntityToRepresentation.toRepresentation(pageModel, EntityToRepresentation::toRepresentation);
    }

    @POST
    @Path("/")
    public Response createCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull @Valid CompanyRepresentation rep
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);

        if (companyRepository.findById(namespaceEntity, rep.getRuc()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("RUC already taken")
                    .build();
        }

        CompanyEntity companyEntity = companyManager.createCompany(namespaceEntity, rep);

        return Response.ok()
                .entity(EntityToRepresentation.toRepresentation(companyEntity))
                .build();
    }

    @GET
    @Path("/{companyId}")
    public CompanyRepresentation getCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);
        return EntityToRepresentation.toRepresentation(companyEntity);
    }

    @PUT
    @Path("/{companyId}")
    public CompanyRepresentation updateCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId,
            @NotNull CompanyRepresentation rep
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        companyEntity = companyManager.updateCompany(rep, companyEntity);

        return EntityToRepresentation.toRepresentation(companyEntity);
    }

    @DELETE
    @Path("/{companyId}")
    public void deleteNamespace(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        companyRepository.delete(companyEntity);
    }

}



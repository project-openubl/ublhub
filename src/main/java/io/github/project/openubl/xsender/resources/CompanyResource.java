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
import io.github.project.openubl.xsender.idm.ErrorRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.keys.ComponentProvider;
import io.github.project.openubl.xsender.keys.KeyManager;
import io.github.project.openubl.xsender.keys.component.ComponentModel;
import io.github.project.openubl.xsender.keys.component.ComponentValidationException;
import io.github.project.openubl.xsender.keys.component.utils.ComponentUtil;
import io.github.project.openubl.xsender.managers.CompanyManager;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.models.utils.RepresentationToModel;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.jboss.logging.Logger;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.KeysMetadataRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Path("/namespaces/{namespaceId}/companies")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class CompanyResource {

    private static final Logger LOG = Logger.getLogger(CompanyResource.class);

    @Context
    UriInfo uriInfo;

    @Inject
    UserIdentity userIdentity;

    @Inject
    KeyManager keystore;

    @Inject
    ComponentUtil componentUtil;

    @Inject
    ComponentProvider componentProvider;

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
    public void deleteCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        companyRepository.delete(companyEntity);
    }

    @GET
    @Path("/{companyId}/keys")
    public KeysMetadataRepresentation getKeyMetadata(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        KeysMetadataRepresentation keys = new KeysMetadataRepresentation();
        keys.setKeys(new LinkedList<>());
        keys.setActive(new HashMap<>());

        for (KeyWrapper key : keystore.getKeys(companyEntity.getId())) {
            KeysMetadataRepresentation.KeyMetadataRepresentation r = new KeysMetadataRepresentation.KeyMetadataRepresentation();
            r.setProviderId(key.getProviderId());
            r.setProviderPriority(key.getProviderPriority());
            r.setKid(key.getKid());
            r.setStatus(key.getStatus() != null ? key.getStatus().name() : null);
            r.setType(key.getType());
            r.setAlgorithm(key.getAlgorithm());
            r.setPublicKey(key.getPublicKey() != null ? PemUtils.encodeKey(key.getPublicKey()) : null);
            r.setCertificate(key.getCertificate() != null ? PemUtils.encodeCertificate(key.getCertificate()) : null);
            keys.getKeys().add(r);

            if (key.getStatus().isActive()) {
                if (!keys.getActive().containsKey(key.getAlgorithm())) {
                    keys.getActive().put(key.getAlgorithm(), key.getKid());
                }
            }
        }

        return keys;
    }

    @GET
    @Path("/{companyId}/components")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ComponentRepresentation> getComponents(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId,
            @QueryParam("parent") String parent,
            @QueryParam("type") String type,
            @QueryParam("name") String name
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        List<ComponentModel> components;
        if (parent == null && type == null) {
            components = componentProvider.getComponents(companyEntity.getId());
        } else if (type == null) {
            components = componentProvider.getComponents(companyEntity.getId(), parent);
        } else if (parent == null) {
            components = componentProvider.getComponents(companyEntity.getId(), companyEntity.getId(), type);
        } else {
            components = componentProvider.getComponents(companyEntity.getId(), parent, type);
        }
        List<ComponentRepresentation> reps = new LinkedList<>();
        for (ComponentModel component : components) {
            if (name != null && !name.equals(component.getName())) continue;
            ComponentRepresentation rep = null;
            try {
                rep = EntityToRepresentation.toRepresentation(component, false, componentUtil);
            } catch (Exception e) {
                LOG.error("Failed to get component list for component model" + component.getName() + "of company " + companyEntity.getName());
                rep = EntityToRepresentation.toRepresentationWithoutConfig(component);
            }
            reps.add(rep);
        }
        return reps;
    }

    @POST
    @Path("/{companyId}/components")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId, ComponentRepresentation rep
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        try {
            ComponentModel model = RepresentationToModel.toModel(rep);
            if (model.getParentId() == null) model.setParentId(companyEntity.getId());

            model = componentProvider.addComponentModel(companyEntity.getId(), model);

            return Response.created(uriInfo.getAbsolutePathBuilder().path(model.getId()).build()).build();
        } catch (ComponentValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorRepresentation(e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @GET
    @Path("/{companyId}/components/{componentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentRepresentation getComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId,
            @PathParam("componentId") String componentId
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        ComponentModel model = componentProvider.getComponent(companyEntity.getId(), componentId);
        if (model == null) {
            throw new NotFoundException("Could not find component");
        }

        return EntityToRepresentation.toRepresentation(model, false, componentUtil);
    }

    @PUT
    @Path("/{companyId}/components/{componentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId,
            @PathParam("componentId") String componentId,
            ComponentRepresentation rep
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        try {
            ComponentModel model = componentProvider.getComponent(companyEntity.getId(), componentId);
            if (model == null) {
                throw new NotFoundException("Could not find component");
            }
            RepresentationToModel.updateComponent(rep, model, false, componentUtil);

            componentProvider.updateComponent(companyEntity.getId(), model);
            return Response.noContent().build();
        } catch (ComponentValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorRepresentation(e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @DELETE
    @Path("/{companyId}/components/{componentId}")
    public void removeComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId,
            @PathParam("componentId") String componentId
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);

        ComponentModel model = componentProvider.getComponent(companyEntity.getId(), componentId);
        if (model == null) {
            throw new NotFoundException("Could not find component");
        }

        componentProvider.removeComponent(companyEntity.getId(), model);
    }
}



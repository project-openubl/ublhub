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
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.models.utils.RepresentationToEntity;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.security.UserIdentity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class CompanyResource {

    private static final Logger LOG = Logger.getLogger(CompanyResource.class);

    @Context
    UriInfo uriInfo;

    @Inject
    UserIdentity userIdentity;

//    @Inject
//    KeyManager keystore;
//
//    @Inject
//    ComponentUtil componentUtil;
//
//    @Inject
//    ComponentProvider componentProvider;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

//    @Inject
//    CompanyManager companyManager;

    @GET
    @Path("/{namespaceId}/companies/{companyId}")
    public Uni<Response> getCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> companyRepository.findById(namespaceEntity, companyId))
                )
                .onItem().ifNotNull().transform(companyEntity -> Response.ok()
                        .entity(EntityToRepresentation.toRepresentation(companyEntity))
                        .build()
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @POST
    @Path("/{namespaceId}/companies")
    public Uni<Response> createCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull @Valid CompanyRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> companyRepository.findByRuc(namespaceEntity, rep.getRuc())
                                .onItem().ifNotNull().transform(companyEntity -> Response.ok()
                                        .status(Response.Status.CONFLICT)
                                        .build()
                                )
                                .onItem().ifNull().switchTo(() -> {
                                    CompanyEntity companyEntity = new CompanyEntity();
                                    companyEntity.id = UUID.randomUUID().toString();
                                    companyEntity.namespace = namespaceEntity;
                                    companyEntity.createdOn = new Date();

                                    RepresentationToEntity.assign(companyEntity, rep);
                                    return companyEntity.persist().map(unused -> Response.ok()
                                            .entity(EntityToRepresentation.toRepresentation(companyEntity))
                                            .build());
                                })
                        )
                        .onItem().ifNull().continueWith(Response.ok()
                                .status(Response.Status.NOT_FOUND)::build
                        )
                );
    }

    @PUT
    @Path("/{namespaceId}/companies/{companyId}")
    public Uni<Response> updateCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId,
            @NotNull CompanyRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> companyRepository.findById(namespaceEntity, companyId)
                                .onItem().ifNotNull().invoke(companyEntity -> RepresentationToEntity.assign(companyEntity, rep))
                        )
                )
                .onItem().ifNotNull().transform(companyEntity -> Response.ok()
                        .entity(EntityToRepresentation.toRepresentation(companyEntity))
                        .build()
                )
                .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{namespaceId}/companies/{companyId}")
    public Uni<Response> deleteCompany(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> companyRepository.findById(namespaceEntity, companyId)
                                .onItem().ifNotNull().call(PanacheEntityBase::delete)
                        )
                )
                .onItem().ifNotNull().transform(entity -> Response.status(Response.Status.NO_CONTENT).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @GET
    @Path("/{namespaceId}/companies")
    public Uni<Response> getCompanies(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("createdOn:desc") List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, CompanyRepository.SORT_BY_FIELDS);

        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> {
                            UniAndGroup2<List<CompanyEntity>, Long> searchResult;
                            if (filterText != null && !filterText.trim().isEmpty()) {
                                searchResult = companyRepository.list(namespaceEntity, filterText, pageBean, sortBeans);
                            } else {
                                searchResult = companyRepository.list(namespaceEntity, pageBean, sortBeans);
                            }
                            return searchResult.asTuple();
                        })
                )
                .onItem().ifNotNull().transform(tuple2 -> Response.ok()
                        .entity(EntityToRepresentation.toRepresentation(
                                tuple2.getItem1(),
                                tuple2.getItem2(),
                                EntityToRepresentation::toRepresentation
                        ))
                        .build()
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }


//    @GET
//    @Path("/{companyId}/keys")
//    public KeysMetadataRepresentation getKeyMetadata(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("companyId") @NotNull String companyId
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);
//
//        KeysMetadataRepresentation keys = new KeysMetadataRepresentation();
//        keys.setKeys(new LinkedList<>());
//        keys.setActive(new HashMap<>());
//
//        for (KeyWrapper key : keystore.getKeys(companyEntity.getId())) {
//            KeysMetadataRepresentation.KeyMetadataRepresentation r = new KeysMetadataRepresentation.KeyMetadataRepresentation();
//            r.setProviderId(key.getProviderId());
//            r.setProviderPriority(key.getProviderPriority());
//            r.setKid(key.getKid());
//            r.setStatus(key.getStatus() != null ? key.getStatus().name() : null);
//            r.setType(key.getType());
//            r.setAlgorithm(key.getAlgorithm());
//            r.setPublicKey(key.getPublicKey() != null ? PemUtils.encodeKey(key.getPublicKey()) : null);
//            r.setCertificate(key.getCertificate() != null ? PemUtils.encodeCertificate(key.getCertificate()) : null);
//            keys.getKeys().add(r);
//
//            if (key.getStatus().isActive()) {
//                if (!keys.getActive().containsKey(key.getAlgorithm())) {
//                    keys.getActive().put(key.getAlgorithm(), key.getKid());
//                }
//            }
//        }
//
//        return keys;
//    }
//
//    @GET
//    @Path("/{companyId}/components")
//    @Produces(MediaType.APPLICATION_JSON)
//    public List<ComponentRepresentation> getComponents(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("companyId") @NotNull String companyId,
//            @QueryParam("parent") String parent,
//            @QueryParam("type") String type,
//            @QueryParam("name") String name
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);
//
//        List<ComponentModel> components;
//        if (parent == null && type == null) {
//            components = componentProvider.getComponents(companyEntity.getId());
//        } else if (type == null) {
//            components = componentProvider.getComponents(companyEntity.getId(), parent);
//        } else if (parent == null) {
//            components = componentProvider.getComponents(companyEntity.getId(), companyEntity.getId(), type);
//        } else {
//            components = componentProvider.getComponents(companyEntity.getId(), parent, type);
//        }
//        List<ComponentRepresentation> reps = new LinkedList<>();
//        for (ComponentModel component : components) {
//            if (name != null && !name.equals(component.getName())) continue;
//            ComponentRepresentation rep = null;
//            try {
//                rep = EntityToRepresentation.toRepresentation(component, false, componentUtil);
//            } catch (Exception e) {
//                LOG.error("Failed to get component list for component model" + component.getName() + "of company " + companyEntity.getName());
//                rep = EntityToRepresentation.toRepresentationWithoutConfig(component);
//            }
//            reps.add(rep);
//        }
//        return reps;
//    }
//
//    @POST
//    @Path("/{companyId}/components")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response createComponent(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("companyId") @NotNull String companyId, ComponentRepresentation rep
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);
//
//        try {
//            ComponentModel model = RepresentationToModel.toModel(rep);
//            if (model.getParentId() == null) model.setParentId(companyEntity.getId());
//
//            model = componentProvider.addComponentModel(companyEntity.getId(), model);
//
//            return Response.created(uriInfo.getAbsolutePathBuilder().path(model.getId()).build()).build();
//        } catch (ComponentValidationException e) {
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity(new ErrorRepresentation(e.getMessage()))
//                    .type(MediaType.APPLICATION_JSON)
//                    .build();
//        }
//    }
//
//    @GET
//    @Path("/{companyId}/components/{componentId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ComponentRepresentation getComponent(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("companyId") @NotNull String companyId,
//            @PathParam("componentId") String componentId
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);
//
//        ComponentModel model = componentProvider.getComponent(companyEntity.getId(), componentId);
//        if (model == null) {
//            throw new NotFoundException("Could not find component");
//        }
//
//        return EntityToRepresentation.toRepresentation(model, false, componentUtil);
//    }
//
//    @PUT
//    @Path("/{companyId}/components/{componentId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response updateComponent(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("companyId") @NotNull String companyId,
//            @PathParam("componentId") String componentId,
//            ComponentRepresentation rep
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);
//
//        try {
//            ComponentModel model = componentProvider.getComponent(companyEntity.getId(), componentId);
//            if (model == null) {
//                throw new NotFoundException("Could not find component");
//            }
//            RepresentationToModel.updateComponent(rep, model, false, componentUtil);
//
//            componentProvider.updateComponent(companyEntity.getId(), model);
//            return Response.noContent().build();
//        } catch (ComponentValidationException e) {
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity(new ErrorRepresentation(e.getMessage()))
//                    .type(MediaType.APPLICATION_JSON)
//                    .build();
//        }
//    }
//
//    @DELETE
//    @Path("/{companyId}/components/{componentId}")
//    public void removeComponent(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("companyId") @NotNull String companyId,
//            @PathParam("componentId") String componentId
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        CompanyEntity companyEntity = companyRepository.findById(namespaceEntity, companyId).orElseThrow(NotFoundException::new);
//
//        ComponentModel model = componentProvider.getComponent(companyEntity.getId(), componentId);
//        if (model == null) {
//            throw new NotFoundException("Could not find component");
//        }
//
//        componentProvider.removeComponent(companyEntity.getId(), model);
//    }
}



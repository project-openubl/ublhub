/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.resources;

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

import io.github.project.openubl.ublhub.idm.NamespaceRepresentation;
import io.github.project.openubl.ublhub.keys.DefaultKeyProviders;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.component.utils.ComponentUtil;
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import io.github.project.openubl.ublhub.models.jpa.NamespaceRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;
import io.github.project.openubl.ublhub.models.utils.EntityToRepresentation;
import io.github.project.openubl.ublhub.models.utils.RepresentationToEntity;
import io.github.project.openubl.ublhub.models.utils.RepresentationToModel;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.KeysMetadataRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class NamespaceResource {

    private static final Logger LOG = Logger.getLogger(NamespaceResource.class);

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    ComponentRepository componentRepository;

    @Inject
    KeyManager keyManager;

    @Inject
    ComponentUtil componentUtil;

    @Inject
    DefaultKeyProviders defaultKeyProviders;

    @POST
    @Path("/")
    public Uni<Response> createNameSpace(@NotNull @Valid NamespaceRepresentation rep) {
        return namespaceRepository.findByName(rep.getName())
                .onItem().ifNotNull().transform(entity -> Response.status(Response.Status.CONFLICT).build())
                .onItem().ifNull().switchTo(() -> Panache
                        .withTransaction(() -> {
                            final NamespaceEntity namespaceEntity = new NamespaceEntity();
                            namespaceEntity.id = UUID.randomUUID().toString();
                            namespaceEntity.name = rep.getName();
                            namespaceEntity.description = rep.getDescription();

                            namespaceEntity.sunat = new SunatEntity();
                            namespaceEntity.sunat.sunatUsername = rep.getCredentials().getUsername();
                            namespaceEntity.sunat.sunatPassword = rep.getCredentials().getPassword();
                            namespaceEntity.sunat.sunatUrlFactura = rep.getWebServices().getFactura();
                            namespaceEntity.sunat.sunatUrlGuiaRemision = rep.getWebServices().getGuia();
                            namespaceEntity.sunat.sunatUrlPercepcionRetencion = rep.getWebServices().getRetenciones();

                            return namespaceRepository.persist(namespaceEntity)
                                    .chain(namespace -> defaultKeyProviders.createProviders(namespace))
                                    .map(unused -> namespaceEntity);
                        })
                        .map(EntityToRepresentation::toRepresentation)
                        .map(result -> Response.status(Response.Status.CREATED).entity(result).build())
                );
    }

    @GET
    @Path("/")
    public Uni<List<NamespaceRepresentation>> getNamespaces() {
        Sort sort = Sort.by(NamespaceRepository.SortByField.created.toString(), Sort.Direction.Descending);
        return Panache.withTransaction(() -> namespaceRepository
                .listAll(sort)
                .map(entities -> entities.stream()
                        .map(EntityToRepresentation::toRepresentation)
                        .collect(Collectors.toList())
                )
        );
    }

    @GET
    @Path("/{namespaceId}")
    public Uni<Response> getNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache.withTransaction(() -> namespaceRepository.findById(namespaceId))
                .onItem().ifNotNull().transform(EntityToRepresentation::toRepresentation)
                .map(rep -> {
                    if (rep != null) {
                        return Response.status(Response.Status.OK).entity(rep).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });
    }

    @PUT
    @Path("/{namespaceId}")
    public Uni<Response> updateNamespace(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull NamespaceRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().invoke(namespaceEntity -> RepresentationToEntity.assign(namespaceEntity, rep))
                )
                .onItem().ifNotNull().transform(EntityToRepresentation::toRepresentation)
                .map(result -> {
                    if (result != null) {
                        return Response.status(Response.Status.OK).entity(result).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });
    }

    @DELETE
    @Path("/{namespaceId}")
    public Uni<Response> deleteNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache.withTransaction(() -> namespaceRepository.deleteById(namespaceId))
                .map(result -> Response
                        .status(result ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                        .build()
                );
    }

    @GET
    @Path("/{namespaceId}/keys")
    public Uni<Response> getKeyMetadata(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespace -> keyManager.getKeys(namespace)
                                .map(keyWrappers -> {
                                    KeysMetadataRepresentation keys = new KeysMetadataRepresentation();
                                    keys.setActive(new HashMap<>());

                                    List<KeysMetadataRepresentation.KeyMetadataRepresentation> namespaceKeys = keyWrappers.stream()
                                            .map(key -> {
                                                if (key.getStatus().isActive()) {
                                                    if (!keys.getActive().containsKey(key.getAlgorithmOrDefault())) {
                                                        keys.getActive().put(key.getAlgorithmOrDefault(), key.getKid());
                                                    }
                                                }
                                                return toKeyMetadataRepresentation(key);
                                            })
                                            .collect(Collectors.toList());
                                    keys.setKeys(namespaceKeys);

                                    return keys;
                                })
                                .map(keysMetadata -> Response.ok(keysMetadata).build())
                        )
                        .onItem().ifNull().continueWith(Response.ok().status(Response.Status.NOT_FOUND)::build)
                );
    }

    private KeysMetadataRepresentation.KeyMetadataRepresentation toKeyMetadataRepresentation(KeyWrapper key) {
        KeysMetadataRepresentation.KeyMetadataRepresentation r = new KeysMetadataRepresentation.KeyMetadataRepresentation();
        r.setProviderId(key.getProviderId());
        r.setProviderPriority(key.getProviderPriority());
        r.setKid(key.getKid());
        r.setStatus(key.getStatus() != null ? key.getStatus().name() : null);
        r.setType(key.getType());
        r.setAlgorithm(key.getAlgorithmOrDefault());
        r.setPublicKey(key.getPublicKey() != null ? PemUtils.encodeKey(key.getPublicKey()) : null);
        r.setCertificate(key.getCertificate() != null ? PemUtils.encodeCertificate(key.getCertificate()) : null);
        r.setUse(key.getUse());
        return r;
    }

    @GET
    @Path("/{namespaceId}/components")
    public Uni<Response> getComponents(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @QueryParam("parent") String parent,
            @QueryParam("type") String type,
            @QueryParam("name") String name
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespace -> {
                            if (parent == null && type == null) {
                                return componentRepository.getComponents(namespace);
                            } else if (type == null) {
                                return componentRepository.getComponents(namespace, parent);
                            } else if (parent == null) {
                                return componentRepository.getComponents(namespace.id, type);
                            } else {
                                return componentRepository.getComponents(parent, type);
                            }
                        })
                        .map(components -> components.stream()
                                .filter(component -> Objects.isNull(name) || Objects.equals(component.getName(), name))
                                .map(component -> {
                                    try {
                                        return EntityToRepresentation.toRepresentation(component, false, componentUtil);
                                    } catch (Exception e) {
                                        LOG.error("Failed to get component list for component model" + component.getName() + "of namespace " + namespaceId);
                                        return EntityToRepresentation.toRepresentationWithoutConfig(component);
                                    }
                                })
                                .collect(Collectors.toList())
                        )
                        .map(components -> Response.ok(components).build())
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @POST
    @Path("/{namespaceId}/components")
    public Uni<Response> createComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            ComponentRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespace -> {
                            ComponentModel model = RepresentationToModel.toModel(rep);
                            model.setId(null);
                            return componentRepository.addComponentModel(namespace, model);
                        })
                        .map(entity -> Response.status(Response.Status.CREATED).build())
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @GET
    @Path("/{namespaceId}/components/{componentId}")
    public Uni<Response> getComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("componentId") String componentId
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespace -> componentRepository.getComponent(namespace, componentId))
                )
                .onItem().ifNotNull().transform(componentModel -> Response.ok().entity(EntityToRepresentation.toRepresentation(componentModel, false, componentUtil)).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @PUT
    @Path("/{namespaceId}/components/{componentId}")
    public Uni<Response> updateComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("componentId") String componentId,
            ComponentRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespace -> componentRepository.getComponent(namespace, componentId)
                                .chain(componentModel -> {
                                    RepresentationToModel.updateComponent(rep, componentModel, false, componentUtil);
                                    return componentRepository
                                            .updateComponent(namespace, componentModel)
                                            .chain(() -> componentRepository.getComponent(namespace, componentId));
                                })
                        )
                        .map(componentModel -> Response.ok().entity(EntityToRepresentation.toRepresentation(componentModel, false, componentUtil)).build())
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @DELETE
    @Path("/{namespaceId}/components/{componentId}")
    public Uni<Response> removeComponent(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("componentId") String componentId
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespace -> componentRepository.getComponent(namespace, componentId)
                                .chain(componentModel -> componentRepository.removeComponent(componentModel))
                        )
                        .map((result) -> Response.ok().build())
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }
}



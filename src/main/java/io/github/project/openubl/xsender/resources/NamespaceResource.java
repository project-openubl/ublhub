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

import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.keys.KeyManager;
import io.github.project.openubl.xsender.keys.component.utils.ComponentUtil;
import io.github.project.openubl.xsender.models.jpa.ComponentRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.security.UserIdentity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.representations.idm.KeysMetadataRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class NamespaceResource {

    private static final Logger LOG = Logger.getLogger(NamespaceResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    ComponentRepository componentRepository;

    @Inject
    KeyManager keyManager;

    @Inject
    ComponentUtil componentUtil;

    @GET
    @Path("/{namespaceId}")
    public Uni<Response> getNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache.withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()))
                .onItem().ifNotNull().transform(entity -> Response.ok().entity(EntityToRepresentation.toRepresentation(entity)).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @PUT
    @Path("/{namespaceId}")
    public Uni<Response> updateNamespace(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull NamespaceRepresentation rep
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().invoke(namespaceEntity -> {
                            namespaceEntity.name = rep.getName();
                            namespaceEntity.description = rep.getDescription();
                        })
                )
                .onItem().ifNotNull().transform(entity -> Response.ok().entity(EntityToRepresentation.toRepresentation(entity)).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @DELETE
    @Path("/{namespaceId}")
    public Uni<Response> deleteNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().call(PanacheEntityBase::delete)
                )
                .onItem().ifNotNull().transform(entity -> Response.status(Response.Status.NO_CONTENT).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }


    @GET
    @Path("/{namespaceId}/keys")
    public Uni<Response> getKeyMetadata(@PathParam("namespaceId") @NotNull String namespaceId) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
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
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
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

}



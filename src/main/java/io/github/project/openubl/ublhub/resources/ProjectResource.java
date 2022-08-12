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

import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.keys.DefaultKeyProviders;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.ComponentMapper;
import io.github.project.openubl.ublhub.mapper.ProjectMapper;
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.security.Permission;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.representations.idm.KeysMetadataRepresentation;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import static org.jboss.resteasy.reactive.RestResponse.Status;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class ProjectResource {

    private static final Logger LOG = Logger.getLogger(ProjectResource.class);

    @Inject
    ProjectMapper projectMapper;

    @Inject
    ComponentMapper componentMapper;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ComponentRepository componentRepository;

    @Inject
    KeyManager keyManager;

    @Inject
    DefaultKeyProviders defaultKeyProviders;

    private ComponentOwner getOwner(String projectId) {
        return ComponentOwner.builder()
                .type(ComponentOwner.OwnerType.project)
                .id(projectId)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create project", description = "Create a project")
    @POST
    @Path("/")
    public Uni<RestResponse<ProjectDto>> createProject(@NotNull @Valid ProjectDto projectDto) {
        Function<ProjectDto, RestResponse<ProjectDto>> successResponse = (dto) -> ResponseBuilder
                .<ProjectDto>create(Status.CREATED)
                .entity(dto)
                .build();
        Function<ProjectEntity, RestResponse<ProjectDto>> errorResponse = entity -> ResponseBuilder
                .<ProjectDto>create(Status.CONFLICT)
                .build();

        Uni<ProjectEntity> createEntityUni = projectMapper.updateEntityFromDto(projectDto, ProjectEntity
                        .builder()
                        .id(UUID.randomUUID().toString())
                        .build()
                )
                .<ProjectEntity>persist()
                .chain(projectEntity -> {
                    ComponentOwner owner = getOwner(projectEntity.id);
                    return defaultKeyProviders.createProviders(owner)
                            .map(unused -> projectEntity);
                });

        Uni<RestResponse<ProjectDto>> createResponseUni = projectRepository.findByName(projectDto.getName())
                .onItem().ifNotNull().transform(errorResponse)
                .onItem().ifNull().switchTo(() -> createEntityUni
                        .map(entity -> projectMapper.toDto(entity))
                        .map(successResponse)
                );

        return Panache.withTransaction(() -> createResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "List projects", description = "List all projects")
    @GET
    @Path("/")
    public Uni<List<ProjectDto>> getProjects() {
        Sort sort = Sort.by(ProjectRepository.SortByField.created.toString(), Sort.Direction.Descending);
        Uni<List<ProjectDto>> responseUni = projectRepository
                .listAll(sort)
                .map(entities -> entities.stream()
                        .map(entity -> projectMapper.toDto(entity))
                        .collect(Collectors.toList())
                );
        return Panache.withTransaction(() -> responseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "Get project", description = "Get one project")
    @GET
    @Path("/{projectId}")
    public Uni<RestResponse<ProjectDto>> getProject(@PathParam("projectId") @NotNull String projectId) {
        Function<ProjectDto, RestResponse<ProjectDto>> successResponse = dto -> ResponseBuilder
                .<ProjectDto>create(Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<ProjectDto>> notFoundResponse = () -> ResponseBuilder
                .<ProjectDto>create(Status.NOT_FOUND)
                .build();

        Uni<RestResponse<ProjectDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transform(projectEntity -> {
                    ProjectDto projectDto = projectMapper.toDto(projectEntity);
                    return successResponse.apply(projectDto);
                })
                .onItem().ifNull().continueWith(notFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update project", description = "Update one project")
    @PUT
    @Path("/{projectId}")
    public Uni<RestResponse<ProjectDto>> updateProject(
            @PathParam("projectId") @NotNull String projectId,
            @NotNull ProjectDto projectDto
    ) {
        Function<ProjectDto, RestResponse<ProjectDto>> successResponse = dto -> ResponseBuilder
                .<ProjectDto>create(Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<ProjectDto>> notFoundResponse = () -> ResponseBuilder
                .<ProjectDto>create(Status.NOT_FOUND)
                .build();

        Uni<RestResponse<ProjectDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> projectMapper
                        .updateEntityFromDto(projectDto, projectEntity).<ProjectEntity>persist()
                        .map(entity -> projectMapper.toDto(entity))
                        .map(successResponse))
                .onItem().ifNull().continueWith(notFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete project", description = "Delete one project")
    @DELETE
    @Path("/{projectId}")
    public Uni<RestResponse<Void>> deleteProject(@PathParam("projectId") @NotNull String projectId) {
        Supplier<RestResponse<Void>> successResponse = () -> ResponseBuilder
                .<Void>create(Status.NO_CONTENT)
                .build();
        Supplier<RestResponse<Void>> notFoundResponse = () -> ResponseBuilder
                .<Void>create(Status.NOT_FOUND)
                .build();

        Uni<RestResponse<Void>> restResponseUni = projectRepository.deleteById(projectId)
                .map(result -> result ? successResponse : notFoundResponse)
                .map(Supplier::get);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Get project keys", description = "List of keys")
    @GET
    @Path("/{projectId}/keys")
    public Uni<RestResponse<KeysMetadataRepresentation>> getKeys(@PathParam("projectId") @NotNull String projectId) {
        ComponentOwner owner = getOwner(projectId);

        Function<KeysMetadataRepresentation, RestResponse<KeysMetadataRepresentation>> successResponse = dto -> ResponseBuilder
                .<KeysMetadataRepresentation>create(Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<KeysMetadataRepresentation>> notFoundResponse = () -> ResponseBuilder
                .<KeysMetadataRepresentation>create(Status.NOT_FOUND)
                .build();

        return projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> keyManager.getKeys(owner)
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
                        .map(successResponse)
                )
                .onItem().ifNull().continueWith(notFoundResponse);
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

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create a project key", description = "Create key")
    @POST
    @Path("/{projectId}/keys")
    public Uni<RestResponse<ComponentDto>> createKey(@PathParam("projectId") @NotNull String projectId, ComponentDto componentDto) {
        componentDto.setId(null);
        componentDto.setParentId(projectId);
        componentDto.setProviderType(KeyProvider.class.getName());

        ComponentOwner owner = getOwner(projectId);

        Function<ComponentDto, RestResponse<ComponentDto>> successResponse = (dto) -> RestResponse.ResponseBuilder
                .<ComponentDto>create(RestResponse.Status.CREATED)
                .entity(dto)
                .build();
        Supplier<RestResponse<ComponentDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<ComponentDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        Uni<RestResponse<ComponentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> componentRepository
                        .addComponentModel(owner, componentMapper.toModel(componentDto))
                        .map(componentModel -> componentMapper.toDto(componentModel, false))
                        .map(successResponse)
                )
                .onItem().ifNull().continueWith(notFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Get project key", description = "Get one key")
    @GET
    @Path("/{projectId}/keys/{componentId}")
    public Uni<RestResponse<ComponentDto>> getKey(@PathParam("projectId") @NotNull String projectId, @PathParam("componentId") String componentId) {
        ComponentOwner owner = getOwner(projectId);

        Function<ComponentDto, RestResponse<ComponentDto>> successResponse = (dto) -> RestResponse.ResponseBuilder
                .<ComponentDto>create(RestResponse.Status.CREATED)
                .entity(dto)
                .build();
        Supplier<RestResponse<ComponentDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<ComponentDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        Uni<RestResponse<ComponentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> componentRepository
                        .getComponent(owner, componentId)
                        .map(model -> componentMapper.toDto(model, false))
                        .map(dto -> dto != null ? successResponse.apply(dto) : notFoundResponse.get())
                ).onItem().ifNull().continueWith(notFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update project key", description = "Update a key")
    @PUT
    @Path("/{projectId}/keys/{componentId}")
    public Uni<RestResponse<ComponentDto>> updateKey(@PathParam("projectId") @NotNull String projectId, @PathParam("componentId") String componentId, ComponentDto componentDto) {
        ComponentOwner owner = getOwner(projectId);

        Function<ComponentDto, RestResponse<ComponentDto>> successResponse = (dto) -> RestResponse.ResponseBuilder
                .<ComponentDto>create(RestResponse.Status.CREATED)
                .entity(dto)
                .build();
        Supplier<RestResponse<ComponentDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<ComponentDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        Uni<RestResponse<ComponentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> componentRepository
                        .getComponent(owner, componentId)
                        .map(model -> componentMapper.updateModelFromDto(componentDto, model, false))
                        .chain(model -> componentRepository.updateComponent(owner, model))
                        .chain(unused -> componentRepository.getComponent(owner, componentId))
                        .map(model -> componentMapper.toDto(model, false))
                        .map(successResponse)
                )
                .onItem().ifNull().continueWith(notFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete a project key", description = "Delete a key")
    @DELETE
    @Path("/{projectId}/keys/{componentId}")
    public Uni<RestResponse<Void>> deleteKey(@PathParam("projectId") @NotNull String projectId, @PathParam("componentId") String componentId) {
        ComponentOwner owner = getOwner(projectId);

        Supplier<RestResponse<Void>> successResponse = () -> RestResponse.ResponseBuilder
                .<Void>create(RestResponse.Status.NO_CONTENT)
                .build();
        Supplier<RestResponse<Void>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<Void>create(RestResponse.Status.NOT_FOUND)
                .build();
        Supplier<RestResponse<Void>> internalServerErrorResponse = () -> RestResponse.ResponseBuilder
                .<Void>create(Status.INTERNAL_SERVER_ERROR)
                .build();

        Uni<RestResponse<Void>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> componentRepository
                        .getComponent(owner, componentId)
                        .chain(model -> componentRepository.removeComponent(owner, model))
                        .map(result -> result ? successResponse.get() : internalServerErrorResponse.get())
                )
                .onItem().ifNull().continueWith(notFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }
}



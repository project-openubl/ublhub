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

import io.github.project.openubl.ublhub.dto.ComponentDto;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.keys.KeyProvider;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.ComponentMapper;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.security.Permission;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.representations.idm.KeysMetadataRepresentation;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.company;
import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.project;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class KeysResource {

    @Context
    UriInfo uriInfo;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    ComponentRepository componentRepository;

    @Inject
    ComponentMapper componentMapper;

    @Inject
    KeyManager keyManager;

    Function<KeysMetadataRepresentation, RestResponse<KeysMetadataRepresentation>> keyMetadataSuccessResponse = dto -> RestResponse.ResponseBuilder
            .<KeysMetadataRepresentation>create(RestResponse.Status.OK)
            .entity(dto)
            .build();
    Supplier<RestResponse<KeysMetadataRepresentation>> keyMetadataNotFoundResponse = () -> RestResponse.ResponseBuilder
            .<KeysMetadataRepresentation>create(RestResponse.Status.NOT_FOUND)
            .build();


    Function<ComponentDto, RestResponse<ComponentDto>> componentDtoCreatedResponse = (dto) -> RestResponse.ResponseBuilder
            .<ComponentDto>create(RestResponse.Status.CREATED)
            .entity(dto)
            .build();
    Function<ComponentDto, RestResponse<ComponentDto>> componentDtoOkResponse = (dto) -> RestResponse.ResponseBuilder
            .<ComponentDto>create(RestResponse.Status.OK)
            .entity(dto)
            .build();
    Supplier<RestResponse<Void>> componentDtoNoContentResponse = () -> RestResponse.ResponseBuilder
            .<Void>create(RestResponse.Status.NO_CONTENT)
            .build();
    Supplier<RestResponse<ComponentDto>> componentDtoNotFoundDtoResponse = () -> RestResponse.ResponseBuilder
            .<ComponentDto>create(RestResponse.Status.NOT_FOUND)
            .build();
    Supplier<RestResponse<Void>> componentDtoNotFoundVoidResponse = () -> RestResponse.ResponseBuilder
            .<Void>create(RestResponse.Status.NOT_FOUND)
            .build();
    Supplier<RestResponse<Void>> componentDtoInternalServerErrorResponse = () -> RestResponse.ResponseBuilder
            .<Void>create(RestResponse.Status.INTERNAL_SERVER_ERROR)
            .build();

    private ComponentOwner getOwnerProject(String projectId) {
        return ComponentOwner.builder()
                .type(project)
                .id(projectId)
                .build();
    }

    private ComponentOwner getOwnerCompany(String companyId) {
        return ComponentOwner.builder()
                .type(company)
                .id(companyId)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get project keys", description = "List of keys")
    @GET
    @Path("/{projectId}/keys")
    public Uni<RestResponse<KeysMetadataRepresentation>> getProjectKeys(
            @PathParam("projectId") @NotNull String projectId
    ) {
        ComponentOwner owner = getOwnerProject(projectId);

        Uni<RestResponse<KeysMetadataRepresentation>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> getKeys(owner)
                        .map(keyMetadataSuccessResponse)
                )
                .onItem().ifNull().continueWith(keyMetadataNotFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get company keys", description = "List of keys")
    @GET
    @Path("/{projectId}/companies/{companyId}/keys")
    public Uni<RestResponse<KeysMetadataRepresentation>> getCompanyKeys(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        ComponentOwner owner = getOwnerCompany(companyId);

        Uni<RestResponse<KeysMetadataRepresentation>> restResponseUni = companyRepository.findById(projectId, companyId)
                .onItem().ifNotNull().transformToUni(projectEntity -> getKeys(owner)
                        .map(keyMetadataSuccessResponse)
                )
                .onItem().ifNull().continueWith(keyMetadataNotFoundResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    private Uni<KeysMetadataRepresentation> getKeys(ComponentOwner owner) {
        return keyManager.getKeys(owner)
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
                });
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
    public Uni<RestResponse<ComponentDto>> createProjectKey(
            @PathParam("projectId") @NotNull String projectId,
            ComponentDto componentDto
    ) {
        componentDto.setId(null);
        componentDto.setParentId(projectId);
        componentDto.setProviderType(KeyProvider.class.getName());

        ComponentOwner owner = getOwnerProject(projectId);

        Uni<RestResponse<ComponentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> createKey(owner, componentDto)
                        .map(componentDtoCreatedResponse)
                )
                .onItem().ifNull().continueWith(componentDtoNotFoundDtoResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create a company key", description = "Create key")
    @POST
    @Path("/{projectId}/companies/{companyId}/keys")
    public Uni<RestResponse<ComponentDto>> createCompanyKey(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId,
            ComponentDto componentDto
    ) {
        componentDto.setId(null);
        componentDto.setParentId(projectId);
        componentDto.setProviderType(KeyProvider.class.getName());

        ComponentOwner owner = getOwnerCompany(companyId);

        Uni<RestResponse<ComponentDto>> restResponseUni = companyRepository.findById(projectId, companyId)
                .onItem().ifNotNull().transformToUni(projectEntity -> createKey(owner, componentDto)
                        .map(componentDtoCreatedResponse)
                )
                .onItem().ifNull().continueWith(componentDtoNotFoundDtoResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    private Uni<ComponentDto> createKey(ComponentOwner owner, ComponentDto componentDto) {
        return componentRepository
                .addComponentModel(owner, componentMapper.toModel(componentDto))
                .map(componentModel -> componentMapper.toDto(componentModel, false));
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get project key", description = "Get one key")
    @GET
    @Path("/{projectId}/keys/{componentId}")
    public Uni<RestResponse<ComponentDto>> getProjectKey(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("componentId") String componentId
    ) {
        ComponentOwner owner = getOwnerProject(projectId);

        Uni<RestResponse<ComponentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> getKey(owner, componentId)
                        .map(dto -> dto != null ? componentDtoOkResponse.apply(dto) : componentDtoNotFoundDtoResponse.get())
                ).onItem().ifNull().continueWith(componentDtoNotFoundDtoResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get company key", description = "Get one key")
    @GET
    @Path("/{projectId}/companies/{companyId}/keys/{componentId}")
    public Uni<RestResponse<ComponentDto>> getCompanyKey(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId,
            @PathParam("componentId") String componentId
    ) {
        ComponentOwner owner = getOwnerCompany(companyId);

        Uni<RestResponse<ComponentDto>> restResponseUni = companyRepository.findById(projectId, companyId)
                .onItem().ifNotNull().transformToUni(projectEntity -> getKey(owner, componentId)
                        .map(dto -> dto != null ? componentDtoOkResponse.apply(dto) : componentDtoNotFoundDtoResponse.get())
                ).onItem().ifNull().continueWith(componentDtoNotFoundDtoResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    private Uni<ComponentDto> getKey(ComponentOwner owner, String componentId) {
        return componentRepository
                .getComponent(owner, componentId)
                .map(model -> componentMapper.toDto(model, false));
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update project key", description = "Update a key")
    @PUT
    @Path("/{projectId}/keys/{componentId}")
    public Uni<RestResponse<ComponentDto>> updateProjectKey(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("componentId") String componentId,
            ComponentDto componentDto
    ) {
        ComponentOwner owner = getOwnerProject(projectId);

        Uni<RestResponse<ComponentDto>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> updateKey(owner, componentId, componentDto)
                        .map(componentDtoOkResponse)
                )
                .onItem().ifNull().continueWith(componentDtoNotFoundDtoResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update company key", description = "Update a key")
    @PUT
    @Path("/{projectId}/companies/{companyId}/keys/{componentId}")
    public Uni<RestResponse<ComponentDto>> updateCompanyKey(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId,
            @PathParam("componentId") String componentId,
            ComponentDto componentDto
    ) {
        ComponentOwner owner = getOwnerCompany(companyId);

        Uni<RestResponse<ComponentDto>> restResponseUni = companyRepository.findById(projectId, companyId)
                .onItem().ifNotNull().transformToUni(projectEntity -> updateKey(owner, componentId, componentDto)
                        .map(componentDtoOkResponse)
                )
                .onItem().ifNull().continueWith(componentDtoNotFoundDtoResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    public Uni<ComponentDto> updateKey(ComponentOwner owner, String componentId, ComponentDto componentDto) {
        return componentRepository
                .getComponent(owner, componentId)
                .map(model -> componentMapper.updateModelFromDto(componentDto, model, false))
                .chain(model -> componentRepository.updateComponent(owner, model))
                .chain(unused -> componentRepository.getComponent(owner, componentId))
                .map(model -> componentMapper.toDto(model, false));
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete a project key", description = "Delete a key")
    @DELETE
    @Path("/{projectId}/keys/{componentId}")
    public Uni<RestResponse<Void>> deleteKey(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("componentId") String componentId
    ) {
        ComponentOwner owner = getOwnerProject(projectId);

        Uni<RestResponse<Void>> restResponseUni = projectRepository.findById(projectId)
                .onItem().ifNotNull().transformToUni(projectEntity -> deleteKey(owner, componentId)
                        .map(result -> result ? componentDtoNoContentResponse.get() : componentDtoInternalServerErrorResponse.get())
                )
                .onItem().ifNull().continueWith(componentDtoNotFoundVoidResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete a company key", description = "Delete a key")
    @DELETE
    @Path("/{projectId}/companies/{companyId}/keys/{componentId}")
    public Uni<RestResponse<Void>> deleteCompanyKey(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId,
            @PathParam("componentId") String componentId
    ) {
        ComponentOwner owner = getOwnerCompany(companyId);

        Uni<RestResponse<Void>> restResponseUni = companyRepository.findById(projectId, companyId)
                .onItem().ifNotNull().transformToUni(projectEntity -> deleteKey(owner, componentId)
                        .map(result -> result ? componentDtoNoContentResponse.get() : componentDtoInternalServerErrorResponse.get())
                )
                .onItem().ifNull().continueWith(componentDtoNotFoundVoidResponse);

        return Panache.withTransaction(() -> restResponseUni);
    }

    public Uni<Boolean> deleteKey(ComponentOwner owner, String componentId) {
        return componentRepository
                .getComponent(owner, componentId)
                .chain(model -> componentRepository.removeComponent(owner, model));
    }
}


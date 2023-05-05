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
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.ComponentMapper;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ComponentRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.security.Permission;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.representations.idm.KeysMetadataRepresentation;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@Transactional
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

    private ComponentOwner getOwnerProject(String project) {
        return ComponentOwner.builder()
                .project(project)
                .build();
    }

    private ComponentOwner getOwnerCompany(String project, String ruc) {
        return ComponentOwner.builder()
                .project(project)
                .ruc(ruc)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get project keys", description = "List of keys")
    @GET
    @Path("/{project}/keys")
    public RestResponse<KeysMetadataRepresentation> getProjectKeys(
            @PathParam("project") @NotNull String project
    ) {
        ProjectEntity projectEntity = projectRepository.findById(project);
        if (projectEntity == null) {
            return keyMetadataNotFoundResponse.get();
        }

        ComponentOwner owner = getOwnerProject(project);
        KeysMetadataRepresentation keys = getKeys(owner);
        return keyMetadataSuccessResponse.apply(keys);
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get company keys", description = "List of keys")
    @GET
    @Path("/{project}/companies/{ruc}/keys")
    public RestResponse<KeysMetadataRepresentation> getCompanyKeys(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc
    ) {
        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return keyMetadataNotFoundResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(project, ruc);
        KeysMetadataRepresentation keys = getKeys(owner);
        return keyMetadataSuccessResponse.apply(keys);
    }

    private KeysMetadataRepresentation getKeys(ComponentOwner owner) {
        List<KeyWrapper> keyWrappers = keyManager.getKeys(owner);

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

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get project components", description = "List of components")
    @GET
    @Path("/{project}/components")
    public RestResponse<List<ComponentDto>> getProjectComponents(
            @PathParam("project") @NotNull String project,
            @QueryParam("parent") String parent,
            @QueryParam("type") String type,
            @QueryParam("name") String name
    ) {
        ProjectEntity projectEntity = projectRepository.findById(project);
        if (projectEntity == null) {
            return RestResponse.ResponseBuilder
                    .<List<ComponentDto>>create(RestResponse.Status.NOT_FOUND)
                    .build();
        }

        ComponentOwner owner = getOwnerProject(project);

        List<ComponentModel> components;
        if (parent == null && type == null) {
            components = componentRepository.getComponents(owner);
        } else if (type == null) {
            components = componentRepository.getComponents(owner, parent);
        } else {
            components = componentRepository.getComponents(owner, parent, type);
        }

        List<ComponentDto> componentDtos = components
                .stream()
                .filter(component -> Objects.isNull(name) || Objects.equals(component.getName(), name))
                .map(model -> componentMapper.toDto(model, false))
                .collect(Collectors.toList());

        return RestResponse.ResponseBuilder
                .<List<ComponentDto>>create(RestResponse.Status.CREATED)
                .entity(componentDtos)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get company components", description = "List of components")
    @GET
    @Path("/{project}/companies/{ruc}/components")
    public RestResponse<List<ComponentDto>> getCompanyComponents(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc,
            @QueryParam("parent") String parent,
            @QueryParam("type") String type,
            @QueryParam("name") String name
    ) {
        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return RestResponse.ResponseBuilder
                    .<List<ComponentDto>>create(RestResponse.Status.NOT_FOUND)
                    .build();
        }

        ComponentOwner owner = getOwnerCompany(project, ruc);

        List<ComponentModel> components;
        if (parent == null && type == null) {
            components = componentRepository.getComponents(owner);
        } else if (type == null) {
            components = componentRepository.getComponents(owner, parent);
        } else {
            components = componentRepository.getComponents(owner, parent, type);
        }

        List<ComponentDto> componentDtos = components.stream()
                .filter(component -> Objects.isNull(name) || Objects.equals(component.getName(), name))
                .map(model -> componentMapper.toDto(model, false))
                .collect(Collectors.toList());

        return RestResponse.ResponseBuilder
                .<List<ComponentDto>>create(RestResponse.Status.CREATED)
                .entity(componentDtos)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create a project component", description = "Create component")
    @POST
    @Path("/{project}/components")
    public RestResponse<ComponentDto> createProjectComponent(
            @PathParam("project") @NotNull String project,
            ComponentDto componentDto
    ) {
        ProjectEntity projectEntity = projectRepository.findById(project);
        if (projectEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        componentDto.setId(null);
        componentDto.setParentId(project);
        componentDto.setProviderType(KeyProvider.class.getName());

        ComponentOwner owner = getOwnerProject(project);
        ComponentDto response = createComponent(owner, componentDto);

        return componentDtoCreatedResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create a company component", description = "Create component")
    @POST
    @Path("/{project}/companies/{ruc}/components")
    public RestResponse<ComponentDto> createCompanyComponent(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc,
            ComponentDto componentDto
    ) {
        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        componentDto.setId(null);
        componentDto.setParentId(project);
        componentDto.setProviderType(KeyProvider.class.getName());

        ComponentOwner owner = getOwnerCompany(project, ruc);
        ComponentDto response = createComponent(owner, componentDto);

        return componentDtoCreatedResponse.apply(response);
    }

    private ComponentDto createComponent(ComponentOwner owner, ComponentDto componentDto) {
        ComponentModel componentModel = componentRepository.addComponentModel(owner, componentMapper.toModel(componentDto));
        return componentMapper.toDto(componentModel, false);
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get project component", description = "Get one component")
    @GET
    @Path("/{project}/components/{componentId}")
    public RestResponse<ComponentDto> getProjectComponent(
            @PathParam("project") @NotNull String project,
            @PathParam("componentId") String componentId
    ) {
        ProjectEntity projectEntity = projectRepository.findById(project);
        if (projectEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerProject(project);
        ComponentDto componentDto = getComponent(owner, componentId);

        return componentDto != null ? componentDtoOkResponse.apply(componentDto) : componentDtoNotFoundDtoResponse.get();
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get company component", description = "Get one component")
    @GET
    @Path("/{project}/companies/{ruc}/components/{componentId}")
    public RestResponse<ComponentDto> getCompanyComponent(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc,
            @PathParam("componentId") String componentId
    ) {
        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(project, ruc);
        ComponentDto componentDto = getComponent(owner, componentId);

        return componentDto != null ? componentDtoOkResponse.apply(componentDto) : componentDtoNotFoundDtoResponse.get();
    }

    private ComponentDto getComponent(ComponentOwner owner, String componentId) {
        ComponentModel model = componentRepository.getComponent(owner, componentId);
        return componentMapper.toDto(model, false);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update project component", description = "Update a component")
    @PUT
    @Path("/{project}/components/{componentId}")
    public RestResponse<ComponentDto> updateProjectComponent(
            @PathParam("project") @NotNull String project,
            @PathParam("componentId") String componentId,
            ComponentDto componentDto
    ) {
        ProjectEntity projectEntity = projectRepository.findById(project);
        if (projectEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerProject(project);
        ComponentDto response = updateComponent(owner, componentId, componentDto);

        return componentDtoOkResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update company component", description = "Update a component")
    @PUT
    @Path("/{project}/companies/{ruc}/components/{componentId}")
    public RestResponse<ComponentDto> updateCompanyComponent(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc,
            @PathParam("componentId") String componentId,
            ComponentDto componentDto
    ) {
        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(project, ruc);
        ComponentDto response = updateComponent(owner, componentId, componentDto);

        return componentDtoOkResponse.apply(response);
    }

    public ComponentDto updateComponent(ComponentOwner owner, String componentId, ComponentDto componentDto) {
        ComponentModel model = componentRepository.getComponent(owner, componentId);
        model = componentMapper.updateModelFromDto(componentDto, model);

        componentRepository.updateComponent(owner, model);
        model = componentRepository.getComponent(owner, componentId);

        return componentMapper.toDto(model, false);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete a project component", description = "Delete a component")
    @DELETE
    @Path("/{project}/components/{componentId}")
    public RestResponse<Void> deleteComponent(
            @PathParam("project") @NotNull String project,
            @PathParam("componentId") String componentId
    ) {
        ProjectEntity projectEntity = projectRepository.findById(project);
        if (projectEntity == null) {
            return componentDtoNotFoundVoidResponse.get();
        }

        ComponentOwner owner = getOwnerProject(project);
        boolean result = deleteComponent(owner, componentId);

        return result ? componentDtoNoContentResponse.get() : componentDtoInternalServerErrorResponse.get();
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete a company component", description = "Delete a component")
    @DELETE
    @Path("/{project}/companies/{ruc}/components/{componentId}")
    public RestResponse<Void> deleteCompanyComponent(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc,
            @PathParam("componentId") String componentId
    ) {
        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return componentDtoNotFoundVoidResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(project, ruc);
        boolean result = deleteComponent(owner, componentId);

        return result ? componentDtoNoContentResponse.get() : componentDtoInternalServerErrorResponse.get();
    }

    public boolean deleteComponent(ComponentOwner owner, String componentId) {
        ComponentModel model = componentRepository.getComponent(owner, componentId);
        return componentRepository.removeComponent(owner, model);
    }
}



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

import com.github.f4b6a3.tsid.TsidFactory;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.company;
import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.project;

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

    @Inject
    TsidFactory tsidFactory;
    
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

    private ComponentOwner getOwnerProject(Long projectId) {
        return ComponentOwner.builder()
                .type(project)
                .id(projectId)
                .build();
    }

    private ComponentOwner getOwnerCompany(Long companyId) {
        return ComponentOwner.builder()
                .type(company)
                .id(companyId)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get project keys", description = "List of keys")
    @GET
    @Path("/{projectId}/keys")
    public RestResponse<KeysMetadataRepresentation> getProjectKeys(
            @PathParam("projectId") @NotNull Long projectId
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return keyMetadataNotFoundResponse.get();
        }

        ComponentOwner owner = getOwnerProject(projectId);
        KeysMetadataRepresentation keys = getKeys(owner);
        return keyMetadataSuccessResponse.apply(keys);
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get company keys", description = "List of keys")
    @GET
    @Path("/{projectId}/companies/{companyId}/keys")
    public RestResponse<KeysMetadataRepresentation> getCompanyKeys(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("companyId") @NotNull Long companyId
    ) {
        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return keyMetadataNotFoundResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(companyId);
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
    @Path("/{projectId}/components")
    public RestResponse<List<ComponentDto>> getProjectComponents(
            @PathParam("projectId") @NotNull Long projectId,
            @QueryParam("parent") Long parent,
            @QueryParam("type") String type,
            @QueryParam("name") String name
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return RestResponse.ResponseBuilder
                    .<List<ComponentDto>>create(RestResponse.Status.NOT_FOUND)
                    .build();
        }

        ComponentOwner owner = getOwnerProject(projectId);

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
    @Path("/{projectId}/companies/{companyId}/components")
    public RestResponse<List<ComponentDto>> getCompanyComponents(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("companyId") @NotNull Long companyId,
            @QueryParam("parent") Long parent,
            @QueryParam("type") String type,
            @QueryParam("name") String name
    ) {
        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return RestResponse.ResponseBuilder
                    .<List<ComponentDto>>create(RestResponse.Status.NOT_FOUND)
                    .build();
        }

        ComponentOwner owner = getOwnerCompany(companyId);

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
    @Path("/{projectId}/components")
    public RestResponse<ComponentDto> createProjectComponent(
            @PathParam("projectId") @NotNull Long projectId,
            ComponentDto componentDto
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        componentDto.setId(null);
        componentDto.setParentId(projectId);
        componentDto.setProviderType(KeyProvider.class.getName());

        ComponentOwner owner = getOwnerProject(projectId);
        ComponentDto response = createComponent(owner, componentDto);

        return componentDtoCreatedResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create a company component", description = "Create component")
    @POST
    @Path("/{projectId}/companies/{companyId}/components")
    public RestResponse<ComponentDto> createCompanyComponent(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("companyId") @NotNull Long companyId,
            ComponentDto componentDto
    ) {
        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        componentDto.setId(null);
        componentDto.setParentId(companyId);
        componentDto.setProviderType(KeyProvider.class.getName());

        ComponentOwner owner = getOwnerCompany(companyId);
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
    @Path("/{projectId}/components/{componentId}")
    public RestResponse<ComponentDto> getProjectComponent(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("componentId") Long componentId
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerProject(projectId);
        ComponentDto componentDto = getComponent(owner, componentId);

        return componentDto != null ? componentDtoOkResponse.apply(componentDto) : componentDtoNotFoundDtoResponse.get();
    }

    @RolesAllowed({Permission.admin, Permission.project_read})
    @Operation(summary = "Get company component", description = "Get one component")
    @GET
    @Path("/{projectId}/companies/{companyId}/components/{componentId}")
    public RestResponse<ComponentDto> getCompanyComponent(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("companyId") @NotNull Long companyId,
            @PathParam("componentId") Long componentId
    ) {
        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(companyId);
        ComponentDto componentDto = getComponent(owner, componentId);

        return componentDto != null ? componentDtoOkResponse.apply(componentDto) : componentDtoNotFoundDtoResponse.get();
    }

    private ComponentDto getComponent(ComponentOwner owner, Long componentId) {
        ComponentModel model = componentRepository.getComponent(owner, componentId);
        return componentMapper.toDto(model, false);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update project component", description = "Update a component")
    @PUT
    @Path("/{projectId}/components/{componentId}")
    public RestResponse<ComponentDto> updateProjectComponent(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("componentId") Long componentId,
            ComponentDto componentDto
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerProject(projectId);
        ComponentDto response = updateComponent(owner, componentId, componentDto);

        return componentDtoOkResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update company component", description = "Update a component")
    @PUT
    @Path("/{projectId}/companies/{companyId}/components/{componentId}")
    public RestResponse<ComponentDto> updateCompanyComponent(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("companyId") @NotNull Long companyId,
            @PathParam("componentId") Long componentId,
            ComponentDto componentDto
    ) {
        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return componentDtoNotFoundDtoResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(companyId);
        ComponentDto response = updateComponent(owner, componentId, componentDto);

        return componentDtoOkResponse.apply(response);
    }

    public ComponentDto updateComponent(ComponentOwner owner, Long componentId, ComponentDto componentDto) {
        ComponentModel model = componentRepository.getComponent(owner, componentId);
        model = componentMapper.updateModelFromDto(componentDto, model);

        componentRepository.updateComponent(owner, model);
        model = componentRepository.getComponent(owner, componentId);

        return componentMapper.toDto(model, false);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete a project component", description = "Delete a component")
    @DELETE
    @Path("/{projectId}/components/{componentId}")
    public RestResponse<Void> deleteComponent(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("componentId") Long componentId
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return componentDtoNotFoundVoidResponse.get();
        }

        ComponentOwner owner = getOwnerProject(projectId);
        boolean result = deleteComponent(owner, componentId);

        return result ? componentDtoNoContentResponse.get() : componentDtoInternalServerErrorResponse.get();
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete a company component", description = "Delete a component")
    @DELETE
    @Path("/{projectId}/companies/{companyId}/components/{componentId}")
    public RestResponse<Void> deleteCompanyComponent(
            @PathParam("projectId") @NotNull Long projectId,
            @PathParam("companyId") @NotNull Long companyId,
            @PathParam("componentId") Long componentId
    ) {
        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return componentDtoNotFoundVoidResponse.get();
        }

        ComponentOwner owner = getOwnerCompany(companyId);
        boolean result = deleteComponent(owner, componentId);

        return result ? componentDtoNoContentResponse.get() : componentDtoInternalServerErrorResponse.get();
    }

    public boolean deleteComponent(ComponentOwner owner, Long componentId) {
        ComponentModel model = componentRepository.getComponent(owner, componentId);
        return componentRepository.removeComponent(owner, model);
    }
}



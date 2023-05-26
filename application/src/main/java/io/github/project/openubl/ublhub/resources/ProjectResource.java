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

import io.github.project.openubl.ublhub.dto.CheckCompanyDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.keys.DefaultKeyProviders;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.ProjectMapper;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.security.Role;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import static org.jboss.resteasy.reactive.RestResponse.Status;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class ProjectResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    ProjectMapper projectMapper;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    DefaultKeyProviders defaultKeyProviders;

    private ComponentOwner getOwner(String projectName) {
        return ComponentOwner.builder()
                .project(projectName)
                .build();
    }

    @Operation(summary = "Verify project name", description = "Verify whether or not the name the project has already been taken")
    @POST
    @Path("/check-name")
    public RestResponse<String> checkProjectName(@NotNull @Valid CheckCompanyDto checkCompanyDto) {
        Supplier<RestResponse<String>> successResponse = () -> ResponseBuilder
                .<String>create(Status.OK)
                .entity(checkCompanyDto.getName() + " available")
                .build();
        Supplier<RestResponse<String>> conflictResponse = () -> ResponseBuilder
                .<String>create(Status.CONFLICT)
                .entity("Name is already taken")
                .build();
        Supplier<RestResponse<String>> badRequestResponse = () -> ResponseBuilder
                .<String>create(Status.BAD_REQUEST)
                .entity("Name does not comply with pattern " + ProjectEntity.NAME_PATTERN)
                .build();

        Pattern namePattern = Pattern.compile(ProjectEntity.NAME_PATTERN);
        if (!namePattern.matcher(checkCompanyDto.getName()).matches()) {
            return badRequestResponse.get();
        }

        ProjectEntity projectEntity = projectRepository.findById(checkCompanyDto.getName());
        if (projectEntity == null) {
            return successResponse.get();
        }

        return conflictResponse.get();
    }

    @Operation(summary = "Create project", description = "Create a project")
    @POST
    @Path("/")
    public RestResponse<ProjectDto> createProject(@NotNull @Valid ProjectDto projectDto) {
        Function<ProjectDto, RestResponse<ProjectDto>> successResponse = (dto) -> ResponseBuilder
                .<ProjectDto>create(Status.CREATED)
                .entity(dto)
                .build();
        Function<ProjectEntity, RestResponse<ProjectDto>> errorResponse = entity -> ResponseBuilder
                .<ProjectDto>create(Status.CONFLICT)
                .build();

        ProjectEntity projectEntity = projectRepository.findById(projectDto.getName());
        if (projectEntity != null) {
            return errorResponse.apply(projectEntity);
        }

        // Create Project and its owner
        projectEntity = projectMapper.updateEntityFromDto(projectDto, ProjectEntity.builder().build());
        projectEntity.persist();

        String username = securityIdentity.getPrincipal().getName();
        projectEntity.setProjectOwner(username);

        // Create default keys
        ComponentOwner owner = getOwner(projectEntity.getName());
        defaultKeyProviders.createProviders(owner);

        return successResponse.apply(projectMapper.toDto(projectEntity));
    }

    @Operation(summary = "List projects", description = "List all projects")
    @GET
    @Path("/")
    public List<ProjectDto> getProjects() {
        String username = securityIdentity.getPrincipal().getName();

        Sort sort = Sort.by(ProjectRepository.SortByField.name.toString(), Sort.Direction.Descending);
        return projectRepository.listAll(username, sort)
                .stream()
                .map(entity -> projectMapper.toDto(entity))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get project", description = "Get one project")
    @GET
    @Path("/{projectName}")
    public RestResponse<ProjectDto> getProject(@PathParam("projectName") @NotNull String projectName) {
        Function<ProjectDto, RestResponse<ProjectDto>> successResponse = dto -> ResponseBuilder
                .<ProjectDto>create(Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<ProjectDto>> notFoundResponse = () -> ResponseBuilder
                .<ProjectDto>create(Status.NOT_FOUND)
                .build();

        String username = securityIdentity.getPrincipal().getName();
        ProjectEntity projectEntity = projectRepository.findById(projectName);
        if (projectEntity == null || !projectEntity.hasAnyRole(username)) {
            return notFoundResponse.get();
        }

        ProjectDto projectDto = projectMapper.toDto(projectEntity);
        return successResponse.apply(projectDto);
    }

    @Operation(summary = "Update project", description = "Update one project")
    @PUT
    @Path("/{projectName}")
    public RestResponse<ProjectDto> updateProject(
            @PathParam("projectName") @NotNull String projectName,
            @NotNull ProjectDto projectDto
    ) {
        Function<ProjectDto, RestResponse<ProjectDto>> successResponse = dto -> ResponseBuilder
                .<ProjectDto>create(Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<ProjectDto>> notFoundResponse = () -> ResponseBuilder
                .<ProjectDto>create(Status.NOT_FOUND)
                .build();

        String username = securityIdentity.getPrincipal().getName();
        ProjectEntity projectEntity = projectRepository.findById(projectName);
        if (projectEntity == null || !projectEntity.hasAnyRole(username, Role.owner)) {
            return notFoundResponse.get();
        }

        projectMapper.updateEntityFromDto(projectDto, projectEntity);

        projectEntity.setName(projectName); // To prevent changing PK
        projectEntity.persist();

        return successResponse.apply(projectMapper.toDto(projectEntity));
    }

    @Operation(summary = "Delete project", description = "Delete one project")
    @DELETE
    @Path("/{projectName}")
    public RestResponse<Void> deleteProject(@PathParam("projectName") @NotNull String projectName) {
        Supplier<RestResponse<Void>> successResponse = () -> ResponseBuilder
                .<Void>create(Status.NO_CONTENT)
                .build();
        Supplier<RestResponse<Void>> notFoundResponse = () -> ResponseBuilder
                .<Void>create(Status.NOT_FOUND)
                .build();

        String username = securityIdentity.getPrincipal().getName();
        ProjectEntity projectEntity = projectRepository.findById(projectName);
        if (projectEntity == null || !projectEntity.hasAnyRole(username, Role.owner)) {
            return notFoundResponse.get();
        }

        boolean result = projectRepository.deleteById(projectName);
        Supplier<RestResponse<Void>> response = result ? successResponse : notFoundResponse;
        return response.get();
    }

}



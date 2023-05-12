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

import com.github.f4b6a3.tsid.TsidFactory;
import io.github.project.openubl.ublhub.dto.CheckCompanyDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.keys.DefaultKeyProviders;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.ProjectMapper;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.security.Permission;
import io.quarkus.panache.common.Sort;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
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
    ProjectMapper projectMapper;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    DefaultKeyProviders defaultKeyProviders;

    @Inject
    TsidFactory tsidFactory;

    private ComponentOwner getOwner(String projectName) {
        return ComponentOwner.builder()
                .project(projectName)
                .build();
    }

    @Operation(summary = "List projects", description = "List all projects")
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

        ProjectEntity projectEntity = projectRepository.findById(checkCompanyDto.getName());
        if (projectEntity == null) {
            return successResponse.get();
        }

        return conflictResponse.get();
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
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

        projectEntity = projectMapper.updateEntityFromDto(projectDto, ProjectEntity.builder().build());
        projectEntity.persist();

        ComponentOwner owner = getOwner(projectEntity.getName());
        defaultKeyProviders.createProviders(owner);

        return successResponse.apply(projectMapper.toDto(projectEntity));
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "List projects", description = "List all projects")
    @GET
    @Path("/")
    public List<ProjectDto> getProjects() {
        Sort sort = Sort.by(ProjectRepository.SortByField.name.toString(), Sort.Direction.Descending);
        return projectRepository.listAll(sort)
                .stream()
                .map(entity -> projectMapper.toDto(entity))
                .collect(Collectors.toList());
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
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

        ProjectEntity projectEntity = projectRepository.findById(projectName);
        if (projectEntity == null) {
            return notFoundResponse.get();
        }

        ProjectDto projectDto = projectMapper.toDto(projectEntity);
        return successResponse.apply(projectDto);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
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

        ProjectEntity projectEntity = projectRepository.findById(projectName);
        if (projectEntity == null) {
            return notFoundResponse.get();
        }

        projectMapper.updateEntityFromDto(projectDto, projectEntity);

        projectEntity.setName(projectName); // To prevent changing PK
        projectEntity.persist();

        return successResponse.apply(projectMapper.toDto(projectEntity));
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
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

        boolean result = projectRepository.deleteById(projectName);
        Supplier<RestResponse<Void>> response = result ? successResponse : notFoundResponse;
        return response.get();
    }

}



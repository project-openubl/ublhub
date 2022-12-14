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

import io.github.project.openubl.ublhub.dto.CompanyDto;
import io.github.project.openubl.ublhub.keys.DefaultKeyProviders;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.mapper.CompanyMapper;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.security.Permission;
import io.quarkus.panache.common.Sort;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.project.openubl.ublhub.keys.component.ComponentOwner.OwnerType.company;

@Path("/projects")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class CompanyResource {

    @Context
    UriInfo uriInfo;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyMapper companyMapper;

    @Inject
    DefaultKeyProviders defaultKeyProviders;

    private ComponentOwner getOwner(String companyId) {
        return ComponentOwner.builder()
                .type(company)
                .id(companyId)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "Get company", description = "Get one company")
    @GET
    @Path("/{projectId}/companies/{companyId}")
    public RestResponse<CompanyDto> getCompany(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        Function<CompanyDto, RestResponse<CompanyDto>> successResponse = dto -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<CompanyDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return notFoundResponse.get();
        }

        CompanyDto companyDto = companyMapper.toDto(companyEntity);
        return successResponse.apply(companyDto);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create company", description = "Create a company")
    @POST
    @Path("/{projectId}/companies")
    public RestResponse<CompanyDto> createCompany(
            @PathParam("projectId") @NotNull String projectId,
            @NotNull @Valid CompanyDto companyDto
    ) {
        Function<CompanyDto, RestResponse<CompanyDto>> successResponse = (dto) -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.CREATED)
                .entity(dto)
                .build();
        Function<CompanyEntity, RestResponse<CompanyDto>> conflictResponse = entity -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.CONFLICT)
                .build();
        Supplier<RestResponse<CompanyDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return notFoundResponse.get();
        }

        CompanyEntity companyEntity = companyRepository.findByRuc(projectEntity, companyDto.getRuc());
        if (companyEntity != null) {
            return conflictResponse.apply(companyEntity);
        }

        companyEntity = companyMapper.updateEntityFromDto(companyDto, CompanyEntity.builder()
                .projectId(projectEntity.getId())
                .id(UUID.randomUUID().toString())
                .build()
        );
        companyEntity.persist();

        ComponentOwner owner = getOwner(companyEntity.getId());
        defaultKeyProviders.createProviders(owner);

        CompanyDto response = companyMapper.toDto(companyEntity);
        return successResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update company", description = "Update one company")
    @PUT
    @Path("/{projectId}/companies/{companyId}")
    public RestResponse<CompanyDto> updateCompany(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId,
            @NotNull CompanyDto companyDto
    ) {
        Function<CompanyDto, RestResponse<CompanyDto>> successResponse = dto -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<CompanyDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        CompanyEntity companyEntity = companyRepository.findById(projectId, companyId);
        if (companyEntity == null) {
            return notFoundResponse.get();
        }

        companyMapper.updateEntityFromDto(companyDto, companyEntity);
        companyEntity.persist();

        CompanyDto response = companyMapper.toDto(companyEntity);
        return successResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete company", description = "Delete one company")
    @DELETE
    @Path("/{projectId}/companies/{companyId}")
    public RestResponse<Void> deleteCompany(
            @PathParam("projectId") @NotNull String projectId,
            @PathParam("companyId") @NotNull String companyId
    ) {
        Supplier<RestResponse<Void>> successResponse = () -> RestResponse.ResponseBuilder
                .<Void>create(RestResponse.Status.NO_CONTENT)
                .build();
        Supplier<RestResponse<Void>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<Void>create(RestResponse.Status.NOT_FOUND)
                .build();

        boolean result = companyRepository.deleteByProjectIdAndId(projectId, companyId);
        return result ? successResponse.get() : notFoundResponse.get();
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "List companies", description = "List all companies")
    @GET
    @Path("/{projectId}/companies")
    public RestResponse<List<CompanyDto>> getCompanies(@PathParam("projectId") @NotNull String projectId) {
        Function<List<CompanyDto>, RestResponse<List<CompanyDto>>> successResponse = dtos -> RestResponse.ResponseBuilder
                .<List<CompanyDto>>create(RestResponse.Status.OK)
                .entity(dtos)
                .build();

        Supplier<RestResponse<List<CompanyDto>>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<List<CompanyDto>>create(RestResponse.Status.NOT_FOUND)
                .build();

        Sort sort = Sort.by(CompanyRepository.SortByField.created.toString(), Sort.Direction.Descending);

        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            return notFoundResponse.get();
        }

        List<CompanyDto> companyDtos = companyRepository.listAll(projectEntity, sort)
                .stream()
                .map(entity -> companyMapper.toDto(entity))
                .collect(Collectors.toList());
        return successResponse.apply(companyDtos);
    }

}



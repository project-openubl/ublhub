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
import io.github.project.openubl.ublhub.dto.CompanyDto;
import io.github.project.openubl.ublhub.files.FilesManager;
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

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    @Inject
    TsidFactory tsidFactory;

    @Inject
    FilesManager filesManager;

    private ComponentOwner getOwner(String project, String ruc) {
        return ComponentOwner.builder()
                .project(project)
                .ruc(ruc)
                .build();
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "Get company", description = "Get one company")
    @GET
    @Path("/{project}/companies/{ruc}")
    public RestResponse<CompanyDto> getCompany(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc
    ) {
        Function<CompanyDto, RestResponse<CompanyDto>> successResponse = dto -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<CompanyDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return notFoundResponse.get();
        }

        CompanyDto companyDto = companyMapper.toDto(companyEntity);
        return successResponse.apply(companyDto);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Create company", description = "Create a company")
    @POST
    @Path("/{project}/companies")
    public RestResponse<CompanyDto> createCompany(
            @PathParam("project") @NotNull String project,
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

        ProjectEntity projectEntity = projectRepository.findById(project);
        if (projectEntity == null) {
            return notFoundResponse.get();
        }

        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(projectEntity.getName(), companyDto.getRuc()));
        if (companyEntity != null) {
            return conflictResponse.apply(companyEntity);
        }

        // Save logo
        String logoFileId = null;
        if (companyDto.getLogo() != null && !companyDto.getLogo().isEmpty()) {
            String logoBase64 = companyDto.getLogo().trim().replaceFirst("data[:]image[/]([a-z])+;base64,", "");
            byte[] logoBytes = Base64.getDecoder().decode(logoBase64);
            logoFileId = filesManager.createFile(logoBytes, true);
        }

        companyEntity = companyMapper.updateEntityFromDto(companyDto, CompanyEntity.builder()
                .id(new CompanyEntity.CompanyId(projectEntity.getName(), companyDto.getRuc()))
                .logoFileId(logoFileId)
                .build()
        );
        companyEntity.persist();

        ComponentOwner owner = getOwner(companyEntity.getId().getProject(), companyEntity.getId().getRuc());
        defaultKeyProviders.createProviders(owner);

        CompanyDto response = companyMapper.toDto(companyEntity);
        return successResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Update company", description = "Update one company")
    @PUT
    @Path("/{project}/companies/{ruc}")
    public RestResponse<CompanyDto> updateCompany(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc,
            @NotNull CompanyDto companyDto
    ) {
        Function<CompanyDto, RestResponse<CompanyDto>> successResponse = dto -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.OK)
                .entity(dto)
                .build();
        Supplier<RestResponse<CompanyDto>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<CompanyDto>create(RestResponse.Status.NOT_FOUND)
                .build();

        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return notFoundResponse.get();
        }

        // Save logo
        if (companyDto.getLogo() != null && !companyDto.getLogo().isEmpty()) {
            String logoBase64 = companyDto.getLogo().trim().replaceFirst("data:image/([a-z])+;base64,", "");
            byte[] logoBytes = Base64.getDecoder().decode(logoBase64);
            String logoFileId = filesManager.createFile(logoBytes, true);

            companyEntity.setLogoFileId(logoFileId);
        }

        companyMapper.updateEntityFromDto(companyDto, companyEntity);
        companyEntity.persist();

        CompanyDto response = companyMapper.toDto(companyEntity);
        return successResponse.apply(response);
    }

    @RolesAllowed({Permission.admin, Permission.project_write})
    @Operation(summary = "Delete company", description = "Delete one company")
    @DELETE
    @Path("/{project}/companies/{ruc}")
    public RestResponse<Void> deleteCompany(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc
    ) {
        Supplier<RestResponse<Void>> successResponse = () -> RestResponse.ResponseBuilder
                .<Void>create(RestResponse.Status.NO_CONTENT)
                .build();
        Supplier<RestResponse<Void>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<Void>create(RestResponse.Status.NOT_FOUND)
                .build();

        boolean result = companyRepository.deleteById(new CompanyEntity.CompanyId(project, ruc));
        return result ? successResponse.get() : notFoundResponse.get();
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "Get company's logo", description = "Get one company's logo")
    @Produces({"image/jpeg", "image/png"})
    @GET
    @Path("/{project}/companies/{ruc}/logo")
    public RestResponse<String> getCompanyBase64Logo(
            @PathParam("project") @NotNull String project,
            @PathParam("ruc") @NotNull String ruc
    ) {
        Function<byte[], RestResponse<String>> successResponse = bytes -> RestResponse.ResponseBuilder
                .<String>create(RestResponse.Status.OK)
                .entity(Base64.getEncoder().withoutPadding().encodeToString(bytes))
                .build();
        Supplier<RestResponse<String>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<String>create(RestResponse.Status.NOT_FOUND)
                .build();

        CompanyEntity companyEntity = companyRepository.findById(new CompanyEntity.CompanyId(project, ruc));
        if (companyEntity == null) {
            return notFoundResponse.get();
        }
        if (companyEntity.getLogoFileId() == null) {
            return RestResponse.ResponseBuilder.<String>ok().build();
        }

        byte[] logoBytes = filesManager.getFileAsBytesAfterUnzip(companyEntity.getLogoFileId());
        return successResponse.apply(logoBytes);
    }

    @RolesAllowed({Permission.admin, Permission.project_write, Permission.project_read})
    @Operation(summary = "List companies", description = "List all companies")
    @GET
    @Path("/{project}/companies")
    public RestResponse<List<CompanyDto>> getCompanies(@PathParam("project") @NotNull String project) {
        Function<List<CompanyDto>, RestResponse<List<CompanyDto>>> successResponse = dtos -> RestResponse.ResponseBuilder
                .<List<CompanyDto>>create(RestResponse.Status.OK)
                .entity(dtos)
                .build();

        Supplier<RestResponse<List<CompanyDto>>> notFoundResponse = () -> RestResponse.ResponseBuilder
                .<List<CompanyDto>>create(RestResponse.Status.NOT_FOUND)
                .build();

        Sort sort = Sort.by(CompanyRepository.SortByField.name.toString(), Sort.Direction.Descending);

        ProjectEntity projectEntity = projectRepository.findById(project);
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



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
package io.github.project.openubl.ublhub.dto;

import io.github.project.openubl.ublhub.security.validators.ValidRole;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@RegisterForReflection
public class UserDto {

    private String id;
    private String fullName;

    @NotNull
    @Size(min = 3, max = 250)
    private String username;

    @NotNull
    @Size(min = 3, max = 250)
    private String password;

    @ValidRole
    @Valid
    @NotEmpty
    private Set<String> permissions;

}

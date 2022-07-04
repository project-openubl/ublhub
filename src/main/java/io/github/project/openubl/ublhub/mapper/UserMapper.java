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
package io.github.project.openubl.ublhub.mapper;

import io.github.project.openubl.ublhub.dto.UserDto;
import io.github.project.openubl.ublhub.models.jpa.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    @Mapping(source = "permissions", target = "permissions")
    @Mapping(source = "password", target = "password", ignore = true)
    UserDto toDto(UserEntity entity);

    default Set<String> permissions(String permissions) {
        return Arrays.stream(permissions.split(","))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}

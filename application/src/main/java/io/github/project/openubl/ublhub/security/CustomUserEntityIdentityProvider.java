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
package io.github.project.openubl.ublhub.security;

import io.github.project.openubl.ublhub.models.jpa.entities.UserEntity;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.jpa.runtime.JpaIdentityProvider;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

@Alternative
@Priority(1)
@ApplicationScoped
public class CustomUserEntityIdentityProvider extends JpaIdentityProvider {

    @Inject
    PgPool client;

    @Override
    public SecurityIdentity authenticate(EntityManager em, UsernamePasswordAuthenticationRequest request) {
        UserEntity authenticatedUser = client.preparedQuery("SELECT id, username, password, permissions, full_name, version FROM app_user WHERE username = $1")
                .execute(Tuple.of(request.getUsername()))
                .onFailure().transform(AuthenticationFailedException::new)
                .onItem().ifNotNull().<UserEntity>transformToUni((rs, uniEmitter) -> {
                    if (rs.rowCount() != 1) {
                        uniEmitter.fail(new AuthenticationFailedException());
                    } else {
                        Row row = rs.iterator().next();

                        Long id = row.get(Long.class, "id");
                        UserEntity userEntity = UserEntity.builder()
                                .username(row.get(String.class, "username"))
                                .password(row.get(String.class, "password"))
                                .permissions(row.get(String.class, "permissions"))
                                .fullName(row.get(String.class, "full_name"))
                                .version(row.get(Integer.class, "version"))
                                .build();
                        userEntity.id = id;

                        uniEmitter.complete(userEntity);
                    }
                })
                .await().asOptional().atMost(Duration.ofSeconds(10)).orElse(null);

        if (authenticatedUser != null) {
            String[] roles = authenticatedUser.getPermissions().split(",");
            return checkPassword(getMcfPassword(authenticatedUser.getPassword()), request)
                    .addRoles(new HashSet<>(Arrays.asList(roles)))
                    .build();
        } else {
            throw new AuthenticationFailedException();
        }
    }

}

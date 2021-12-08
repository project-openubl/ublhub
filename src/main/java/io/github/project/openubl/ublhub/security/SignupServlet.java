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

import io.github.project.openubl.ublhub.idm.BasicUserRepresentation;
import io.github.project.openubl.ublhub.models.jpa.entities.BasicUserEntity;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@WebServlet("/j_security_signup")
public class SignupServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SignupServlet.class);

    @Inject
    UserTransaction tx;

    @Inject
    Validator validator;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("j_username");
        String password1 = req.getParameter("j_password1");
        String password2 = req.getParameter("j_password2");

        if (Objects.isNull(username) ||
                Objects.isNull(password1) ||
                Objects.isNull(password2) ||
                !Objects.equals(password1, password2)
        ) {
            resp.sendRedirect("signup-error.html");
            return;
        }

        BasicUserRepresentation userRepresentation = new BasicUserRepresentation();
        userRepresentation.setUsername(username);
        userRepresentation.setPassword(password1);
        userRepresentation.setPermissions(new HashSet<>(List.of(Permission.admin)));

        Set<ConstraintViolation<BasicUserRepresentation>> violations = validator.validate(userRepresentation);
        if (violations.isEmpty()) {
            BasicUserEntity.count().subscribeAsCompletionStage().whenComplete((currentNumberOfUsers, throwable) -> {
                try {
                    tx.begin();
                    if (currentNumberOfUsers == 0) {
                        BasicUserEntity.add(userRepresentation);
                    }
                    tx.commit();
                } catch (NotSupportedException | HeuristicRollbackException | HeuristicMixedException | RollbackException | SystemException e) {
                    try {
                        tx.rollback();
                    } catch (SystemException se) {
                        LOGGER.error(se);
                    }
                }
            });
        } else {
            resp.sendRedirect("signup-error.html");
        }
    }
}

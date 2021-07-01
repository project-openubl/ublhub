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
package io.github.project.openubl.xsender.resources.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static io.restassured.RestAssured.given;

public abstract class BaseKeycloakTest {

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String oidcAuthServerUrl;

    protected String getAccessToken(String userName) {
        return given()
                .relaxedHTTPSValidation()
                .auth().preemptive().basic("xsender", "secret")
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("username", userName)
                .formParam("password", userName)
                .when()
                .post(oidcAuthServerUrl + "/protocol/openid-connect/token")
                .then().extract().path("access_token").toString();
    }

}

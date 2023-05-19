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
package io.github.project.openubl.ublhub;

import io.restassured.specification.RequestSpecification;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.flywaydb.core.Flyway;

import java.io.StringReader;

import static io.restassured.RestAssured.given;

public abstract class AbstractBaseTest {

    protected void cleanDB() {
        Flyway flyway = flyway();
        flyway.clean();
        flyway.migrate();
    }

    private Flyway flyway() {
        Config config = ConfigProvider.getConfig();
        String username = config.getValue("quarkus.datasource.username", String.class);
        String password = config.getValue("quarkus.datasource.password", String.class);

        String jdbcUrl = config.getValue("quarkus.datasource.jdbc.url", String.class);

        // Flyway
        return Flyway.configure()
                .cleanDisabled(false)
                .dataSource(jdbcUrl, username, password)
                .connectRetries(120)
                .load();
    }

    protected RequestSpecification givenAuth(String username) {
        Config config = ConfigProvider.getConfig();
        Boolean isAuthEnabled = config.getValue("openubl.auth.enabled", Boolean.class);
        if (isAuthEnabled) {
            String accessToken = getAccessToken(username);
            return given().auth().oauth2(accessToken);
        } else {
            return given();
        }
    }

    private String getAccessToken(String userName) {
        Config config = ConfigProvider.getConfig();
        String oidcAuthServerUrl = config.getValue("quarkus.oidc.auth-server-url", String.class);
        String oidcAuthClientId = config.getValue("quarkus.oidc.client-id", String.class);
        String oidcAuthSecret = config.getValue("quarkus.oidc.credentials.secret", String.class);

        return given()
                .relaxedHTTPSValidation()
                .auth().preemptive().basic(oidcAuthClientId, oidcAuthSecret)
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("username", userName)
                .formParam("password", userName)
                .when()
                .post(oidcAuthServerUrl + "/protocol/openid-connect/token")
                .then().extract().path("access_token").toString();
    }

    public static javax.json.JsonObject toJavax(Object object) {
        io.vertx.core.json.JsonObject vertxJson = io.vertx.core.json.JsonObject.mapFrom(object);

        javax.json.JsonReader jsonReader = javax.json.Json.createReader(new StringReader(vertxJson.toString()));
        javax.json.JsonObject javaxJson = jsonReader.readObject();
        jsonReader.close();

        return javaxJson;
    }
}

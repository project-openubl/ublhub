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
package io.github.project.openubl.ublhub.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.ublhub.AbstractBaseTest;
import io.github.project.openubl.ublhub.ProfileManager;
import io.github.project.openubl.ublhub.idm.DocumentRepresentation;
import io.github.project.openubl.ublhub.resources.DocumentResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.websocket.*;
import java.io.File;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Disabled
@QuarkusTest
@TestProfile(ProfileManager.class)
public class DocumentsEndpointTest extends AbstractBaseTest {

    static final int TIMEOUT = 60;
    static final String nsId = "1";

    @TestHTTPResource("/namespaces/" + nsId + "/documents")
    URI uri;

    @Inject
    ObjectMapper objectMapper;

    static final LinkedBlockingDeque<String> WS_MESSAGES = new LinkedBlockingDeque<>();

    @Override
    public Class<?> getTestClass() {
        return DocumentsEndpointTest.class;
    }

    @ClientEndpoint
    public static class Client {
        @OnOpen
        public void open(Session session) {
            session.getAsyncRemote().sendText("CONNECT"); // Instead of "CONNECT" we need to send a token or BASIC auth once the websocket allows authentication
        }

        @OnMessage
        void message(String msg) {
            WS_MESSAGES.add(msg);
        }
    }

    @BeforeEach
    public void beforeEach() {
        WS_MESSAGES.clear();
    }

    @Test
    public void testWebsocketChat() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            // Given
            URI fileURI = DocumentsEndpointTest.class.getClassLoader().getResource("xml/voided-document_12345678912.xml").toURI();
            File file = new File(fileURI);

            // When
            DocumentRepresentation response = given()
                    .accept(ContentType.JSON)
                    .multiPart("file", file, "application/xml")
                    .when()
                    .post("/api/namespaces/" + nsId + "/documents/upload")
                    .then()
                    .statusCode(200)
                    .body("inProgress", is(true))
                    .extract().body().as(DocumentRepresentation.class);

            // Then
            await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
                DocumentRepresentation watchResponse = given()
                        .contentType(ContentType.JSON)
                        .when()

                        .get("/api/namespaces/" + nsId + "/documents/" + response.getId())
                        .then()
                        .statusCode(200)
                        .extract().body().as(DocumentRepresentation.class);
                return !watchResponse.isInProgress();
            });

            DocumentRepresentation wsEvent = objectMapper.readValue(WS_MESSAGES.getLast(), DocumentRepresentation.class);

            assertEquals(3, WS_MESSAGES.size());
            assertFalse(wsEvent.isInProgress());
            assertEquals(wsEvent.getFileContent().getDocumentID(), "RA-20200328-1");
            assertEquals(wsEvent.getSunat().getStatus(), "ACEPTADO");
        }
    }
}

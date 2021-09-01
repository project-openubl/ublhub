package io.github.project.openubl.xsender.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.openubl.xsender.BaseAuthTest;
import io.github.project.openubl.xsender.ProfileManager;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.resources.DocumentResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.websocket.*;
import java.io.File;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
@TestProfile(ProfileManager.class)
@TestHTTPEndpoint(DocumentResource.class)
public class DocumentsEndpointTest extends BaseAuthTest {

    static final int TIMEOUT = 60;
    static final String nsId = "1";

    @TestHTTPResource("/namespaces/" + nsId + "/documents")
    URI uri;

    @Inject
    ObjectMapper objectMapper;

    static final LinkedBlockingDeque<String> WS_MESSAGES = new LinkedBlockingDeque<>();

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
            DocumentRepresentation response = givenAuth("alice")
                    .accept(ContentType.JSON)
                    .multiPart("file", file, "application/xml")
                    .when()
                    .post("/" + nsId + "/documents/upload")
                    .then()
                    .statusCode(200)
                    .body("inProgress", is(true))
                    .extract().body().as(DocumentRepresentation.class);

            // Then
            await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
                DocumentRepresentation watchResponse = givenAuth("alice")
                        .contentType(ContentType.JSON)
                        .when()

                        .get("/" + nsId + "/documents/" + response.getId())
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

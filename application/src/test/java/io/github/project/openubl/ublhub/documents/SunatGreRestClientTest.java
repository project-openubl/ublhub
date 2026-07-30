package io.github.project.openubl.ublhub.documents;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SunatGreRestClientTest {

    private HttpServer server;
    private SunatGreRestClient client;
    private SunatEntity config;
    private AtomicInteger ticketChecks;

    @BeforeEach
    void setUp() throws IOException {
        ticketChecks = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/oauth/client-id", exchange ->
                json(exchange, 200, "{\"access_token\":\"test-token\",\"expires_in\":3600}"));
        server.createContext("/oauth-error/client-id", exchange ->
                json(exchange, 401,
                        "{\"error\":\"invalid_grant\","
                                + "\"error_description\":\"Credenciales OAuth invalidas\"}"));
        server.createContext("/gre/20100066603-09-T001-00000003", exchange -> {
            assertEquals("Bearer test-token", exchange.getRequestHeaders().getFirst("Authorization"));
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(body.contains("20100066603-09-T001-00000003.zip"));
            assertTrue(body.contains("\"hashZip\""));
            json(exchange, 200,
                    "{\"numTicket\":\"550e8400-e29b-41d4-a716-446655440026\"}");
        });
        server.createContext("/gre-error/20100066603-09-T001-00000003", exchange ->
                json(exchange, 422,
                        "{\"cod\":\"422\",\"msg\":\"Unprocessable Entity\","
                                + "\"errors\":[{\"codError\":\"3210\","
                                + "\"desError\":\"Firma digital invalida\"}]}"));
        server.createContext("/gre/envios/550e8400-e29b-41d4-a716-446655440026", exchange -> {
            if (ticketChecks.getAndIncrement() == 0) {
                json(exchange, 200, "{\"codRespuesta\":\"98\"}");
            } else {
                json(exchange, 200,
                        "{\"codRespuesta\":\"0\",\"indCdrGenerado\":\"1\","
                                + "\"arcCdr\":\"" + java.util.Base64.getEncoder()
                                .encodeToString("cdr".getBytes(StandardCharsets.UTF_8)) + "\"}");
            }
        });
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new SunatGreRestClient();
        client.tokenUrl = baseUrl + "/oauth/{clientId}";
        config = SunatEntity.builder()
                .sunatUrlGuiaRemision(baseUrl + "/gre")
                .sunatClientId("client-id")
                .sunatClientSecret("client-secret")
                .sunatUsername("USER")
                .sunatPassword("password")
                .build();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void submitsAndPollsGreWithoutUsingTheInvoiceSoapRoute() throws Exception {
        SunatGreRestClient.GreResponse submitted = client.submit(
                "<DespatchAdvice/>".getBytes(StandardCharsets.UTF_8),
                "20100066603",
                "T001-00000003",
                config
        );
        assertEquals(SunatGreRestClient.GreResponse.State.PENDING, submitted.state());

        SunatGreRestClient.GreResponse pending =
                client.verify(submitted.ticket(), "20100066603", config);
        assertEquals(SunatGreRestClient.GreResponse.State.PENDING, pending.state());

        SunatGreRestClient.GreResponse accepted =
                client.verify(submitted.ticket(), "20100066603", config);
        assertEquals(SunatGreRestClient.GreResponse.State.ACCEPTED, accepted.state());
        assertArrayEquals("cdr".getBytes(StandardCharsets.UTF_8), accepted.cdr());
    }

    @Test
    void rejectsInvalidGreSeriesBeforeCallingSunat() {
        assertThrows(IllegalArgumentException.class, () -> client.submit(
                "<DespatchAdvice/>".getBytes(StandardCharsets.UTF_8),
                "20100066603",
                "F001-1",
                config
        ));
    }

    @Test
    void preservesSanitizedSunatHttpErrorDetails() {
        client.tokenUrl = "http://localhost:" + server.getAddress().getPort()
                + "/oauth-error/{clientId}";

        IOException error = assertThrows(IOException.class, () -> client.submit(
                "<DespatchAdvice/>".getBytes(StandardCharsets.UTF_8),
                "20100066603",
                "T001-00000003",
                config
        ));

        assertEquals(
                "SUNAT GRE HTTP 401: Credenciales OAuth invalidas",
                error.getMessage()
        );
    }

    @Test
    void preservesSunatFunctionalValidationDetails() {
        config.setSunatUrlGuiaRemision(
                "http://localhost:" + server.getAddress().getPort() + "/gre-error"
        );

        IOException error = assertThrows(IOException.class, () -> client.submit(
                "<DespatchAdvice/>".getBytes(StandardCharsets.UTF_8),
                "20100066603",
                "T001-00000003",
                config
        ));

        assertEquals(
                "SUNAT GRE HTTP 422: 3210 - Firma digital invalida",
                error.getMessage()
        );
    }

    private static void json(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}

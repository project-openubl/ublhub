/*
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.project.openubl.ublhub.documents;

import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class SunatGreRestClient {

    @ConfigProperty(
            name = "openubl.sunat.gre.token-url",
            defaultValue = "https://api-seguridad.sunat.gob.pe/v1/clientessol/{clientId}/oauth2/token/"
    )
    String tokenUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public GreResponse submit(byte[] xml, String ruc, String documentId, SunatEntity config)
            throws IOException, InterruptedException, NoSuchAlgorithmException {
        assertRestConfiguration(config);
        GreDocumentName name = GreDocumentName.from(ruc, documentId);
        byte[] zip = zip(name.xmlFileName(), xml);
        String token = requestToken(ruc, config);

        JsonObject payload = Json.createObjectBuilder()
                .add("archivo", Json.createObjectBuilder()
                        .add("nomArchivo", name.zipFileName())
                        .add("arcGreZip", Base64.getEncoder().encodeToString(zip))
                        .add("hashZip", sha256(zip)))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stripTrailingSlash(config.getSunatUrlGuiaRemision())
                        + "/" + name.resourceId()))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
        JsonObject response = sendJson(request);
        String ticket = response.getString("numTicket", null);
        if (ticket == null || ticket.isBlank()) {
            throw new IOException("SUNAT GRE did not return numTicket");
        }
        return GreResponse.pending(ticket);
    }

    public GreResponse verify(String ticket, String ruc, SunatEntity config)
            throws IOException, InterruptedException {
        assertRestConfiguration(config);
        String token = requestToken(ruc, config);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stripTrailingSlash(config.getSunatUrlGuiaRemision())
                        + "/envios/" + url(ticket)))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        JsonObject response = sendJson(request);
        String code = response.getString("codRespuesta", "");
        if ("98".equals(code)) {
            return GreResponse.pending(ticket);
        }

        byte[] cdr = null;
        String encodedCdr = response.getString("arcCdr", null);
        if (encodedCdr != null && !encodedCdr.isBlank()) {
            cdr = Base64.getDecoder().decode(encodedCdr);
        }
        if ("0".equals(code)) {
            return GreResponse.accepted(ticket, cdr);
        }
        if ("99".equals(code)) {
            JsonObject error = response.getJsonObject("error");
            String errorCode = error != null ? error.getString("numError", null) : null;
            String description = error != null
                    ? error.getString("desError", "SUNAT rejected GRE")
                    : "SUNAT rejected GRE";
            return GreResponse.rejected(ticket, errorCode, description, cdr);
        }
        throw new IOException("Unknown SUNAT GRE response code: " + code);
    }

    private String requestToken(String ruc, SunatEntity config)
            throws IOException, InterruptedException {
        String body = form("grant_type", "password")
                + "&" + form("scope", "https://api-cpe.sunat.gob.pe")
                + "&" + form("client_id", config.getSunatClientId())
                + "&" + form("client_secret", config.getSunatClientSecret())
                + "&" + form("username", ruc + config.getSunatUsername())
                + "&" + form("password", config.getSunatPassword());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl.replace("{clientId}", url(config.getSunatClientId()))))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        JsonObject response = sendJson(request);
        String token = response.getString("access_token", null);
        if (token == null || token.isBlank()) {
            throw new IOException("SUNAT OAuth did not return access_token");
        }
        return token;
    }

    private JsonObject sendJson(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        JsonObject json;
        try (JsonReader reader = Json.createReader(
                new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)))) {
            json = reader.readObject();
        } catch (RuntimeException error) {
            throw new IOException("SUNAT GRE returned non-JSON HTTP " + response.statusCode(), error);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String message = functionalError(json);
            if (message == null) {
                message = firstString(
                        json,
                        "error_description",
                        "msg",
                        "message",
                        "desError",
                        "error"
                );
            }
            if (message == null || message.isBlank()) {
                message = "SUNAT GRE request failed";
            }
            throw new IOException("SUNAT GRE HTTP " + response.statusCode() + ": " + message);
        }
        return json;
    }

    private static String functionalError(JsonObject json) {
        if (!json.containsKey("errors")
                || json.isNull("errors")
                || json.get("errors").getValueType() != JsonValue.ValueType.ARRAY
                || json.getJsonArray("errors").isEmpty()
                || json.getJsonArray("errors").get(0).getValueType()
                != JsonValue.ValueType.OBJECT) {
            return null;
        }
        JsonObject error = json.getJsonArray("errors").getJsonObject(0);
        String code = firstString(error, "codError", "cod");
        String description = firstString(error, "desError", "msg", "message");
        if (description == null) {
            return code;
        }
        return code == null ? description : code + " - " + description;
    }

    private static String firstString(JsonObject json, String... keys) {
        for (String key : keys) {
            JsonValue value = json.get(key);
            if (value != null && value.getValueType() == JsonValue.ValueType.STRING) {
                return json.getString(key);
            }
        }
        return null;
    }

    private void assertRestConfiguration(SunatEntity config) {
        if (config == null
                || isBlank(config.getSunatUrlGuiaRemision())
                || isBlank(config.getSunatClientId())
                || isBlank(config.getSunatClientSecret())
                || isBlank(config.getSunatUsername())
                || isBlank(config.getSunatPassword())) {
            throw new IllegalStateException(
                    "SUNAT GRE REST requires guiaUrl, clientId, clientSecret, SOL username and password"
            );
        }
    }

    private static byte[] zip(String xmlFileName, byte[] xml) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry(xmlFileName));
            zip.write(xml);
            zip.closeEntry();
        }
        return output.toByteArray();
    }

    private static String sha256(byte[] value) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private static String form(String key, String value) {
        return url(key) + "=" + url(value);
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String stripTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record GreResponse(
            State state,
            String ticket,
            String errorCode,
            String description,
            byte[] cdr
    ) {
        public enum State { PENDING, ACCEPTED, REJECTED }

        static GreResponse pending(String ticket) {
            return new GreResponse(State.PENDING, ticket, null, "SUNAT GRE processing", null);
        }

        static GreResponse accepted(String ticket, byte[] cdr) {
            return new GreResponse(State.ACCEPTED, ticket, "0", "SUNAT accepted GRE", cdr);
        }

        static GreResponse rejected(
                String ticket,
                String errorCode,
                String description,
                byte[] cdr
        ) {
            return new GreResponse(State.REJECTED, ticket, errorCode, description, cdr);
        }
    }

    record GreDocumentName(String ruc, String documentType, String series, String number) {
        static GreDocumentName from(String ruc, String documentId) {
            String[] parts = documentId.split("-", 2);
            if (parts.length != 2 || !parts[0].matches("[TV][A-Z0-9]{3}")
                    || !parts[1].matches("\\d{1,8}")) {
                throw new IllegalArgumentException("Invalid GRE document ID: " + documentId);
            }
            String type = parts[0].startsWith("T") ? "09" : "31";
            return new GreDocumentName(ruc, type, parts[0], parts[1]);
        }

        String baseName() {
            return ruc + "-" + documentType + "-" + series + "-" + number;
        }

        String xmlFileName() {
            return baseName() + ".xml";
        }

        String zipFileName() {
            return baseName() + ".zip";
        }

        String resourceId() {
            return baseName();
        }
    }
}

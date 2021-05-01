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
package io.github.project.openubl.xsender.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.*;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

@ApplicationScoped
public class KeycloakAuthenticator {

    private static final Logger LOG = Logger.getLogger(KeycloakAuthenticator.class);

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    @Inject
    ObjectMapper objectMapper;

    public Optional<String> authenticate(String message, Session session) {
        try {
            String tokenValue;
            try {
                JsonNode jsonNode = objectMapper.readTree(message);

                JsonNode authentication = jsonNode.get("authentication");
                if (authentication == null) {
                    throw new KeycloakAuthenticationException("Authentication message did not contain a token");
                }

                JsonNode token = authentication.get("token");
                if (token == null) {
                    throw new KeycloakAuthenticationException("Authentication message did not contain a token");
                }

                tokenValue = token.asText();
            } catch (JsonProcessingException e) {
                throw new KeycloakAuthenticationException("Unable to parse message due to: " + e.getMessage());
            }

            JsonNode jsonNode = validateToken(tokenValue);
            String username = jsonNode.get("preferred_username").asText();
            return Optional.of(username);
        } catch (KeycloakAuthenticationException e) {
            LOG.warn("Received a request with an invalid token", e);
            try {
                String errorMessage = e.getLocalizedMessage();

                // Shorten the message to meet the standards for "CloseReason" (only allows 122 chars)
                if (StringUtils.isNotBlank(errorMessage) && errorMessage.length() >= 122) {
                    errorMessage = errorMessage.substring(0, 122);
                }

                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, errorMessage));
            } catch (IOException e1) {
                LOG.warn(e.getMessage());
            }

            return Optional.empty();
        }
    }

    public JsonNode validateToken(String token) throws KeycloakAuthenticationException {
        StringBuilder keycloakUrl = new StringBuilder(authServerUrl);

        if (!authServerUrl.endsWith("/")) {
            keycloakUrl.append("/");
        }

        String fullUrl = keycloakUrl.append("protocol/openid-connect/userinfo").toString();
        try {
            URLConnection urlConnection = new URL(fullUrl).openConnection();
            if (urlConnection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;
                trustAll(httpsConnection);
            }

            urlConnection.setRequestProperty("Authorization", "Bearer " + token);

            try (InputStream inputStream = urlConnection.getInputStream()) {
                int responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
                if (responseCode != 200) {
                    throw new KeycloakAuthenticationException("Failed to authenticate request (" + responseCode + "!");
                }

                // Just consume it... the main thing is that it must be a 200 code
                return objectMapper.readTree(inputStream);
            }
        } catch (Exception e) {
            throw new KeycloakAuthenticationException("Could not authenticate due to: " + e.getMessage(), e);
        }
    }

    private static void trustAll(HttpsURLConnection httpsConnection) throws KeyManagementException, NoSuchAlgorithmException {
        httpsConnection.setHostnameVerifier((s, sslSession) -> true);
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509ExtendedTrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        sslContext.init(null, trustAllCerts, new SecureRandom());

        httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
    }

}

package io.github.project.openubl.xsender.websockets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
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

    public Optional<UserInfo> authenticate(String message, Session session) {
        try {
            String token;
            try {
                Jsonb jsonb = JsonbBuilder.create();
                Authentication auth = jsonb.fromJson(message, Authentication.class);
                token = auth.getAuthentication().getToken();
                if (token == null) {
                    throw new KeycloakAuthenticationException("Authentication message did not contain a token");
                }
            } catch (JsonbException e) {
                throw new KeycloakAuthenticationException("Unable to parse message due to: " + e.getMessage());
            }

            return Optional.of(validateToken(token));
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

    public UserInfo validateToken(String token) throws KeycloakAuthenticationException {
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
                Jsonb jsonb = JsonbBuilder.create();
                return jsonb.fromJson(inputStream, UserInfo.class);
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

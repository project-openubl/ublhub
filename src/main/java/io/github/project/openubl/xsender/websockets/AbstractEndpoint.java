package io.github.project.openubl.xsender.websockets;

import io.vertx.core.impl.ConcurrentHashSet;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.websocket.Session;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractEndpoint {

    private static final Logger LOG = Logger.getLogger(AbstractEndpoint.class);

    protected static final Map<Session, String> sessions = new ConcurrentHashMap<>();
    protected static final Map<String, Set<Session>> userSessions = new ConcurrentHashMap<>();

    @Inject
    KeycloakAuthenticator keycloakAuthenticator;

    public void handleOnMessage(String message, Session session) {
        Optional<UserInfo> userInfoOptional = keycloakAuthenticator.authenticate(message, session);
        userInfoOptional.ifPresent(userInfo -> {
            sessions.put(session, userInfo.getPreferred_username());

            if (!userSessions.containsKey(userInfo.getPreferred_username())) {
                userSessions.put(userInfo.getPreferred_username(), new ConcurrentHashSet<>());
            }
            userSessions.get(userInfo.getPreferred_username()).add(session);
        });
    }

    public void handleOnClose(Session session) {
        String username = sessions.get(session);
        if (username != null) {
            sessions.remove(session);
            userSessions.getOrDefault(username, Collections.emptySet()).remove(session);
        }
    }

}
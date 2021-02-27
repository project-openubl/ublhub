package io.github.project.openubl.xsender.websockets;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Authentication {

    private Body authentication;

    public Body getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Body authentication) {
        this.authentication = authentication;
    }

    @RegisterForReflection
    public static class Body {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}

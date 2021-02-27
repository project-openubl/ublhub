package io.github.project.openubl.xsender.websockets;

import java.util.Objects;

public class UserInfo {

    private String preferred_username;

    public String getPreferred_username() {
        return preferred_username;
    }

    public void setPreferred_username(String preferred_username) {
        this.preferred_username = preferred_username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return preferred_username.equals(userInfo.preferred_username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preferred_username);
    }
}

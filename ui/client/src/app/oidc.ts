import { OidcClientSettings, User } from "oidc-client-ts";
import { ENV } from "./env";

const OIDC_SERVER_URL =
  ENV.OIDC_SERVER_URL || "http://localhost:9001/realms/openubl";
const OIDC_CLIENT_ID = ENV.OIDC_CLIENT_ID || "openubl-ui";

export const oidcClientSettings: OidcClientSettings = {
  authority: OIDC_SERVER_URL,
  client_id: OIDC_CLIENT_ID,
  redirect_uri: window.location.href,
  post_logout_redirect_uri: window.location.href.split("?")[0],
};

export function getUser() {
  const oidcStorage = sessionStorage.getItem(
    `oidc.user:${OIDC_SERVER_URL}:${OIDC_CLIENT_ID}`
  );
  if (!oidcStorage) {
    return null;
  }

  return User.fromStorageString(oidcStorage);
}

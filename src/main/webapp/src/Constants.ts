export enum Permission {
  admin = "admin:app",
  search = "search",
  version_write = "version:write",
  user_write = "user:write",
}
export const ALL_PERMISSIONS: Permission[] = [
  Permission.admin,
  Permission.search,
  Permission.version_write,
  Permission.user_write,
];

export type SEARCHPE_AUTH_METHOD = "oidc" | "basic";

interface Settings {
  defaultAuthMethod: SEARCHPE_AUTH_METHOD;
  formCookieName: string;
  oidcLogoutPath: string;
  isElasticsearchEnabled: boolean;
}

const defaultSettings: Settings = {
  defaultAuthMethod: "basic",
  formCookieName: "searchpe-credential",
  oidcLogoutPath: "/logout",
  isElasticsearchEnabled: false,
};

const SEARCHPE_SETTINGS: Settings =
  (window as any)["SEARCHPE_SETTINGS"] || defaultSettings;

export const isBasicAuthEnabled = (): boolean => {
  return SEARCHPE_SETTINGS.defaultAuthMethod === "basic";
};

export const isOidcAuthEnabled = (): boolean => {
  return SEARCHPE_SETTINGS.defaultAuthMethod === "oidc";
};

export const getAuthFormCookieName = (): string => {
  return SEARCHPE_SETTINGS.formCookieName;
};

export const getOidcLogoutPath = (): string => {
  return SEARCHPE_SETTINGS.oidcLogoutPath;
};

export const isElasticsearchEnabled = (): boolean => {
  return SEARCHPE_SETTINGS.isElasticsearchEnabled;
};

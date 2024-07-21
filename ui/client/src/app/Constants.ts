import ENV from "./env";

export enum BrandType {
  Openubl = "openubl",
  LF = "lf",
}
export const APP_BRAND = ENV.PROFILE as BrandType;

// URL param prefixes: should be short, must be unique for each table that uses one
export enum TableURLParamKeyPrefix {
  repositories = "r",
  tags = "t",
}

export const isAuthRequired = ENV.AUTH_REQUIRED !== "false";
export const uploadLimit = "500m";

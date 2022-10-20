import { UseQueryResult } from "react-query";

export type UnknownResult = Pick<
  UseQueryResult<unknown>,
  "isError" | "isLoading" | "isIdle" | "error"
>;

export interface PageQuery {
  page: number;
  perPage: number;
}

export interface SortByQuery {
  orderBy: string | undefined;
  orderDirection: "asc" | "desc";
}

export interface PageDto<T> {
  count: number;
  items: T[];
}

export interface ProjectDto {
  id?: string;
  name: string;
  description?: string;
  sunat: SunatDto;
}

export interface CompanyDto {
  id?: string;
  ruc: string;
  name: string;
  description?: string;
  sunat?: SunatDto;
}

export interface SunatDto {
  facturaUrl: string;
  guiaUrl: string;
  retencionUrl: string;
  username: string;
  password?: string;
}

export interface KeysMetadataDto {
  active: { [key: string]: string };
  keys: KeyMetadataDto[];
}

export interface KeyMetadataDto {
  providerId: string;
  providerPriority: number;
  kid: string;
  status: "ACTIVE" | "PASSIVE" | "DISABLED";
  type: string;
  algorithm: string;
  publicKey: string;
  certificate: string;
  // This does not come from backend but from UI
  provider?: ComponentDto;
}

export interface ComponentDto {
  id?: string;
  name: string;
  providerId: string;
  providerType: string;
  parentId?: string;
  subType?: string;
  config: { [key: string]: string[] };
}

export interface ComponentTypes {
  keyProviders: ComponentTypeDto[];
}

export interface ComponentTypeDto {
  id: string;
  helpText: string;
  properties: ConfigPropertyDto[];
}

export interface ConfigPropertyDto {
  name: string;
  label: string;
  helpText: string;
  type: "String" | "boolean" | "List" | "File";
  defaultValue: string;
  options: string[];
  secret: boolean;
}

export interface ServerInfoDto {
  componentTypes: { [key: string]: ComponentTypeDto[] };
}

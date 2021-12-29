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

export interface PageRepresentation<T> {
  meta: Meta;
  links: Links;
  items: T[];
}

export interface Meta {
  offset: number;
  limit: number;
  count: number;
}

export interface Links {
  first: string;
  next: string;
  previous: string;
  last: string;
}

export type VersionStatus =
  | "SCHEDULED"
  | "DOWNLOADING"
  | "UNZIPPING"
  | "IMPORTING"
  | "ERROR"
  | "COMPLETED"
  | "DELETING";

export interface Version {
  id: number;
  createdAt: string;
  updatedAt: string;
  status: VersionStatus;
  records: number;
}

export interface Namespace {
  id?: string;
  name: string;
  description?: string;
  webServices: {
    factura: string;
    guia: string;
    retenciones: string;
  };
  credentials: {
    username: string;
    password: string;
  };
}

export interface ServerInfoRepresentation {
  componentTypes: ComponentTypes;
}

export interface KeysMetadataRepresentation {
  active: { [key: string]: string };
  keys: KeyMetadataRepresentation[];
}

export interface KeyMetadataRepresentation {
  providerId: string;
  providerPriority: number;
  kid: string;
  status: "ACTIVE" | "PASSIVE" | "DISABLED";
  type: string;
  algorithm: string;
  publicKey: string;
  certificate: string;
  // This does not come from backend but from UI
  provider?: ComponentRepresentation;
}

export interface ComponentRepresentation {
  id?: string;
  name: string;
  providerId: string;
  providerType: string;
  parentId: string;
  subType?: string;
  config: { [key: string]: string[] };
}

export interface ServerInfoRepresentation {
  componentTypes: ComponentTypes;
}

export interface ComponentTypes {
  keyProviders: ComponentTypeRepresentation[];
}

export interface ComponentTypeRepresentation {
  id: string;
  helpText: string;
  properties: ConfigPropertyRepresentation[];
}

export interface ConfigPropertyRepresentation {
  name: string;
  label: string;
  helpText: string;
  type: string;
  defaultValue: string;
  options: string[];
  secret: boolean;
}

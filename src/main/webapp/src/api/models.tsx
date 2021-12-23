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

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
  data: T[];
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

export interface Contribuyente {
  tipoPersona: TipoContribuyente;
  numeroDocumento: string;
  nombre: string;
  estado: string;
  ubigeo: string;
  condicionDomicilio: string;
  tipoVia: string;
  nombreVia: string;
  codigoZona: string;
  tipoZona: string;
  numero: string;
  interior: string;
  lote: string;
  departamento: string;
  manzana: string;
  kilometro: string;
}

export type TipoContribuyente = "NATURAL" | "JURIDICA";

export enum MimeType {
  TAR = "tar",
  YAML = "yaml",
}

/** Mark an object as "New" therefore does not have an `id` field. */
export type New<T extends { id: number }> = Omit<T, "id">;

export interface HubFilter {
  field: string;
  operator?: "=" | "!=" | "~" | ">" | ">=" | "<" | "<=";
  value:
    | string
    | number
    | {
        list: (string | number)[];
        operator?: "AND" | "OR";
      };
}

export interface HubRequestParams {
  filters?: HubFilter[];
  sort?: {
    field: string;
    direction: "asc" | "desc";
  };
  page?: {
    pageNumber: number; // 1-indexed
    itemsPerPage: number;
  };
}

export interface HubPaginatedResult<T> {
  data: T[];
  total: number;
  params: HubRequestParams;
}

//

export interface Project {
  id: number;
  name: string;
  description?: string;
}

export interface UblDocument {
  id: number;
  file_id: string;
  document_type: string;
  document_id: string;
  supplier_id: string;
  voided_document_code?: string;
}

export interface Credentials {
  id: number;
  name: string;
  description?: string;
  supplier_ids_applied_to: string[];
  soap?: {
    username_sol: string;
    password_sol: string;
    url_invoice: string;
    url_perception_retention: string;
  };
  rest?: {
    client_id: string;
    client_secret: string;
    url_despatch: string;
  };
}

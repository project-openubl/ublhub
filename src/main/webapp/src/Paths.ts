import { Namespace } from "api/models";

export const formatPath = (path: Paths, data: any) => {
  let url = path as string;

  for (const k of Object.keys(data)) {
    url = url.replace(":" + k, data[k]);
  }

  return url;
};

export enum Paths {
  base = "/",
  notFound = "/not-found",

  namespaces = "/namespaces",
  namespaces_edit = "/namespaces/:namespaceId",
  namespaces_edit_companies = "/namespaces/:namespaceId/commpanies",
  namespaces_edit_sunat = "/namespaces/:namespaceId/sunat",
  namespaces_edit_keys = "/namespaces/:namespaceId/keys",

  documents = "/documents",
  documents_ns = "/documents/ns/:namespaceId",
  documents_ns_create = "/documents/ns/:namespaceId/~new",
}

export interface INamespaceParams {
  namespaceId: string;
}

export const documentsPath = (namespace: Namespace) => {
  return formatPath(Paths.documents_ns, { namespaceId: namespace.id });
};

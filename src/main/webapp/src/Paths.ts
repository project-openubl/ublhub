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
}

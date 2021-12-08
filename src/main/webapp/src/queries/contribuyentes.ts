import { UseQueryResult, useQuery } from "react-query";

import { CoreClusterResource, CoreClusterResourceKind } from "api-client";
import { Contribuyente, PageRepresentation } from "api/models";

import { ApiClientError } from "api-client/types";
import { useSearchpeClient } from "./fetchHelpers";

const resource = new CoreClusterResource(CoreClusterResourceKind.Contribuyente);

export interface IContribuyentesParams {
  filterText?: string;
  offset?: number;
  limit?: number;
  sort_by?: string;
}

export class IContribuyentesParamsBuilder {
  private _params: IContribuyentesParams = {};

  public withFilterText(filterText: string) {
    this._params.filterText = filterText;
    return this;
  }
  public withPagination(pagination: { page: number; perPage: number }) {
    this._params.offset = (pagination.page - 1) * pagination.perPage;
    this._params.limit = pagination.perPage;
    return this;
  }
  public withSorting(sorting?: {
    orderBy: string | undefined;
    orderDirection: "asc" | "desc";
  }) {
    if (sorting) {
      this._params.sort_by = `${sorting.orderBy}:${sorting.orderDirection}`;
    }
    return this;
  }

  public build() {
    return this._params;
  }
}

export const useContribuyentesQuery = (
  params: IContribuyentesParams
): UseQueryResult<PageRepresentation<Contribuyente>, ApiClientError> => {
  const client = useSearchpeClient();
  const result = useQuery<PageRepresentation<Contribuyente>, ApiClientError>({
    queryKey: ["contribuyentes", params],
    queryFn: async (): Promise<PageRepresentation<Contribuyente>> => {
      return (
        await client.list<PageRepresentation<Contribuyente>>(resource, params)
      ).data;
    },
    keepPreviousData: true,
    refetchOnMount: true,
  });
  return result;
};

export const useContribuyenteQuery = (
  numeroDocumento: string | null
): UseQueryResult<Contribuyente, ApiClientError> => {
  const client = useSearchpeClient();
  const result = useQuery<Contribuyente, ApiClientError>({
    queryKey: ["contribuyente", numeroDocumento],
    queryFn: async (): Promise<Contribuyente> => {
      return (await client.get<Contribuyente>(resource, numeroDocumento || ""))
        .data;
    },
    enabled: !!numeroDocumento,
    retry: false,
    refetchOnMount: true,
  });
  return result;
};

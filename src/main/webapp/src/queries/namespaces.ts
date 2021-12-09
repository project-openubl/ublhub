import { UseQueryResult, useQuery } from "react-query";

import { CoreClusterResource, CoreClusterResourceKind } from "api-client";
import { Namespace, PageRepresentation } from "api/models";

import { usePollingContext } from "shared/context";

import { ApiClientError } from "api-client/types";
import { useSearchpeClient } from "./fetchHelpers";

const resource = new CoreClusterResource(CoreClusterResourceKind.Namespace);

export enum NamespaceSortByKey {
  name = "name",
}

export interface INamespaceParams {
  filterText?: string;
  offset?: number;
  limit?: number;
  sort_by?: string;
}

export class INamespaceParamsBuilder {
  private _params: INamespaceParams = {};

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
    if (sorting && sorting.orderBy) {
      this._params.sort_by = `${sorting.orderBy}:${sorting.orderDirection}`;
    }
    return this;
  }

  public build() {
    return this._params;
  }
}

export const useNamespacesQuery = (
  params: INamespaceParams
): UseQueryResult<PageRepresentation<Namespace>, ApiClientError> => {
  const client = useSearchpeClient();
  const result = useQuery<PageRepresentation<Namespace>, ApiClientError>({
    queryKey: ["namespaces", params],
    queryFn: async (): Promise<PageRepresentation<Namespace>> => {
      return (
        await client.list<PageRepresentation<Namespace>>(resource, params)
      ).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
  });
  return result;
};

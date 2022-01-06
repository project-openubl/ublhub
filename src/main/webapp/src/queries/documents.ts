import {
  UseMutationResult,
  useQueryClient,
  useMutation,
  UseQueryResult,
  useQuery,
} from "react-query";
import { CoreNamespacedResource, CoreNamespacedResourceKind } from "api-client";

import { ApiClientError } from "api-client/types";
import { Input, UBLDocument } from "api/ublhub";
import { useUblhubClient } from "./fetchHelpers";
import { PageRepresentation } from "api/models";
import { usePollingContext } from "shared/context";

export interface IDocumentsParams {
  filterText?: string;
  offset?: number;
  limit?: number;
  sort_by?: string;
}

export class IDocumentsParamsBuilder {
  private _params: IDocumentsParams = {};

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

export const useDocumentsQuery = (
  namespaceId: string | null,
  params: IDocumentsParams
): UseQueryResult<PageRepresentation<UBLDocument>, ApiClientError> => {
  const defaultRefetchInterval = usePollingContext().refetchInterval;

  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.Document,
    namespaceId || "undefined"
  );

  const client = useUblhubClient();
  const result = useQuery<PageRepresentation<UBLDocument>, ApiClientError>({
    queryKey: ["documents", params],
    queryFn: async (): Promise<PageRepresentation<UBLDocument>> => {
      return (
        await client.list<PageRepresentation<UBLDocument>>(resource, params)
      ).data;
    },
    refetchInterval: (data) => {
      if (data && data.items && data.items.length > 0) {
        const someIsBegingInProgress = data.items.some((f) => f.inProgress);
        if (someIsBegingInProgress) {
          return 3_000;
        }
      }

      return defaultRefetchInterval;
    },
    enabled: !!namespaceId,
  });
  return result;
};

export const usePreviewDocumentMutation = (
  namespaceId: string | null,
  onSuccess?: (xml: string) => void
): UseMutationResult<string, ApiClientError, Input> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.DocumentPreview,
    namespaceId || ""
  );

  const client = useUblhubClient();
  return useMutation<string, ApiClientError, Input>(
    async (input) => {
      return (await client.create<string>(resource, input)).data;
    },
    {
      onSuccess: (response) => {
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useCreateDocumentMutation = (
  namespaceId: string | null,
  onSuccess?: (document: UBLDocument) => void
): UseMutationResult<UBLDocument, ApiClientError, Input> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.Document,
    namespaceId || ""
  );

  const client = useUblhubClient();
  const queryClient = useQueryClient();
  return useMutation<UBLDocument, ApiClientError, Input>(
    async (form) => {
      return (await client.create<UBLDocument>(resource, form)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries("documents");
        queryClient.removeQueries("documents", { inactive: true });
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDocumentUBLFileQuery = (
  namespaceId: string | null,
  documentId: string | null,
  requestedFile: "ubl" | "cdr",
  requestedFormat: "zip" | "xml"
): UseQueryResult<string, ApiClientError> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.DocumentFiles,
    namespaceId || ""
  );

  const client = useUblhubClient();
  const result = useQuery<string, ApiClientError>({
    queryKey: ["document-files", documentId],
    queryFn: async (): Promise<string> => {
      return (
        await client.get<string>(resource, documentId || "", {
          requestedFile,
          requestedFormat,
        })
      ).data;
    },
    enabled: !!documentId,
    retry: false,
    refetchOnMount: true,
  });
  return result;
};

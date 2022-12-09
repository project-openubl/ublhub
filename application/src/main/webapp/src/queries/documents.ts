import axios, { AxiosError } from "axios";
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from "react-query";

import { DocumentDto, DocumentInputDto, PageDto } from "api/models";
import { usePollingContext } from "shared/context";

export interface IDocumentsQueryParams {
  filterText?: string;
  ruc?: string;
  offset?: number;
  limit?: number;
}

export class IDocumentsQueryParamsBuilder {
  private _params: IDocumentsQueryParams = {};

  public withFilterText(filterText: string) {
    this._params.filterText = filterText;
    return this;
  }

  public withRuc(ruc: string) {
    this._params.ruc = ruc;
    return this;
  }

  public withPagination(pagination: { page: number; perPage: number }) {
    this._params.offset = (pagination.page - 1) * pagination.perPage;
    this._params.limit = pagination.perPage;
    return this;
  }

  public build() {
    return this._params;
  }
}

export const useDocumentsQuery = (
  projectId: string | null,
  params: IDocumentsQueryParams
): UseQueryResult<PageDto<DocumentDto>, AxiosError> => {
  const result = useQuery<PageDto<DocumentDto>, AxiosError>({
    queryKey: ["documents", projectId, params],
    queryFn: async () => {
      const url = `/projects/${projectId}/documents`;

      const searchParams = new URLSearchParams();
      Object.entries(params)
        .filter(([_, value]) => value !== null && value !== undefined)
        .forEach(([key, value]) => {
          searchParams.append(key, value);
        });

      return (
        await axios.get<PageDto<DocumentDto>>(url, { params: searchParams })
      ).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectId,
  });
  return result;
};

export const useEnrichDocumentMutation = (
  projectId: string | null,
  onSuccess?: (document: DocumentInputDto) => void
): UseMutationResult<DocumentInputDto, AxiosError, DocumentInputDto> => {
  return useMutation<DocumentInputDto, AxiosError, DocumentInputDto>(
    async (documentInput) => {
      const url = `/projects/${projectId}/enrich-document`;
      const response = (await axios.post<DocumentDto>(url, documentInput)).data;
      return {
        ...documentInput,
        spec: {
          ...documentInput.spec,
          document: response,
        },
      };
    },
    {
      onSuccess: (response) => {
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useRenderDocumentMutation = (
  projectId: string | null,
  onSuccess?: (document: string) => void
): UseMutationResult<string, AxiosError, any> => {
  return useMutation<string, AxiosError, any>(
    async (documentInput) => {
      const url = `/projects/${projectId}/render-document`;
      return (await axios.post<string>(url, documentInput)).data;
    },
    {
      onSuccess: (response) => {
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useCreateDocumentMutation = (
  projectId: string | null,
  onSuccess?: (document: DocumentDto) => void
): UseMutationResult<DocumentDto, AxiosError, DocumentInputDto> => {
  const queryClient = useQueryClient();
  return useMutation<DocumentDto, AxiosError, DocumentInputDto>(
    async (documentInput) => {
      const url = `/projects/${projectId}/documents`;
      return (await axios.post<DocumentDto>(url, documentInput)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["documents", projectId]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDocumentXmlCdrQuery = (
  projectId: string | null,
  documentId: string | null,
  variant: "xml" | "cdr"
): UseQueryResult<string, AxiosError> => {
  const result = useQuery<string, AxiosError>({
    queryKey: [variant, projectId, documentId],
    queryFn: async () => {
      const url = `/projects/${projectId}/documents/${documentId}/${variant}`;
      return (await axios.get<string>(url)).data;
    },
    refetchInterval: false,
    enabled: !!projectId,
  });
  return result;
};

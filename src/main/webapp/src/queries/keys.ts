import axios, { AxiosError } from "axios";
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from "react-query";

import { ComponentDto, KeysMetadataDto } from "api/models";
import { usePollingContext } from "shared/context";

export const useKeysQuery = (
  projectId: string | null,
  companyId: string | null
): UseQueryResult<KeysMetadataDto, AxiosError> => {
  const result = useQuery<KeysMetadataDto, AxiosError>({
    queryKey: ["keys", { project: projectId, company: companyId }],
    queryFn: async () => {
      const url =
        companyId === null
          ? `/projects/${projectId}/keys`
          : `/projects/${projectId}/companies/${companyId}/keys`;
      return (await axios.get<KeysMetadataDto>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectId,
  });
  return result;
};

export const useComponentsQuery = (
  projectId: string | null,
  companyId: string | null
): UseQueryResult<ComponentDto[], AxiosError> => {
  const result = useQuery<ComponentDto[], AxiosError>({
    queryKey: ["components", { project: projectId, company: companyId }],
    queryFn: async () => {
      const url =
        companyId === null
          ? `/projects/${projectId}/components`
          : `/projects/${projectId}/companies/${companyId}/components`;
      return (await axios.get<ComponentDto[]>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectId,
  });
  return result;
};

export const useComponentQuery = (
  projectId: string | null,
  companyId: string | null,
  componentId: string | null
): UseQueryResult<ComponentDto, AxiosError> => {
  const result = useQuery<ComponentDto, AxiosError>({
    queryKey: [
      "components",
      { project: projectId, company: companyId },
      componentId,
    ],
    queryFn: async () => {
      const url =
        companyId === null
          ? `/projects/${projectId}/components/${componentId}`
          : `/projects/${projectId}/companies/${companyId}/components/${componentId}`;
      return (await axios.get<ComponentDto>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectId,
  });
  return result;
};

export const useCreateComponentMutation = (
  projectId: string | null,
  companyId: string | null,
  onSuccess?: (component: ComponentDto) => void
): UseMutationResult<ComponentDto, AxiosError, ComponentDto> => {
  const queryClient = useQueryClient();
  return useMutation<ComponentDto, AxiosError, ComponentDto>(
    async (component) => {
      const url =
        companyId === null
          ? `/projects/${projectId}/components`
          : `/projects/${projectId}/companies/${companyId}/components`;
      return (await axios.post<ComponentDto>(url, component)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries([
          "keys",
          { project: projectId, company: companyId },
        ]);
        queryClient.invalidateQueries([
          "components",
          { project: projectId, company: companyId },
        ]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useUpdateComponentMutation = (
  projectId: string | null,
  companyId: string | null,
  onSuccess?: (component: ComponentDto) => void
): UseMutationResult<ComponentDto, AxiosError, ComponentDto> => {
  const queryClient = useQueryClient();
  return useMutation<ComponentDto, AxiosError, ComponentDto>(
    async (component) => {
      const url =
        companyId === null
          ? `/projects/${projectId}/components/${component.id}`
          : `/projects/${projectId}/companies/${companyId}/components/${component.id}`;
      return (await axios.put<ComponentDto>(url, component)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries([
          "keys",
          { project: projectId, company: companyId },
        ]);
        queryClient.invalidateQueries([
          "components",
          { project: projectId, company: companyId },
        ]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDeleteComponentMutation = (
  projectId: string | null,
  companyId: string | null,
  onSuccess?: () => void
): UseMutationResult<void, AxiosError, ComponentDto, unknown> => {
  const queryClient = useQueryClient();

  return useMutation<void, AxiosError, ComponentDto>(
    async (component: ComponentDto) => {
      const url =
        companyId === null
          ? `/projects/${projectId}/components/${component.id}`
          : `/projects/${projectId}/companies/${companyId}/components/${component.id}`;
      await axios.delete<void>(url);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries([
          "keys",
          { project: projectId, company: companyId },
        ]);
        queryClient.invalidateQueries([
          "components",
          { project: projectId, company: companyId },
        ]);
        onSuccess && onSuccess();
      },
    }
  );
};

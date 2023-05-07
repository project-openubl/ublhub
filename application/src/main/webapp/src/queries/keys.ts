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
  projectName: string | null,
  ruc: string | null
): UseQueryResult<KeysMetadataDto, AxiosError> => {
  const result = useQuery<KeysMetadataDto, AxiosError>({
    queryKey: ["keys", { project: projectName, company: ruc }],
    queryFn: async () => {
      const url =
        ruc === null
          ? `/projects/${projectName}/keys`
          : `/projects/${projectName}/companies/${ruc}/keys`;
      return (await axios.get<KeysMetadataDto>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectName,
  });
  return result;
};

export const useComponentsQuery = (
  projectName: string | null,
  ruc: string | null
): UseQueryResult<ComponentDto[], AxiosError> => {
  const result = useQuery<ComponentDto[], AxiosError>({
    queryKey: ["components", { project: projectName, company: ruc }],
    queryFn: async () => {
      const url =
        ruc === null
          ? `/projects/${projectName}/components`
          : `/projects/${projectName}/companies/${ruc}/components`;
      return (await axios.get<ComponentDto[]>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectName,
  });
  return result;
};

export const useComponentQuery = (
  projectName: string | null,
  ruc: string | null,
  componentId: string | null
): UseQueryResult<ComponentDto, AxiosError> => {
  const result = useQuery<ComponentDto, AxiosError>({
    queryKey: [
      "components",
      { project: projectName, company: ruc },
      componentId,
    ],
    queryFn: async () => {
      const url =
        ruc === null
          ? `/projects/${projectName}/components/${componentId}`
          : `/projects/${projectName}/companies/${ruc}/components/${componentId}`;
      return (await axios.get<ComponentDto>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectName,
  });
  return result;
};

export const useCreateComponentMutation = (
  projectName: string | null,
  ruc: string | null,
  onSuccess?: (component: ComponentDto) => void
): UseMutationResult<ComponentDto, AxiosError, ComponentDto> => {
  const queryClient = useQueryClient();
  return useMutation<ComponentDto, AxiosError, ComponentDto>(
    async (component) => {
      const url =
        ruc === null
          ? `/projects/${projectName}/components`
          : `/projects/${projectName}/companies/${ruc}/components`;
      return (await axios.post<ComponentDto>(url, component)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries([
          "keys",
          { project: projectName, company: ruc },
        ]);
        queryClient.invalidateQueries([
          "components",
          { project: projectName, company: ruc },
        ]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useUpdateComponentMutation = (
  projectName: string | null,
  ruc: string | null,
  onSuccess?: (component: ComponentDto) => void
): UseMutationResult<ComponentDto, AxiosError, ComponentDto> => {
  const queryClient = useQueryClient();
  return useMutation<ComponentDto, AxiosError, ComponentDto>(
    async (component) => {
      const url =
        ruc === null
          ? `/projects/${projectName}/components/${component.id}`
          : `/projects/${projectName}/companies/${ruc}/components/${component.id}`;
      return (await axios.put<ComponentDto>(url, component)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries([
          "keys",
          { project: projectName, company: ruc },
        ]);
        queryClient.invalidateQueries([
          "components",
          { project: projectName, company: ruc },
        ]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDeleteComponentMutation = (
  projectName: string | null,
  ruc: string | null,
  onSuccess?: () => void
): UseMutationResult<void, AxiosError, ComponentDto, unknown> => {
  const queryClient = useQueryClient();

  return useMutation<void, AxiosError, ComponentDto>(
    async (component: ComponentDto) => {
      const url =
        ruc === null
          ? `/projects/${projectName}/components/${component.id}`
          : `/projects/${projectName}/companies/${ruc}/components/${component.id}`;
      await axios.delete<void>(url);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries([
          "keys",
          { project: projectName, company: ruc },
        ]);
        queryClient.invalidateQueries([
          "components",
          { project: projectName, company: ruc },
        ]);
        onSuccess && onSuccess();
      },
    }
  );
};

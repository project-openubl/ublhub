import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AxiosError } from "axios";

import { Credentials, New } from "@app/api/models";
import {
  createCredentials,
  deleteCredentials,
  getCredentials,
  getCredentialsById,
  updateCredentials,
} from "@app/api/rest";

export const CredentialsQueryKey = "credentials";

export const useFetchCredentials = (projectId: number | string) => {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: [CredentialsQueryKey, projectId],
    queryFn: () => getCredentials(projectId),
  });
  return {
    credentials: data || [],
    isFetching: isLoading,
    fetchError: error,
    refetch,
  };
};

export const useFethCredentialsById = (
  projectId: number | string,
  id?: number | string
) => {
  const { data, isLoading, error } = useQuery({
    queryKey: [CredentialsQueryKey, projectId, id],
    queryFn: () =>
      id === undefined
        ? Promise.resolve(undefined)
        : getCredentialsById(projectId, id),
    enabled: id !== undefined,
  });

  return {
    credentials: data,
    isFetching: isLoading,
    fetchError: error as AxiosError,
  };
};

export const useCreateCredentialsMutation = (
  projectId: number | string,
  onSuccess: (res: Credentials) => void,
  onError: (err: AxiosError, payload: New<Credentials>) => void
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (obj) => createCredentials(projectId, obj),
    onSuccess: ({ data }, _payload) => {
      onSuccess(data);
      queryClient.invalidateQueries({ queryKey: [CredentialsQueryKey] });
    },
    onError,
  });
};

export const useUpdateCredentialsMutation = (
  projectId: number | string,
  onSuccess: (payload: Credentials) => void,
  onError: (err: AxiosError, payload: Credentials) => void
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (obj) => updateCredentials(projectId, obj),
    onSuccess: (_res, payload) => {
      onSuccess(payload);
      queryClient.invalidateQueries({ queryKey: [CredentialsQueryKey] });
    },
    onError: onError,
  });
};

export const useDeleteCredentialsMutation = (
  onSuccess: (id: number | string) => void,
  onError: (err: AxiosError, id: number | string) => void
) => {
  const queryClient = useQueryClient();

  const { isPending, mutate, error } = useMutation({
    mutationFn: ({
      projectId,
      id,
    }: {
      projectId: string | number;
      id: string | number;
    }) => deleteCredentials(projectId, id),
    onSuccess: (_res, { id }) => {
      onSuccess(id);
      queryClient.invalidateQueries({ queryKey: [CredentialsQueryKey] });
    },
    onError: (err: AxiosError, { id }) => {
      onError(err, id);
      queryClient.invalidateQueries({ queryKey: [CredentialsQueryKey] });
    },
  });

  return {
    mutate,
    isPending,
    error,
  };
};

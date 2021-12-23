import * as yup from "yup";
import {
  UseQueryResult,
  useQuery,
  useQueryClient,
  useMutation,
  UseMutationResult,
} from "react-query";

import { CoreClusterResource, CoreClusterResourceKind } from "api-client";
import { Namespace } from "api/models";

import { usePollingContext } from "shared/context";

import { ApiClientError } from "api-client/types";
import { useUblhubClient } from "./fetchHelpers";
import { useCallback } from "react";

const resource = new CoreClusterResource(CoreClusterResourceKind.Namespace);

export const useNamespacesQuery = (): UseQueryResult<
  Namespace[],
  ApiClientError
> => {
  const sortNamespaceListByNameCallback = useCallback(
    (data: Namespace[]): Namespace[] =>
      [...data].sort((a, b) => a.name.localeCompare(b.name)),
    []
  );
  const client = useUblhubClient();
  const result = useQuery<Namespace[], ApiClientError>({
    queryKey: ["namespaces"],
    queryFn: async () => {
      return (await client.list<Namespace[]>(resource)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    select: sortNamespaceListByNameCallback,
  });
  return result;
};

export const useCreateNamespaceMutation = (
  onSuccess?: (namespace: Namespace) => void
): UseMutationResult<Namespace, ApiClientError, Namespace> => {
  const client = useUblhubClient();
  const queryClient = useQueryClient();
  return useMutation<Namespace, ApiClientError, Namespace>(
    async (form) => {
      return (await client.create<Namespace>(resource, form)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries("namespaces");
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const getNamespaceNameSchema = (
  namespacesQuery: UseQueryResult<Namespace[]>,
  namespaceBeingPrefilled: Namespace | null
): yup.StringSchema =>
  yup
    .string()
    .required()
    .trim()
    .min(3)
    .max(250)
    .test(
      "unique-name",
      "Un namespace con el mismo nombre ya existe",
      (value) => {
        if (namespaceBeingPrefilled?.name === value) {
          return true;
        }
        if (namespacesQuery.data?.find((ns) => ns.name === value)) {
          return false;
        }

        return true;
      }
    );

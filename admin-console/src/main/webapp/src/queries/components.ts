import * as yup from "yup";
import {
  UseQueryResult,
  useQuery,
  UseMutationResult,
  useQueryClient,
  useMutation,
} from "react-query";

import { CoreNamespacedResource, CoreNamespacedResourceKind } from "api-client";
import {
  ComponentRepresentation,
  KeysMetadataRepresentation,
} from "api/models";

import { usePollingContext } from "shared/context";

import { ApiClientError } from "api-client/types";
import { useUblhubClient } from "./fetchHelpers";

export const useKeysQuery = (
  namespaceId: string | null
): UseQueryResult<KeysMetadataRepresentation, ApiClientError> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.Key,
    namespaceId || ""
  );

  const client = useUblhubClient();
  const result = useQuery<KeysMetadataRepresentation, ApiClientError>({
    queryKey: ["keys", { namespaceId }],
    queryFn: async () => {
      return (await client.list<KeysMetadataRepresentation>(resource)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
  });
  return result;
};

export const useComponentsQuery = (
  namespaceId: string | null
): UseQueryResult<ComponentRepresentation[], ApiClientError> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.Component,
    namespaceId || ""
  );

  const client = useUblhubClient();
  const result = useQuery<ComponentRepresentation[], ApiClientError>({
    queryKey: ["components", { namespaceId }],
    queryFn: async () => {
      return (await client.list<ComponentRepresentation[]>(resource)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
  });
  return result;
};

export const useCreateComponentMutation = (
  namespaceId: string | null,
  onSuccess?: (component: ComponentRepresentation) => void
): UseMutationResult<
  ComponentRepresentation,
  ApiClientError,
  ComponentRepresentation
> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.Component,
    namespaceId || ""
  );

  const client = useUblhubClient();
  const queryClient = useQueryClient();
  return useMutation<
    ComponentRepresentation,
    ApiClientError,
    ComponentRepresentation
  >(
    async (form) => {
      return (await client.create<ComponentRepresentation>(resource, form))
        .data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["keys", { namespaceId }]);
        queryClient.invalidateQueries(["components", { namespaceId }]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useUpdateComponentMutation = (
  namespaceId: string | null,
  onSuccess?: (component: ComponentRepresentation) => void
): UseMutationResult<
  ComponentRepresentation,
  ApiClientError,
  ComponentRepresentation
> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.Component,
    namespaceId || ""
  );

  const client = useUblhubClient();
  const queryClient = useQueryClient();
  return useMutation<
    ComponentRepresentation,
    ApiClientError,
    ComponentRepresentation
  >(
    async (form) => {
      return (
        await client.put<ComponentRepresentation>(resource, form.id || "", form)
      ).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["keys", { namespaceId }]);
        queryClient.invalidateQueries(["components", { namespaceId }]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDeleteComponentMutation = (
  namespaceId: string | null,
  onSuccess?: () => void
): UseMutationResult<
  void,
  ApiClientError,
  ComponentRepresentation,
  unknown
> => {
  const resource = new CoreNamespacedResource(
    CoreNamespacedResourceKind.Component,
    namespaceId || ""
  );

  const client = useUblhubClient();
  const queryClient = useQueryClient();
  return useMutation<void, ApiClientError, ComponentRepresentation>(
    async (component: ComponentRepresentation) => {
      await client.delete<void>(resource, `${component.id}`);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(["keys", { namespaceId }]);
        queryClient.invalidateQueries(["components", { namespaceId }]);
        onSuccess && onSuccess();
      },
    }
  );
};

export const getComponentNameSchema = (
  componentsQuery: UseQueryResult<ComponentRepresentation[]>,
  componentBeingPrefilled?: ComponentRepresentation
): yup.StringSchema =>
  yup
    .string()
    .label("Name")
    .required()
    .trim()
    .min(3)
    .max(250)
    .test(
      "unique-name",
      "A component with this name already exists",
      (value) => {
        if (componentBeingPrefilled?.name === value) {
          return true;
        }
        if (componentsQuery.data?.find((ns) => ns.name === value)) {
          return false;
        }

        return true;
      }
    );

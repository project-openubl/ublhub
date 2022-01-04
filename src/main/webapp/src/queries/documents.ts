import { UseMutationResult, useQueryClient, useMutation } from "react-query";
import { CoreNamespacedResource, CoreNamespacedResourceKind } from "api-client";

import { ApiClientError } from "api-client/types";
import { Input, UBLDocument } from "api/ublhub";
import { useUblhubClient } from "./fetchHelpers";

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
        queryClient.invalidateQueries(["documents", { namespaceId }]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

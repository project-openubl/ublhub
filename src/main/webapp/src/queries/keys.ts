import axios, { AxiosError } from "axios";
import { UseQueryResult, useQuery } from "react-query";

import { ComponentDto, KeysMetadataDto } from "api/models";
import { usePollingContext } from "shared/context";

export const useKeysQuery = (
  projectId: string | null,
  companyId: string | null
): UseQueryResult<KeysMetadataDto, AxiosError> => {
  const result = useQuery<KeysMetadataDto, AxiosError>({
    queryKey: ["keys", projectId, companyId],
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

export const useKeyQuery = (
  projectId: string | null,
  companyId: string | null,
  keyId: string | null
): UseQueryResult<ComponentDto, AxiosError> => {
  const result = useQuery<ComponentDto, AxiosError>({
    queryKey: ["key", projectId, companyId, keyId],
    queryFn: async () => {
      const url =
        companyId === null
          ? `/projects/${projectId}/keys/${keyId}`
          : `/projects/${projectId}/companies/${companyId}/keys/${keyId}`;
      return (await axios.get<ComponentDto>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectId,
  });
  return result;
};

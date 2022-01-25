import { UseQueryResult, useQuery } from "react-query";

import { CoreClusterResource, CoreClusterResourceKind } from "api-client";
import { ServerInfoRepresentation } from "api/models";

import { usePollingContext } from "shared/context";

import { ApiClientError } from "api-client/types";
import { useUblhubClient } from "./fetchHelpers";

const resource = new CoreClusterResource(CoreClusterResourceKind.ServerInfo);

export const useServerInfoQuery = (): UseQueryResult<
  ServerInfoRepresentation,
  ApiClientError
> => {
  const client = useUblhubClient();
  const result = useQuery<ServerInfoRepresentation, ApiClientError>({
    queryKey: ["serverInfo"],
    queryFn: async () => {
      return (await client.list<ServerInfoRepresentation>(resource)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
  });
  return result;
};

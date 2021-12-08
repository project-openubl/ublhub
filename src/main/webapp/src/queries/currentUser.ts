import { UseQueryResult, useQuery } from "react-query";

import {
  CurrentUserClusterResource,
  CurrentUserClusterResourceKind,
} from "api-client";
import { ApiClientError } from "api-client/types";

import { User } from "api/models";
import { useSearchpeClient } from "./fetchHelpers";

const whoAmIResource = new CurrentUserClusterResource(
  CurrentUserClusterResourceKind.WhoAmI
);

export const useCurrentUserQuery = (): UseQueryResult<User, ApiClientError> => {
  const client = useSearchpeClient();
  const result = useQuery<User, ApiClientError>({
    queryKey: "currentUser",
    queryFn: async () => {
      return (await client.get<User>(whoAmIResource, "")).data;
    },
    refetchInterval: 60_000,
    retry: process.env.NODE_ENV === "development" ? false : undefined,
  });
  return result;
};

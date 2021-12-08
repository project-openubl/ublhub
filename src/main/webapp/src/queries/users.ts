import { useCallback } from "react";
import {
  UseQueryResult,
  UseMutationResult,
  useQuery,
  useQueryClient,
  useMutation,
} from "react-query";

import { AdminClusterResource, AdminClusterResourceKind } from "api-client";
import { User } from "api/models";

import { ApiClientError } from "api-client/types";
import { useSearchpeClient } from "./fetchHelpers";

const userResource = new AdminClusterResource(AdminClusterResourceKind.User);

export const useUsersQuery = (): UseQueryResult<User[], ApiClientError> => {
  const sortUserListByUsernameCallback = useCallback((data: User[]): User[] => {
    return data.sort((a, b) => a.username.localeCompare(b.username));
  }, []);
  const client = useSearchpeClient();
  const result = useQuery<User[], ApiClientError>({
    queryKey: "users",
    queryFn: async () => {
      return (await client.list<User[]>(userResource)).data;
    },
    select: sortUserListByUsernameCallback,
  });
  return result;
};

export const useCreateUserMutation = (
  onSuccess?: (user: User) => void
): UseMutationResult<User, ApiClientError, User> => {
  const client = useSearchpeClient();
  const queryClient = useQueryClient();
  return useMutation<User, ApiClientError, User>(
    async (user: User) => {
      return (await client.create<User>(userResource, user)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries("users");
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useUpdateUserMutation = (
  onSuccess?: (user: User) => void
): UseMutationResult<User, ApiClientError, User> => {
  const client = useSearchpeClient();
  const queryClient = useQueryClient();
  return useMutation<User, ApiClientError, User>(
    async (user: User) => {
      return (
        await client.put<User>(userResource, user.id?.toString() || "", user)
      ).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries("users");
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDeleteUserMutation = (
  onSuccess?: () => void
): UseMutationResult<void, ApiClientError, User, unknown> => {
  const client = useSearchpeClient();
  const queryClient = useQueryClient();
  return useMutation<void, ApiClientError, User>(
    async (user: User) => {
      await client.delete<void>(userResource, `${user.id}`);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries("users");
        onSuccess && onSuccess();
      },
    }
  );
};

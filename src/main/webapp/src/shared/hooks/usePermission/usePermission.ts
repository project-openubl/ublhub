import { Permission } from "Constants";
import { useCurrentUserQuery } from "queries/currentUser";

export interface IArgs {
  hasAny: Permission[];
}

export interface IState {
  isAllowed: boolean;
}

export const usePermission = ({ hasAny }: IArgs): IState => {
  const currentUser = useCurrentUserQuery();

  const userPermissions = currentUser.data?.permissions || [];
  const isAllowed = hasAny.some((permission) => {
    return userPermissions.some((f) => f === permission);
  });

  return {
    isAllowed,
  };
};

export default usePermission;

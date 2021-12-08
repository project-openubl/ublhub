import React from "react";
import { Permission } from "Constants";
import { usePermission } from "shared/hooks";

export interface VisibilityByPermissionProps {
  hasAny: Permission[];
}

export const VisibilityByPermission: React.FC<VisibilityByPermissionProps> = ({
  hasAny,
  children,
}) => {
  const { isAllowed } = usePermission({ hasAny });
  return <>{isAllowed && children}</>;
};

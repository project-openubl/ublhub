import React from "react";
import { Route, RouteProps } from "react-router-dom";

import {
  Bullseye,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from "@patternfly/react-core";
import { WarningTriangleIcon } from "@patternfly/react-icons";

import { usePermission } from "shared/hooks";

import { Permission } from "Constants";

export interface IProtectedRouteProps extends RouteProps {
  hasAny: Permission[];
}

export const ProtectedRoute: React.FC<IProtectedRouteProps> = ({
  hasAny,
  ...rest
}) => {
  const { isAllowed } = usePermission({ hasAny });

  const notAuthorizedState = (
    <Bullseye>
      <EmptyState variant={EmptyStateVariant.small}>
        <EmptyStateIcon icon={WarningTriangleIcon} />
        <Title headingLevel="h2" size="lg">
          403 Forbidden
        </Title>
        <EmptyStateBody>You are not allowed to access this page</EmptyStateBody>
      </EmptyState>
    </Bullseye>
  );

  if (!isAllowed) {
    return <Route render={() => notAuthorizedState}></Route>;
  }

  return <Route {...rest} />;
};

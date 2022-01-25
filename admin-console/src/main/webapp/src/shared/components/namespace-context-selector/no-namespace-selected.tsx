import React from "react";
import {
  EmptyState,
  Title,
  EmptyStateBody,
  EmptyStateVariant,
  EmptyStateIcon,
  Bullseye,
} from "@patternfly/react-core";
import { ThumbTackIcon } from "@patternfly/react-icons";

export const NoNamespaceSelected: React.FC = () => {
  return (
    <Bullseye>
      <EmptyState variant={EmptyStateVariant.small}>
        <EmptyStateIcon icon={ThumbTackIcon} />
        <Title headingLevel="h4" size="lg">
          Select a namespace
        </Title>
        <EmptyStateBody>
          To see the content you are looking for, you need to select a
          namespace.
        </EmptyStateBody>
      </EmptyState>
    </Bullseye>
  );
};

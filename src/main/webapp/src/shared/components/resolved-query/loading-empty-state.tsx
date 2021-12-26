import React from "react";
import {
  Bullseye,
  EmptyState,
  EmptyStateBody,
  Spinner,
  SpinnerProps,
  Title,
} from "@patternfly/react-core";

interface ILoadingEmptyStateProps {
  className?: string;
  spinnerProps?: Partial<SpinnerProps>;
  body?: React.ReactNode;
}

export const LoadingEmptyState: React.FunctionComponent<ILoadingEmptyStateProps> =
  ({
    className = "",
    spinnerProps = {},
    body = null,
  }: ILoadingEmptyStateProps) => (
    <Bullseye className={className}>
      <EmptyState variant="large">
        <div className="pf-c-empty-state__icon">
          <Spinner
            aria-labelledby="loadingPrefLabel"
            size="xl"
            {...spinnerProps}
          />
        </div>
        <Title id="loadingPrefLabel" headingLevel="h2">
          Loading...
        </Title>
        {body ? <EmptyStateBody>{body}</EmptyStateBody> : null}
      </EmptyState>
    </Bullseye>
  );

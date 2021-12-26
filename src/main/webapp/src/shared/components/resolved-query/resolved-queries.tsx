import React from "react";
import { UseMutationResult } from "react-query";
import { UnknownResult } from "api/models";
import { ApiClientError } from "api-client/types";

import {
  Spinner,
  Alert,
  AlertActionCloseButton,
  SpinnerProps,
  AlertProps,
  AlertGroup,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";
import { getAggregateQueryStatus } from "queries/helpers";

import { LoadingEmptyState } from "./loading-empty-state";

export enum QuerySpinnerMode {
  Inline = "Inline",
  EmptyState = "EmptyState",
  None = "None",
}

export interface IResolvedQueriesProps {
  results: UnknownResult[];
  errorTitles: string[];
  errorsInline?: boolean;
  spinnerMode?: QuerySpinnerMode;
  emptyStateBody?: React.ReactNode;
  spinnerProps?: Partial<SpinnerProps>;
  alertProps?: Partial<AlertProps>;
  className?: string;
  forceLoadingState?: boolean;
  children?: React.ReactNode;
}

export const ResolvedQueries: React.FunctionComponent<IResolvedQueriesProps> =
  ({
    results,
    errorTitles,
    errorsInline = true,
    spinnerMode = QuerySpinnerMode.EmptyState,
    emptyStateBody = null,
    spinnerProps = {},
    alertProps = {},
    className = "",
    forceLoadingState = false,
    children = null,
  }: IResolvedQueriesProps) => {
    const status = getAggregateQueryStatus(results);
    const erroredResults = results.filter((result) => result.isError);
    const filteredErrorTitles = errorTitles.filter(
      (_title, index) => results[index].isError
    );

    let spinner: React.ReactNode = null;
    if (spinnerMode === QuerySpinnerMode.Inline) {
      spinner = <Spinner size="lg" className={className} {...spinnerProps} />;
    } else if (spinnerMode === QuerySpinnerMode.EmptyState) {
      spinner = (
        <LoadingEmptyState spinnerProps={spinnerProps} body={emptyStateBody} />
      );
    }

    React.useEffect(() => {
      return () => {
        // On unmount, dismiss any errors in mutations.
        results.forEach((result) => {
          if (
            !result.isLoading &&
            (result as UseMutationResult<unknown>).reset
          ) {
            (result as UseMutationResult<unknown>).reset();
          }
        });
      };
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
      <>
        {status === "loading" || forceLoadingState ? (
          spinner
        ) : status === "error" ? (
          <AlertGroup aria-live="assertive">
            {erroredResults.map((result, index) => (
              <Alert
                key={`error-${index}`}
                variant="danger"
                isInline={errorsInline}
                title={filteredErrorTitles[index]}
                className={`${
                  index !== erroredResults.length - 1 ? spacing.mbMd : ""
                } ${className}`}
                actionClose={
                  (result as { reset?: () => void }).reset ? (
                    <AlertActionCloseButton
                      aria-label="Dismiss error"
                      onClose={(result as UseMutationResult<unknown>).reset}
                    />
                  ) : null
                }
                {...alertProps}
              >
                {result.error ? (
                  <>
                    {(result.error as Error).message || null}
                    {(result.error as ApiClientError).response ? (
                      <>
                        <br />
                        {
                          (result.error as ApiClientError).response?.data
                            ?.message
                        }
                      </>
                    ) : null}
                    {(result.error as Response).status
                      ? `${(result.error as Response).status}: ${
                          (result.error as Response).statusText
                        }`
                      : null}
                    {typeof result.error === "string" ? result.error : null}
                  </>
                ) : null}
              </Alert>
            ))}
          </AlertGroup>
        ) : (
          children
        )}
      </>
    );
  };

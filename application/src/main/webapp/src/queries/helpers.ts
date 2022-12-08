import { UnknownResult } from "api/models";
import { QueryStatus, UseQueryResult } from "react-query";

export const getAggregateQueryStatus = (
  queryResults: UnknownResult[]
): QueryStatus => {
  if (queryResults.some((result) => result.isError)) return "error";
  if (queryResults.some((result) => result.isLoading)) return "loading";
  if (queryResults.every((result) => result.isIdle)) return "idle";
  return "success";
};

export const getFirstQueryError = <TError>(
  queryResults: UseQueryResult<unknown, TError>[]
): TError | null => {
  for (const result of queryResults) {
    if (result.isError) return result.error;
  }
  return null;
};

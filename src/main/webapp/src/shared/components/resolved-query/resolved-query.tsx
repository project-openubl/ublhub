import React from "react";
import { UnknownResult } from "api/models";
import { ResolvedQueries, IResolvedQueriesProps } from "./resolved-queries";

export interface IResolvedQueryProps
  extends Omit<IResolvedQueriesProps, "results" | "errorTitles"> {
  result: UnknownResult;
  errorTitle: string;
}

// TODO lib-ui candidate
export const ResolvedQuery: React.FunctionComponent<IResolvedQueryProps> = ({
  result,
  errorTitle,
  ...props
}: IResolvedQueryProps) => (
  <ResolvedQueries results={[result]} errorTitles={[errorTitle]} {...props} />
);

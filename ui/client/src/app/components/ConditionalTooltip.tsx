import React from "react";
import { Tooltip, TooltipProps } from "@patternfly/react-core";

export interface IConditionalTooltipProps extends TooltipProps {
  isTooltipEnabled: boolean;
  children: React.ReactElement;
}

// TODO: lib-ui candidate
export const ConditionalTooltip: React.FC<IConditionalTooltipProps> = ({
  isTooltipEnabled,
  children,
  ...props
}: IConditionalTooltipProps) =>
  isTooltipEnabled ? <Tooltip {...props}>{children}</Tooltip> : children;

import React from "react";
import { Text, TextContent, TextVariants } from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

interface IWizardStepContainerProps {
  title: string;
  children: React.ReactNode;
}

export const WizardStepContainer: React.FunctionComponent<
  IWizardStepContainerProps
> = ({ title, children }: IWizardStepContainerProps) => (
  <>
    <TextContent className={spacing.mbMd}>
      <Text component={TextVariants.h2}>{title}</Text>
    </TextContent>
    {children}
  </>
);

import React from "react";
import {
  PageSection,
  PageSectionVariants,
  TextContent,
  Text,
} from "@patternfly/react-core";

export interface SimplePageSectionProps {
  title: string;
  description?: string;
}

export const SimplePageSection: React.FC<SimplePageSectionProps> = ({
  title,
  description,
}) => {
  return (
    <PageSection variant={PageSectionVariants.light}>
      <TextContent>
        <Text component="h1">{title}</Text>
        {description && <Text component="small">{description}</Text>}
      </TextContent>
    </PageSection>
  );
};

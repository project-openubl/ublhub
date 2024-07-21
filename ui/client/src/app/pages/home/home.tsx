import React from "react";

import {
  PageSection,
  PageSectionVariants,
  Text,
  TextContent,
} from "@patternfly/react-core";

export const Home: React.FC = () => {
  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">Trusted Content</Text>
          <Text component="p">Fully hosted and managed service</Text>
        </TextContent>
      </PageSection>
      <PageSection variant={PageSectionVariants.default}>content</PageSection>
    </>
  );
};

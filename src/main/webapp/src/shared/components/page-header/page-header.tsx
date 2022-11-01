import React from "react";
import {
  Split,
  SplitItem,
  Stack,
  StackItem,
  Text,
  TextContent,
} from "@patternfly/react-core";
import { BreadCrumbPath } from "./breadcrumb-path";
import { MenuActions } from "./menu-actions";

export interface PageHeaderProps {
  title: string;
  breadcrumbs: { title: string; path: string }[];
  menuActions: { label: string; callback: () => void }[];
}

export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  breadcrumbs,
  menuActions,
}) => {
  return (
    <Stack hasGutter>
      <StackItem>
        <BreadCrumbPath breadcrumbs={breadcrumbs} />
      </StackItem>
      <StackItem>
        <Split>
          <SplitItem isFilled>
            <TextContent>
              <Text component="h1">{title}</Text>
            </TextContent>
          </SplitItem>
          <SplitItem>
            <MenuActions actions={menuActions} />
          </SplitItem>
        </Split>
      </StackItem>
    </Stack>
  );
};

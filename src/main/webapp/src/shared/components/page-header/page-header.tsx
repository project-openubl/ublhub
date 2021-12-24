import React from "react";
import {
  Stack,
  StackItem,
  Split,
  SplitItem,
  TextContent,
  Text,
} from "@patternfly/react-core";
import { BreadCrumbPath } from "./breadcrumb-path";
import { MenuActions } from "./menu-actions";
import { HorizontalNav } from "./horizontal-nav";

export interface PageHeaderProps {
  title: string;
  breadcrumbs: { title: string; path: string }[];
  menuActions: { label: string; callback: () => void }[];
  navItems: { title: string; path: string }[];
}

export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  breadcrumbs,
  menuActions,
  navItems,
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
      <StackItem>
        <HorizontalNav navItems={navItems} />
      </StackItem>
    </Stack>
  );
};

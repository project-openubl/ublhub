import React from "react";

import {
  Button,
  DataList,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Flex,
  FlexItem,
} from "@patternfly/react-core";
import { FileCodeIcon, FileImportIcon } from "@patternfly/react-icons";

import { Input } from "api/ublhub";
import { InputData } from "./templates-data";

interface ITemplatesProps {
  onSelect: (input: Input) => void;
}

export const Templates: React.FC<ITemplatesProps> = ({ onSelect }) => {
  return (
    <>
      {/* <Toolbar>
        <ToolbarContent>
          <ToolbarGroup>
            <ToolbarItem>
              <SearchInput onSearch={setFilterText} />
            </ToolbarItem>
          </ToolbarGroup>
        </ToolbarContent>
      </Toolbar> */}

      <DataList aria-label="Templates list" wrapModifier="breakWord" isCompact>
        {InputData.map((item, index) => (
          <DataListItem key={index} aria-labelledby="simple-item1">
            <DataListItemRow>
              <DataListItemCells
                dataListCells={[
                  <DataListCell key="content" width={4} isFilled>
                    <Flex
                      direction={{ default: "column" }}
                      spaceItems={{ default: "spaceItemsMd" }}
                    >
                      <Flex
                        direction={{ default: "column" }}
                        spaceItems={{ default: "spaceItemsNone" }}
                      >
                        <FlexItem>{item.metadata.title}</FlexItem>
                        <FlexItem>
                          <small>{item.metadata.description}</small>
                        </FlexItem>
                      </Flex>
                      <Flex flexWrap={{ default: "wrap" }}>
                        <Flex spaceItems={{ default: "spaceItemsXs" }}>
                          <FlexItem>
                            <FileCodeIcon />
                          </FlexItem>
                          <FlexItem>{item.category}</FlexItem>
                        </Flex>
                      </Flex>
                    </Flex>
                  </DataListCell>,
                  <DataListCell key="actions" alignRight>
                    <Flex justifyContent={{ default: "justifyContentFlexEnd" }}>
                      <FlexItem>
                        <Button
                          variant="tertiary"
                          onClick={() => onSelect(item.input)}
                        >
                          <FileImportIcon /> Import
                        </Button>
                      </FlexItem>
                    </Flex>
                  </DataListCell>,
                ]}
              />
            </DataListItemRow>
          </DataListItem>
        ))}
      </DataList>
    </>
  );
};

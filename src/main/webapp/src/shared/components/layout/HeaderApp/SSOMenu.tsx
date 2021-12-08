import React, { useState } from "react";
import {
  Dropdown,
  DropdownToggle,
  PageHeaderToolsItem,
} from "@patternfly/react-core";

import { useCurrentUserQuery } from "queries/currentUser";

export const SSOMenu: React.FC = () => {
  const currentUser = useCurrentUserQuery();

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const onDropdownSelect = () => {
    setIsDropdownOpen((current) => !current);
  };
  const onDropdownToggle = (isOpen: boolean) => {
    setIsDropdownOpen(isOpen);
  };

  return (
    <PageHeaderToolsItem
      id="user-dropdown"
      visibility={{
        default: "hidden",
        md: "visible",
        lg: "visible",
        xl: "visible",
        "2xl": "visible",
      }} /** this user dropdown is hidden on mobile sizes */
    >
      <Dropdown
        isPlain
        position="right"
        onSelect={onDropdownSelect}
        isOpen={isDropdownOpen}
        toggle={
          <DropdownToggle onToggle={onDropdownToggle}>
            {currentUser.data?.username}
          </DropdownToggle>
        }
        // dropdownItems={[
        //   <DropdownGroup key="user-management">
        //     {components}
        //   </DropdownGroup>,
        // ]}
      />
    </PageHeaderToolsItem>
  );
};

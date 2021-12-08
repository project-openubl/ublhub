import React from "react";
import { DropdownItem } from "@patternfly/react-core";
import { getOidcLogoutPath } from "Constants";

export const OidcMenuDropdownItems: React.FC = () => {
  return (
    <>
      <DropdownItem
        key="logout"
        onClick={() => {
          window.location.replace(getOidcLogoutPath());
        }}
      >
        Logout
      </DropdownItem>
    </>
  );
};

import React from "react";
import { DropdownItem } from "@patternfly/react-core";
import { getAuthFormCookieName } from "Constants";

export const BasicMenuDropdownItems: React.FC = () => {
  const logout = () => {
    document.cookie = `${getAuthFormCookieName()}=; Max-Age=0`;
    window.location.reload();
  };

  return (
    <>  
      <DropdownItem key="logout" onClick={logout}>
        Logout
      </DropdownItem>
    </>
  );
};

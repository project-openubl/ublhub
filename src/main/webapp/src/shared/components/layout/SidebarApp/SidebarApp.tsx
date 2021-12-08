import React from "react";
import { NavLink } from "react-router-dom";

import { Nav, NavItem, PageSidebar, NavGroup } from "@patternfly/react-core";

import { Paths } from "Paths";
import { LayoutTheme } from "../LayoutUtils";

export const SidebarApp: React.FC = () => {
  const renderPageNav = () => {
    return (
      <Nav id="nav-sidebar" aria-label="Nav" theme={LayoutTheme}>
        <NavGroup title="General">
          <NavItem>
            <NavLink to={Paths.namespaces} activeClassName="pf-m-current">
              Namespaces
            </NavLink>
          </NavItem>
        </NavGroup>
      </Nav>
    );
  };

  return <PageSidebar nav={renderPageNav()} theme={LayoutTheme} />;
};

import React from "react";
import { NavLink } from "react-router-dom";

import { Nav, NavItem, PageSidebar, NavList } from "@patternfly/react-core";

import { useNamespaceContext } from "shared/components";

import { documentsPath, Paths } from "Paths";
import { LayoutTheme } from "../LayoutUtils";

export const SidebarApp: React.FC = () => {
  const namespaceContext = useNamespaceContext();

  const renderPageNav = () => {
    return (
      <Nav id="nav-sidebar" aria-label="Nav" theme={LayoutTheme}>
        <NavList title="General">
          <NavItem>
            <NavLink to={Paths.namespaces} activeClassName="pf-m-current">
              Namespaces
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink
              to={
                !namespaceContext
                  ? Paths.documents
                  : documentsPath(namespaceContext)
              }
              activeClassName="pf-m-current"
            >
              Documents
            </NavLink>
          </NavItem>
        </NavList>
      </Nav>
    );
  };

  return <PageSidebar nav={renderPageNav()} theme={LayoutTheme} />;
};

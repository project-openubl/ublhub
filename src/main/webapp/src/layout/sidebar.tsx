import React from "react";
import { NavLink } from "react-router-dom";

import { Nav, PageSidebar, NavList } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import { LayoutTheme } from "./layout-constants";

export const SidebarApp: React.FC = () => {
  const renderPageNav = () => {
    return (
      <Nav id="nav-sidebar" aria-label="Nav" theme={LayoutTheme}>
        <NavList>
          <NavLink
            to="/projects"
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Projects
          </NavLink>
        </NavList>
        {/* <NavList>
          <NavLink
            to={
              !currentContext
                ? "/issues"
                : "/issues/applications/" + currentContext.key
            }
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Issues
          </NavLink>
        </NavList> */}
      </Nav>
    );
  };

  return <PageSidebar nav={renderPageNav()} theme={LayoutTheme} />;
};

import React from "react";
import { NavLink } from "react-router-dom";

import { Nav, NavList, PageSidebar } from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import { useProjectContext } from "shared/context";
import { LayoutTheme } from "./layout-constants";

export const SidebarApp: React.FC = () => {
  const { currentContext } = useProjectContext();

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
            Proyectos
          </NavLink>
        </NavList>
        <NavList>
          <NavLink
            to={
              !currentContext
                ? "/documents"
                : "/documents/projects/" + currentContext.key
            }
            className={({ isActive }) =>
              css("pf-c-nav__link", isActive ? "pf-m-current" : "")
            }
          >
            Documentos
          </NavLink>
        </NavList>
      </Nav>
    );
  };

  return <PageSidebar nav={renderPageNav()} theme={LayoutTheme} />;
};

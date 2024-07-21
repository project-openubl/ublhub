import React, { useReducer } from "react";
import { NavLink, useMatch, useNavigate } from "react-router-dom";

import {
  Button,
  ContextSelector,
  ContextSelectorFooter,
  ContextSelectorItem,
  Nav,
  NavList,
  PageSidebar,
} from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

import { LayoutTheme } from "./layout-constants";

import "./sidebar.css";
import { useProjectsQuery } from "queries/projects";
import { AddProjectWizard } from "shared/components";

export const SidebarApp: React.FC = () => {
  const navigate = useNavigate();
  const routeParams = useMatch("/projects/:projectName/*");

  const projectsQuery = useProjectsQuery();

  const [isCreateProjectWizardOpen, toggleCreateProjectWizard] = useReducer(
    (val) => !val,
    false
  );
  const [isCtxSelectorToggled, toggleCtxSelector] = useReducer(
    (val) => !val,
    false
  );

  const navigateToSelectedProject = (projectName: string) => {
    navigate(`/projects/${projectName}`);
  };

  const renderPageNav = () => {
    return (
      <Nav id="nav-sidebar" aria-label="Nav" theme={LayoutTheme}>
        <div className="project">
          <ContextSelector
            toggleText={"selected"}
            // onSearchInputChange={onSearchInputChange}
            isOpen={isCtxSelectorToggled}
            // searchInputValue={searchValue}
            onToggle={toggleCtxSelector}
            // onSelect={onSelect}
            // onSearchButtonClick={onSearchButtonClick}
            footer={
              <ContextSelectorFooter>
                <Button
                  variant="primary"
                  isBlock
                  onClick={toggleCreateProjectWizard}
                >
                  Crear proyecto
                </Button>
              </ContextSelectorFooter>
            }
          >
            {projectsQuery.data?.map((project) => (
              <ContextSelectorItem
                key={project.name}
                onClick={() => {
                  toggleCtxSelector();
                  navigateToSelectedProject(project.name);
                }}
              >
                {project.name}
              </ContextSelectorItem>
            ))}
          </ContextSelector>

          {isCreateProjectWizardOpen && (
            <AddProjectWizard
              onSave={(project) => {
                toggleCreateProjectWizard();
                navigateToSelectedProject(project.name);
              }}
              onClose={toggleCreateProjectWizard}
            />
          )}
        </div>

        {routeParams?.params.projectName && (
          <>
            <NavList>
              <NavLink
                to={`/projects/${routeParams.params.projectName}/settings`}
                className={({ isActive }) => {
                  return css("pf-c-nav__link", isActive ? "pf-m-current" : "");
                }}
              >
                Configuración
              </NavLink>
              <NavLink
                to={`/projects/${routeParams.params.projectName}/certificates`}
                className={({ isActive }) => {
                  return css("pf-c-nav__link", isActive ? "pf-m-current" : "");
                }}
              >
                Certificados
              </NavLink>
              <NavLink
                to={`/projects/${routeParams.params.projectName}/sunat`}
                className={({ isActive }) => {
                  return css("pf-c-nav__link", isActive ? "pf-m-current" : "");
                }}
              >
                SUNAT
              </NavLink>
              <NavLink
                to={`/projects/${routeParams.params.projectName}/companies`}
                className={({ isActive }) => {
                  return css("pf-c-nav__link", isActive ? "pf-m-current" : "");
                }}
              >
                Empresas
              </NavLink>
              <NavLink
                to={`/projects/${routeParams.params.projectName}/documents`}
                className={({ isActive }) => {
                  return css("pf-c-nav__link", isActive ? "pf-m-current" : "");
                }}
              >
                Documentos
              </NavLink>
            </NavList>
          </>
        )}
      </Nav>
    );
  };

  return <PageSidebar nav={renderPageNav()} theme={LayoutTheme} />;
};

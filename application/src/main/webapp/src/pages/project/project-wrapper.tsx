import React, { useMemo } from "react";
import { Outlet, useMatch } from "react-router-dom";
import { useProjectsQuery } from "queries/projects";

export const ProjectWrapper: React.FC = () => {
  const routeParams = useMatch("/projects/:projectName/*");

  const projectsQuery = useProjectsQuery();
  const project = useMemo(() => {
    const projectName = routeParams?.params.projectName;
    return (
      projectsQuery.data?.find((project) => project.name === projectName) ||
      null
    );
  }, [routeParams?.params, projectsQuery.data]);

  return <Outlet context={project} />;
};

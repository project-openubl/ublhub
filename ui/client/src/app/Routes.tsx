import React, { Suspense, lazy } from "react";
import { useParams, useRoutes } from "react-router-dom";

import { Bullseye, Spinner } from "@patternfly/react-core";

const Home = lazy(() => import("./pages/home"));
const ProjectList = lazy(() => import("./pages/project-list"));
const ProjectDetails = lazy(() => import("./pages/project-details"));

export const ViewRepositoryRouteParam = "repositoryId";
export const ViewPackageRouteParam = "packageId";

export enum PathParam {
  PROJECT_ID = "projectId",
}

export const AppRoutes = () => {
  const allRoutes = useRoutes([
    { path: "/", element: <Home /> },
    { path: "/projects", element: <ProjectList /> },
    { path: `/projects/:${PathParam.PROJECT_ID}`, element: <ProjectDetails /> },
    // { path: "/projects/:projectId/documents", element: <Documents /> },
    // { path: "*", element: <Navigate to="/organizations" /> },
  ]);

  return (
    <Suspense
      fallback={
        <Bullseye>
          <Spinner />
        </Bullseye>
      }
    >
      {allRoutes}
    </Suspense>
  );
};

export const useRouteParams = (pathParam: PathParam) => {
  const params = useParams();
  return params[pathParam];
};

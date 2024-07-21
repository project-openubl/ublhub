import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

const GettingStarted = lazy(() => import("./pages/getting-started"));

const ProjectWrapper = lazy(() => import("./pages/project"));
const ProjectSettings = lazy(() => import("./pages/project/settings"));
const ProjectSunat = lazy(() => import("./pages/project/sunat"));
const ProjectCertificates = lazy(() => import("./pages/project/certificates"));
const ProjectCompanies = lazy(() => import("./pages/project/companies"));
const ProjectDocuments = lazy(() => import("./pages/project/documents"));

export const AppRoutes = () => {
  const routes = [
    {
      Component: GettingStarted,
      path: "/getting-started",
      hasDescendant: false,
    },
    {
      Component: ProjectWrapper,
      path: "/projects/:projectName",
      children: [
        {
          Component: () => <Navigate to="settings" replace />,
          path: "",
        },
        {
          Component: ProjectSettings,
          path: "settings",
        },
        {
          Component: ProjectSunat,
          path: "sunat",
        },
        {
          Component: ProjectCertificates,
          path: "certificates",
        },
        {
          Component: ProjectCompanies,
          path: "companies",
        },
        {
          Component: ProjectDocuments,
          path: "documents",
        },
      ],
    },
  ];

  return (
    <Suspense fallback={<span>Loading...</span>}>
      <Routes>
        {routes.map(({ path, hasDescendant, Component, children }, index) => (
          <Route
            key={index}
            path={!hasDescendant ? path : `${path}/*`}
            element={<Component />}
          >
            {children?.map(
              ({ path: childPath, Component: ChildComponent }, childIndex) => (
                <Route
                  key={childIndex}
                  path={childPath}
                  element={<ChildComponent />}
                />
              )
            )}
          </Route>
        ))}
        <Route path="/" element={<Navigate to="/getting-started" />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Suspense>
  );
};

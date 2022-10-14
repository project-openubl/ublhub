import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";

const ProjectList = lazy(() => import("./pages/project-list"));
const ProjectEdit = lazy(() => import("./pages/project-edit"));
const ProjectEditGeneral = lazy(() => import("./pages/project-edit/general"));
const ProjectEditSunat = lazy(() => import("./pages/project-edit/sunat"));
const ProjectEditCertificates = lazy(() => import("./pages/project-edit/certificates"));
const ProjectEditCompanies = lazy(() => import("./pages/project-edit/companies"));

export const AppRoutes = () => {
  const routes = [
    {
      Component: ProjectList,
      path: "/projects",
      hasDescendant: true,
    },
    {
      Component: ProjectEdit,
      path: "/projects/:projectId",
      children: [
        {
          Component: () => <Navigate to="general" replace />,
          path: "",
        },
        {
          Component: ProjectEditGeneral,
          path: "general",
        },
        {
          Component: ProjectEditSunat,
          path: "sunat",
        },
        {
          Component: ProjectEditCertificates,
          path: "certificates",
        },
        {
          Component: ProjectEditCompanies,
          path: "companies",
        }       
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
        <Route path="/" element={<Navigate to="/projects" />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Suspense>
  );
};

import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import ProjectEditGeneral from "pages/project-edit/general";

const ProjectList = lazy(() => import("./pages/project-list"));
const ProjectEdit = lazy(() => import("./pages/project-edit"));
// const ProjectEditGeneral = lazy(() => import("./pages/project-edit/general"));
const ProjectEditSunat = lazy(() => import("./pages/project-edit/sunat"));
const ProjectEditCertificates = lazy(
  () => import("./pages/project-edit/certificates")
);
const ProjectEditCompanies = lazy(
  () => import("./pages/project-edit/companies")
);

const DocumentList = lazy(() => import("./pages/document-list"));

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
        },
      ],
    },
    {
      Component: ProjectList,
      path: "/projects",
      hasDescendant: true,
    },
    {
      Component: DocumentList,
      path: "/documents",
      hasDescendant: false,
    },
    {
      Component: DocumentList,
      path: "/documents/projects/:projectId",
      hasDescendant: false,
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

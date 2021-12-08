import React, { lazy, Suspense } from "react";
import { Switch, Redirect } from "react-router-dom";

import { SimplePlaceholder } from "@project-openubl/lib-ui";

import { IProtectedRouteProps, ProtectedRoute } from "ProtectedRoute";
import { isElasticsearchEnabled, Permission } from "Constants";

import { Paths } from "./Paths";

const ConsultaRuc = lazy(() => import("./pages/consulta-ruc"));
const Contribuyentes = lazy(() => import("./pages/contribuyentes"));
const Versions = lazy(() => import("./pages/versions"));
const Settings = lazy(() => import("./pages/settings"));

export const AppRoutes = () => {
  const routes: IProtectedRouteProps[] = [
    {
      component: ConsultaRuc,
      path: Paths.consultaRuc,
      exact: false,
      hasAny: [Permission.admin, Permission.search],
    },
    {
      component: Contribuyentes,
      path: Paths.contribuyenteList,
      exact: false,
      hasAny: [Permission.admin, Permission.search],
    },
    {
      component: Versions,
      path: Paths.versionList,
      exact: false,
      hasAny: [Permission.admin, Permission.version_write],
    },
    {
      component: Settings,
      path: Paths.settings,
      exact: false,
      hasAny: [Permission.admin, Permission.user_write],
    },
  ];

  return (
    <Suspense fallback={<SimplePlaceholder />}>
      <Switch>
        {routes.map(({ path, component, ...rest }, index) => (
          <ProtectedRoute
            key={index}
            path={path}
            component={component}
            {...rest}
          />
        ))}
        <Redirect
          from={Paths.base}
          to={
            isElasticsearchEnabled()
              ? Paths.contribuyenteList
              : Paths.consultaRuc
          }
          exact
        />
      </Switch>
    </Suspense>
  );
};

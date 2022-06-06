import React, { lazy, Suspense } from "react";
import { Route, Switch, Redirect, RouteProps } from "react-router-dom";

import { SimplePlaceholder } from "@project-openubl/lib-ui";

import { Paths } from "./Paths";

const Namespaces = lazy(() => import("./pages/namespaces"));
const Documents = lazy(() => import("./pages/documents"));

export const AppRoutes = () => {
  const routes: RouteProps[] = [
    {
      component: Namespaces,
      path: Paths.namespaces,
      exact: false,
    },
    {
      component: Documents,
      path: Paths.documents,
      exact: false,
    },
  ];

  return (
    <Suspense fallback={<SimplePlaceholder />}>
      <Switch>
        {routes.map(({ path, component, ...rest }, index) => (
          <Route key={index} path={path} component={component} {...rest} />
        ))}
        <Redirect from={Paths.base} to={Paths.namespaces} exact />
      </Switch>
    </Suspense>
  );
};

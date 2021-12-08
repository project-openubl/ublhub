import React, { lazy, Suspense } from "react";
import { Switch, Route } from "react-router-dom";

import { SimplePlaceholder } from "@project-openubl/lib-ui";
import { Paths } from "Paths";

const NamespacesList = lazy(() => import("./namespaces-list"));

export const Namespaces: React.FC = () => {
  return (
    <>
      <Suspense fallback={<SimplePlaceholder />}>
        <Switch>
          <Route path={Paths.namespaces} component={NamespacesList} exact />
        </Switch>
      </Suspense>
    </>
  );
};

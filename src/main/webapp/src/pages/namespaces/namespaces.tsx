import React, { lazy, Suspense } from "react";
import { Switch, Route } from "react-router-dom";

import { SimplePlaceholder } from "@project-openubl/lib-ui";
import { Paths } from "Paths";

const NamespacesList = lazy(() => import("./namespaces-list"));
const EditNamespace = lazy(() => import("./edit-namespace"));

export const Namespaces: React.FC = () => {
  return (
    <>
      <Suspense fallback={<SimplePlaceholder />}>
        <Switch>
          <Route path={Paths.namespaces} component={NamespacesList} exact />
          <Route path={Paths.namespaces_edit} component={EditNamespace} />
        </Switch>
      </Suspense>
    </>
  );
};

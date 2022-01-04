import React, { lazy, Suspense } from "react";
import { Switch, Route } from "react-router-dom";

import { SimplePlaceholder } from "@project-openubl/lib-ui";
import { Paths } from "Paths";

const DocumentsList = lazy(() => import("./documents-list"));
const CreateDocument = lazy(() => import("./create-document"));

export const Documents: React.FC = () => {
  return (
    <>
      <Suspense fallback={<SimplePlaceholder />}>
        <Switch>
          <Route path={Paths.documents} component={DocumentsList} exact />
          <Route path={Paths.documents_ns} component={DocumentsList} exact />
          <Route path={Paths.documents_ns_create} component={CreateDocument} />
        </Switch>
      </Suspense>
    </>
  );
};

import React, { lazy, Suspense } from "react";
import { Switch, Route } from "react-router-dom";

import { SimplePlaceholder } from "@project-openubl/lib-ui";
import { Paths } from "Paths";

const VersionList = lazy(() => import("./version-list"));

export const Versions: React.FC = () => {
  return (
    <>
      <Suspense fallback={<SimplePlaceholder />}>
        <Switch>
          <Route path={Paths.versionList} component={VersionList} exact />
        </Switch>
      </Suspense>
    </>
  );
};

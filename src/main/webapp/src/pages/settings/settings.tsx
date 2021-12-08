import React, { lazy, Suspense } from "react";
import { Switch, Route, Redirect } from "react-router-dom";

import { SimplePlaceholder } from "@project-openubl/lib-ui";
import { Paths } from "Paths";

const UserList = lazy(() => import("./user-list"));

export const Settings: React.FC = () => {
  return (
    <>
      <Suspense fallback={<SimplePlaceholder />}>
        <Switch>
          <Route path={Paths.settings_userList} component={UserList} exact />
          <Redirect from={Paths.settings} to={Paths.settings_userList} exact />
        </Switch>
      </Suspense>
    </>
  );
};

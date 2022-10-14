import React from "react";
import { HashRouter } from "react-router-dom";

import {
  ConfirmationContextProvider,
  NotificationContextProvider,
  Notifications,
} from "@project-openubl/lib-ui";

import { AppRoutes } from "./Routes";

import { DefaultLayout } from "./layout";

const App: React.FC = () => {
  return (
    <HashRouter>
      <ConfirmationContextProvider>
        <NotificationContextProvider>
          <DefaultLayout>
            <AppRoutes />
          </DefaultLayout>
          <Notifications />
        </NotificationContextProvider>
      </ConfirmationContextProvider>
    </HashRouter>
  );
};

export default App;

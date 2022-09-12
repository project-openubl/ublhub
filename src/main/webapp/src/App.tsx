import React from "react";
import { HashRouter } from "react-router-dom";

import { AppRoutes } from "./Routes";
import "./App.scss";

import { DefaultLayout } from "./shared/components";

import NotificationsPortal from "@redhat-cloud-services/frontend-components-notifications/NotificationPortal";
import "@redhat-cloud-services/frontend-components-notifications/index.css";

import { ConfirmationContextProvider } from "@project-openubl/lib-ui";

const App: React.FC = () => {
  return (
    <HashRouter>
      <ConfirmationContextProvider>
        <DefaultLayout>
          <AppRoutes />
        </DefaultLayout>
        <NotificationsPortal />
      </ConfirmationContextProvider>
    </HashRouter>
  );
};

export default App;
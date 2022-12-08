import React from "react";
import { HashRouter } from "react-router-dom";

import {
  ConfirmationContextProvider,
  NotificationContextProvider,
  Notifications,
} from "@project-openubl/lib-ui";

import { AppRoutes } from "./Routes";

import { ProjectContextProvider } from "shared/context";
import { useProjectsQuery } from "queries/projects";
import { DefaultLayout } from "./layout";

const App: React.FC = () => {
  const projectsQuery = useProjectsQuery();

  return (
    <HashRouter>
      <ProjectContextProvider
        allContexts={(projectsQuery.data || []).map((e) => ({
          key: e.id!,
          label: e.name,
        }))}
      >
        <ConfirmationContextProvider>
          <NotificationContextProvider>
            <DefaultLayout>
              <AppRoutes />
            </DefaultLayout>
            <Notifications />
          </NotificationContextProvider>
        </ConfirmationContextProvider>
      </ProjectContextProvider>
    </HashRouter>
  );
};

export default App;

import React, { useState } from "react";
import { useParams } from "react-router-dom";
import {
  Card,
  CardBody,
  Tab,
  TabContentBody,
  Tabs,
  TabTitleText,
} from "@patternfly/react-core";

import { INamespaceParams } from "../edit-namespace";
import { WebServicesForm } from "./web-services-form";
import { CredentialsForm } from "./credentials-form";

enum TabKey {
  ServiciosWeb,
  Credenciales,
}

export const Sunat: React.FC = () => {
  const routeParams = useParams<INamespaceParams>();
  const namespaceId = routeParams.namespaceId;

  const [activeTabKey, setActiveTabKey] = useState<string | number>(
    TabKey.ServiciosWeb
  );

  return (
    <Card>
      <CardBody>
        <Tabs
          activeKey={activeTabKey}
          onSelect={(_, key) => setActiveTabKey(key)}
        >
          <Tab
            eventKey={TabKey.ServiciosWeb}
            title={<TabTitleText>Servicios web</TabTitleText>}
          >
            <TabContentBody>
              <WebServicesForm namespaceId={namespaceId} />
            </TabContentBody>
          </Tab>
          <Tab
            eventKey={TabKey.Credenciales}
            title={<TabTitleText>Credenciales</TabTitleText>}
          >
            <TabContentBody>
              <CredentialsForm namespaceId={namespaceId} />
            </TabContentBody>
          </Tab>
        </Tabs>
      </CardBody>
    </Card>
  );
};

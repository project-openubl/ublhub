import React from "react";
import { Link } from "react-router-dom";

import {
    Breadcrumb,
    BreadcrumbItem,
    PageSection,
    PageSectionVariants,
    Tab,
    TabTitleText,
    Tabs,
    Text,
    TextContent
} from "@patternfly/react-core";

import {
    useFetchProjectById
} from "@app/queries/projects";

import { PathParam, useRouteParams } from "@app/Routes";
import { CredentialsTable } from "./credentials/credentials-table";

export const ProjectDetails: React.FC = () => {
  const projectId = useRouteParams(PathParam.PROJECT_ID);
  const { project } = useFetchProjectById(projectId);

  return (
    <>
      <PageSection variant={PageSectionVariants.light} type="breadcrumb">
        <Breadcrumb>
          <BreadcrumbItem key="projects">
            <Link to="/projects">Proyectos</Link>
          </BreadcrumbItem>
          <BreadcrumbItem to="#" isActive>
            Proyecto detalles
          </BreadcrumbItem>
        </Breadcrumb>
      </PageSection>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">{project?.name}</Text>
        </TextContent>

        <Tabs defaultActiveKey={0} aria-label="CVE tabs" role="region">
          <Tab eventKey={0} title={<TabTitleText>Documentos</TabTitleText>}>
            documentos
          </Tab>
          <Tab eventKey={1} title={<TabTitleText>Credenciales</TabTitleText>}>
            {projectId && <CredentialsTable projectId={projectId} />}
          </Tab>
        </Tabs>
      </PageSection>
    </>
  );
};

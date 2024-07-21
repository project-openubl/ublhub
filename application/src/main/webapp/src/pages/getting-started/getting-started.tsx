import React from "react";

import {
  Bullseye,
  Button,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateSecondaryActions,
  SelectVariant,
  Title,
} from "@patternfly/react-core";

import FileCodeIcon from "@patternfly/react-icons/dist/esm/icons/file-code-icon";
import { SimpleSelect, useModal } from "@project-openubl/lib-ui";
import { ProjectDto } from "api/models";
import { AddProjectWizard } from "shared/components";
import { useNavigate } from "react-router-dom";
import { useProjectsQuery } from "queries/projects";

export const GettingStarted: React.FC = () => {
  const navigate = useNavigate();
  const projectModal = useModal<"ADD", ProjectDto>();

  const projectsQuery = useProjectsQuery();

  return (
    <>
      <Bullseye>
        <EmptyState>
          <EmptyStateIcon icon={FileCodeIcon} />
          <Title headingLevel="h4" size="lg">
            Bienvenido a Ublhub
          </Title>
          <EmptyStateBody>
            Selecciona un proyecto o crea uno nuevo.
          </EmptyStateBody>
          <Button variant="primary" onClick={() => projectModal.open("ADD")}>
            Crear proyecto
          </Button>
          <EmptyStateSecondaryActions>
            <SimpleSelect
              toggleId="project-list-toggle"
              variant={SelectVariant.single}
              aria-label="Select project"
              id="project-list"
              options={(projectsQuery.data || []).map((elem) => elem.name)}
              onChange={(selection) => {
                navigate(`/projects/${selection}`);
              }}
            />
          </EmptyStateSecondaryActions>
        </EmptyState>
      </Bullseye>
      {projectModal.isOpen && projectModal.isAction("ADD") && (
        <AddProjectWizard
          onSave={(project) => {
            projectModal.close();
            navigate(`/projects/${project.name}`);
          }}
          onClose={projectModal.close}
        />
      )}
    </>
  );
};

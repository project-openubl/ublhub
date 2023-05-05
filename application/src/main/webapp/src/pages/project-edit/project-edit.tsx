import React, { useMemo } from "react";
import { Outlet, useMatch, useNavigate } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";

import { useConfirmationContext } from "@project-openubl/lib-ui";

import { ButtonVariant, PageSection } from "@patternfly/react-core";

import { useDeleteProjectMutation, useProjectsQuery } from "queries/projects";
import { HorizontalNav, PageHeader } from "shared/components";

export const ProjectEdit: React.FC = () => {
  const { t } = useTranslation();

  const navigate = useNavigate();

  const routeParams = useMatch("/projects/:projectName/*");
  const confirmationModal = useConfirmationContext();

  const projectsQuery = useProjectsQuery();
  const project = useMemo(() => {
    const projectName = routeParams?.params.projectName;
    return (
      projectsQuery.data?.find((project) => project.name === projectName) || null
    );
  }, [routeParams?.params, projectsQuery.data]);

  const deleteProject = useDeleteProjectMutation(() => {
    confirmationModal.close();
    navigate("/");
  });

  const onDeleteProject = () => {
    if (!project) {
      console.log("Can not delete null");
      return;
    }

    confirmationModal.open({
      title: t("modal.confirm-delete.title", {
        what: t("terms.project"),
      }),
      titleIconVariant: "warning",
      message: (
        <Trans
          i18nKey="modal.confirm-delete.body"
          values={{
            type: t("terms.project"),
            name: project.name,
          }}
        >
          ¿Estas seguro de querer eliminar este(a) <b>type</b>? Esta acción
          eliminará <b>name</b> permanentemente.
        </Trans>
      ),
      confirmBtnVariant: ButtonVariant.danger,
      confirmBtnLabel: t("actions.delete"),
      cancelBtnLabel: t("actions.cancel"),
      onConfirm: () => {
        confirmationModal.enableProcessing();
        deleteProject.mutate(project);
      },
    });
  };

  return (
    <>
      <PageSection variant="light" type="breadcrumb">
        <PageHeader
          title={project?.name || ""}
          breadcrumbs={[
            {
              title: t("terms.projects"),
              path: "/projects",
            },
            {
              title: t("actions.edit"),
              path: "",
            },
          ]}
          menuActions={[
            { label: t("actions.delete"), callback: onDeleteProject },
          ]}
        />
      </PageSection>
      <PageSection variant="light" type="tabs">
        <HorizontalNav
          navItems={[
            {
              title: "General",
              path: `/projects/${project?.name}/general`,
            },
            {
              title: "SUNAT",
              path: `/projects/${project?.name}/sunat`,
            },
            {
              title: t("terms.certificates"),
              path: `/projects/${project?.name}/certificates`,
            },
            {
              title: t("terms.companies"),
              path: `/projects/${project?.name}/companies`,
            },
          ]}
        />
      </PageSection>
      <PageSection variant="light" className="pf-u-p-0">
        <Outlet context={project} />
      </PageSection>
    </>
  );
};

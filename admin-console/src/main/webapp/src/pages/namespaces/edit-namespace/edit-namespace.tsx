import React, { lazy, Suspense } from "react";
import { Route, Switch, useHistory, useParams } from "react-router-dom";
import { useTranslation, Trans } from "react-i18next";
import { ButtonVariant, PageSection } from "@patternfly/react-core";
import {
  useConfirmationContext,
  SimplePlaceholder,
} from "@project-openubl/lib-ui";

import { useDispatch } from "react-redux";
import { alertActions } from "store/alert";

import { PageHeader } from "shared/components";

import {
  useDeleteNamespaceMutation,
  useNamespacesQuery,
} from "queries/namespaces";

import { formatPath, Paths } from "Paths";
import { getAxiosErrorMessage } from "utils/modelUtils";

const Overview = lazy(() => import("./overview"));
const Sunat = lazy(() => import("./sunat"));
const Keys = lazy(() => import("./keys"));
const Companies = lazy(() => import("./companies"));

export interface INamespaceParams {
  namespaceId: string;
}

export const EditNamespace: React.FC = () => {
  const { t } = useTranslation();

  const dispatch = useDispatch();
  const history = useHistory();

  const routeParams = useParams<INamespaceParams>();
  const confirmationModal = useConfirmationContext();

  const namespacesQuery = useNamespacesQuery();

  const prefillNamespaceId = routeParams.namespaceId;
  const namespaceBeingPrefilled =
    namespacesQuery.data?.find((ns) => ns.id === prefillNamespaceId) || null;

  const deleteNamespace = useDeleteNamespaceMutation();

  const onDeleteNs = () => {
    if (!namespaceBeingPrefilled) {
      console.log("Can not delete null");
      return;
    }

    confirmationModal.open({
      title: t("modal.confirm-delete.title", {
        what: "namespace",
      }),
      titleIconVariant: "warning",
      message: (
        <Trans
          i18nKey="modal.confirm-delete.body"
          values={{ type: "namespace", name: namespaceBeingPrefilled.name }}
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
        deleteNamespace
          .mutateAsync(namespaceBeingPrefilled)
          .catch((error) => {
            dispatch(
              alertActions.addAlert(
                "danger",
                "Error",
                getAxiosErrorMessage(error)
              )
            );
          })
          .finally(() => {
            confirmationModal.close();
            history.push(Paths.namespaces);
          });
      },
    });
  };

  return (
    <>
      <PageSection variant="light" type="breadcrumb">
        <PageHeader
          title={namespaceBeingPrefilled?.name || ""}
          breadcrumbs={[
            {
              title: "Namespaces",
              path: Paths.namespaces,
            },
            {
              title: t("actions.edit"),
              path: "",
            },
          ]}
          menuActions={[{ label: t("actions.delete"), callback: onDeleteNs }]}
          navItems={[
            {
              title: t("terms.general"),
              path: formatPath(Paths.namespaces_edit, {
                namespaceId: namespaceBeingPrefilled?.id,
              }),
            },
            {
              title: "SUNAT",
              path: formatPath(Paths.namespaces_edit_sunat, {
                namespaceId: namespaceBeingPrefilled?.id,
              }),
            },
            {
              title: t("terms.keys"),
              path: formatPath(Paths.namespaces_edit_keys, {
                namespaceId: namespaceBeingPrefilled?.id,
              }),
            },
            {
              title: t("terms.companies"),
              path: formatPath(Paths.namespaces_edit_companies, {
                namespaceId: namespaceBeingPrefilled?.id,
              }),
            },
          ]}
        />
      </PageSection>
      <PageSection>
        <Suspense fallback={<SimplePlaceholder />}>
          <Switch>
            <Route path={Paths.namespaces_edit} component={Overview} exact />
            <Route path={Paths.namespaces_edit_sunat} component={Sunat} />
            <Route path={Paths.namespaces_edit_keys} component={Keys} />
            <Route
              path={Paths.namespaces_edit_companies}
              component={Companies}
            />
          </Switch>
        </Suspense>
      </PageSection>
    </>
  );
};

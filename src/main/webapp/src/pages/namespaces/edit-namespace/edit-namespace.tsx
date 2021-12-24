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
  useNamespaceQuery,
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

  const namespace = useNamespaceQuery(routeParams.namespaceId);
  const deleteNamespace = useDeleteNamespaceMutation();

  const onDeleteNs = () => {
    if (!namespace.data) {
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
          values={{ type: "namespace", name: namespace.data.name }}
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
          .mutateAsync(namespace.data)
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
          title={namespace.data?.name || ""}
          breadcrumbs={[
            {
              title: "Namespaces",
              path: Paths.namespaces,
            },
            {
              title: "editar",
              path: "",
            },
          ]}
          menuActions={[{ label: "Eliminar", callback: onDeleteNs }]}
          navItems={[
            {
              title: "General",
              path: formatPath(Paths.namespaces_edit, {
                namespaceId: namespace.data?.id,
              }),
            },
            {
              title: "SUNAT",
              path: formatPath(Paths.namespaces_edit_sunat, {
                namespaceId: namespace.data?.id,
              }),
            },
            {
              title: "Certificados",
              path: formatPath(Paths.namespaces_edit_keys, {
                namespaceId: namespace.data?.id,
              }),
            },
            {
              title: "Empresas",
              path: formatPath(Paths.namespaces_edit_companies, {
                namespaceId: namespace.data?.id,
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

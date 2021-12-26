import React from "react";
import * as yup from "yup";
import { UseQueryResult } from "react-query";
import { useTranslation } from "react-i18next";
import { useFormField, useFormState } from "@konveyor/lib-ui";
import {
  Button,
  Wizard,
  WizardContextConsumer,
  WizardFooter,
  WizardStep,
} from "@patternfly/react-core";

import {
  getNamespaceNameSchema,
  useCreateNamespaceMutation,
  useNamespacesQuery,
} from "queries/namespaces";

import { Namespace } from "api/models";

import { WizardStepContainer } from "./wizard-step-container";

import { GeneralForm } from "./general-form";
import { WebServicesForm } from "./web-services-form";
import { CredentialsForm } from "./credentials-form";
import { Review } from "./review";
import { useHistory } from "react-router-dom";
import { formatPath, Paths } from "Paths";

const useNamespaceWizardFormState = (
  namespacesQuery: UseQueryResult<Namespace[]>
) => {
  const forms = {
    general: useFormState({
      name: useFormField("", getNamespaceNameSchema(namespacesQuery, null)),
      description: useFormField("", yup.string().trim().max(250)),
    }),
    webServices: useFormState({
      urlFactura: useFormField<string>(
        "",
        yup.string().required().trim().url().max(250)
      ),
      urlGuiaRemision: useFormField<string>(
        "",
        yup.string().required().trim().url().max(250)
      ),
      urlPercepcionRetencion: useFormField<string>(
        "",
        yup.string().required().trim().url().max(250)
      ),
    }),
    credentials: useFormState({
      sunatUsername: useFormField<string>(
        "",
        yup.string().trim().min(3).max(250)
      ),
      sunatPassword: useFormField<string>("", yup.string().min(3).max(250)),
    }),
  };

  return {
    ...forms,
    isSomeFormDirty: (Object.keys(forms) as (keyof typeof forms)[]).some(
      (key) => forms[key].isDirty
    ),
  };
};

export type NamespaceWizardFormState = ReturnType<
  typeof useNamespaceWizardFormState
>;

enum StepId {
  General = 0,
  WebServices,
  Credentials,
  Review,
}

interface IAddNamespaceWizardProps {
  onClose: () => void;
}

export const AddNamespaceWizard: React.FC<IAddNamespaceWizardProps> = ({
  onClose,
}) => {
  const { t } = useTranslation();
  const history = useHistory();

  const createNamespaceMutation = useCreateNamespaceMutation((ns) => {
    history.push(formatPath(Paths.namespaces_edit, { namespaceId: ns.id }));
    onClose();
  });

  // Form
  const namespacesQuery = useNamespacesQuery();
  const forms = useNamespaceWizardFormState(namespacesQuery);

  const stepForms = [forms.general, forms.webServices, forms.credentials];
  const firstInvalidFormIndex = stepForms.findIndex((form) => !form.isValid);
  const stepIdReached: StepId =
    firstInvalidFormIndex === -1 ? StepId.Review : firstInvalidFormIndex;

  const allMutationResults = [createNamespaceMutation];
  const allMutationErrorTitles = ["Can not create namespace"];

  const steps: WizardStep[] = [
    {
      id: StepId.General,
      name: t("terms.general"),
      component: (
        <WizardStepContainer title={t("terms.general")}>
          <GeneralForm form={forms.general} />
        </WizardStepContainer>
      ),
      enableNext: forms.general.isValid && !createNamespaceMutation.isLoading,
      canJumpTo: !createNamespaceMutation.isLoading,
    },
    {
      name: "SUNAT",
      steps: [
        {
          id: StepId.WebServices,
          name: t("terms.webServices"),
          component: (
            <WizardStepContainer title={t("terms.webServices")}>
              <WebServicesForm form={forms.webServices} />
            </WizardStepContainer>
          ),
          enableNext:
            forms.webServices.isValid && !createNamespaceMutation.isLoading,
          canJumpTo:
            stepIdReached >= StepId.WebServices &&
            !createNamespaceMutation.isLoading,
        },
        {
          id: StepId.Credentials,
          name: t("terms.credentials"),
          component: (
            <WizardStepContainer title={t("terms.credentials")}>
              <CredentialsForm form={forms.credentials} />
            </WizardStepContainer>
          ),
          enableNext:
            forms.credentials.isValid && !createNamespaceMutation.isLoading,
          canJumpTo:
            stepIdReached >= StepId.Credentials &&
            !createNamespaceMutation.isLoading,
        },
      ],
    },
    {
      id: StepId.Review,
      name: t("terms.review"),
      component: (
        <WizardStepContainer title={t("terms.review")}>
          <Review
            forms={forms}
            allMutationResults={allMutationResults}
            allMutationErrorTitles={allMutationErrorTitles}
          />
        </WizardStepContainer>
      ),
      nextButtonText: t("actions.create"),
      enableNext: !createNamespaceMutation.isLoading,
      canJumpTo:
        stepIdReached >= StepId.Review && !createNamespaceMutation.isLoading,
    },
  ];

  const footer = (
    <WizardFooter>
      <WizardContextConsumer>
        {({ activeStep, onNext, onBack, onClose }) => {
          return (
            <>
              <Button
                variant="primary"
                type="button"
                onClick={onNext}
                isDisabled={
                  !activeStep.enableNext || createNamespaceMutation.isLoading
                }
              >
                {activeStep.nextButtonText || t("actions.next")}
              </Button>
              <Button
                variant="secondary"
                onClick={onBack}
                className={
                  activeStep.id === StepId.General ? "pf-m-disabled" : ""
                }
                isDisabled={createNamespaceMutation.isLoading}
              >
                {t("actions.back")}
              </Button>
              <Button
                variant="link"
                onClick={onClose}
                isDisabled={createNamespaceMutation.isLoading}
              >
                {t("actions.cancel")}
              </Button>
            </>
          );
        }}
      </WizardContextConsumer>
    </WizardFooter>
  );

  const onSave = () => {
    createNamespaceMutation.mutate({
      name: forms.general.values.name,
      description: forms.general.values.description,
      webServices: {
        factura: forms.webServices.values.urlFactura,
        guia: forms.webServices.values.urlGuiaRemision,
        retenciones: forms.webServices.values.urlPercepcionRetencion,
      },
      credentials: {
        username: forms.credentials.values.sunatUsername,
        password: forms.credentials.values.sunatPassword,
      },
    });
  };

  return (
    <Wizard
      isOpen
      title={t("actions.create-object", { what: "namespace" })}
      steps={steps}
      onSubmit={(event) => event.preventDefault()}
      onSave={onSave}
      onClose={onClose}
      footer={footer}
    />
  );
};

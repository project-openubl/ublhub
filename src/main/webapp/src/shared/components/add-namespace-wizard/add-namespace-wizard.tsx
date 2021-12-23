import React from "react";
import * as yup from "yup";
import { UseQueryResult } from "react-query";
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
        yup.string().trim().min(1).max(250)
      ),
      sunatPassword: useFormField<string>("", yup.string().min(1).max(250)),
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
  const createPlanMutation = useCreateNamespaceMutation(onClose);

  // Form
  const namespacesQuery = useNamespacesQuery();
  const forms = useNamespaceWizardFormState(namespacesQuery);

  const stepForms = [forms.general, forms.webServices, forms.credentials];
  const firstInvalidFormIndex = stepForms.findIndex((form) => !form.isValid);
  const stepIdReached: StepId =
    firstInvalidFormIndex === -1 ? StepId.Review : firstInvalidFormIndex;

  const steps: WizardStep[] = [
    {
      id: StepId.General,
      name: "General",
      component: (
        <WizardStepContainer title="Configuración general">
          <GeneralForm form={forms.general} />
        </WizardStepContainer>
      ),
      enableNext: forms.general.isValid && !createPlanMutation.isLoading,
      canJumpTo: !createPlanMutation.isLoading,
    },
    {
      name: "SUNAT",
      steps: [
        {
          id: StepId.WebServices,
          name: "Servicios web",
          component: (
            <WizardStepContainer title="Servicios web">
              <WebServicesForm form={forms.webServices} />
            </WizardStepContainer>
          ),
          enableNext:
            forms.webServices.isValid && !createPlanMutation.isLoading,
          canJumpTo:
            stepIdReached >= StepId.WebServices &&
            !createPlanMutation.isLoading,
        },
        {
          id: StepId.Credentials,
          name: "Credenciales",
          component: (
            <WizardStepContainer title="Credenciales">
              <CredentialsForm form={forms.credentials} />
            </WizardStepContainer>
          ),
          enableNext:
            forms.credentials.isValid && !createPlanMutation.isLoading,
          canJumpTo:
            stepIdReached >= StepId.Credentials &&
            !createPlanMutation.isLoading,
        },
      ],
    },
    {
      id: StepId.Review,
      name: "Revisión",
      component: (
        <WizardStepContainer title="Revisión del namespace">
          <Review forms={forms} />
        </WizardStepContainer>
      ),
      nextButtonText: "Finalizar",
      enableNext: !createPlanMutation.isLoading,
      canJumpTo:
        stepIdReached >= StepId.Review && !createPlanMutation.isLoading,
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
                  !activeStep.enableNext || createPlanMutation.isLoading
                }
              >
                {activeStep.nextButtonText || "Siguiente"}
              </Button>
              <Button
                variant="secondary"
                onClick={onBack}
                className={
                  activeStep.id === StepId.General ? "pf-m-disabled" : ""
                }
                isDisabled={createPlanMutation.isLoading}
              >
                Atrás
              </Button>
              <Button
                variant="link"
                onClick={onClose}
                isDisabled={createPlanMutation.isLoading}
              >
                Cancelar
              </Button>
            </>
          );
        }}
      </WizardContextConsumer>
    </WizardFooter>
  );

  const onSave = () => {
    createPlanMutation.mutate({
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
      title="Crear namespace"
      steps={steps}
      onSubmit={(event) => event.preventDefault()}
      onSave={onSave}
      onClose={onClose}
      footer={footer}
    />
  );
};

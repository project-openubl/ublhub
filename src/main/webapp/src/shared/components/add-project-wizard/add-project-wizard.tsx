import React from "react";
import axios, { AxiosError } from "axios";
import { useTranslation } from "react-i18next";

import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { object, string,  } from "yup";

import {
  Button,
  Wizard,
  WizardContextConsumer,
  WizardFooter,
  WizardStep,
} from "@patternfly/react-core";

import { useCreateProjectMutation } from "queries/projects";

import { ProjectDto } from "api/models";
import {
  WebServicesForm,
  CredentialsForm,
  IWebServicesForm,
  ICredentialsForm,
  SchemaFormWebServices,
  SchemaFormCredentials,
} from "shared/components";

import { WizardStepContainer } from "./wizard-step-container";
import { GeneralForm } from "./general-form";
import { Review } from "./review";

enum StepId {
  General = 0,
  WebServices,
  Credentials,
  Review,
}

export interface IGeneralForm {
  name: string;
  description: string;
}

interface IAddProjectWizardProps {
  onSave: (instance: ProjectDto) => void;
  onClose: () => void;
}

export const AddProjectWizard: React.FC<IAddProjectWizardProps> = ({
  onSave,
  onClose,
}) => {
  const { t } = useTranslation();

  const createProjectMutation = useCreateProjectMutation((project) => {
    onSave(project);
  });

  const formGeneral = useForm<IGeneralForm>({
    defaultValues: { name: "", description: "" },
    resolver: yupResolver(
      object().shape({
        name: string()
          .trim()
          .required()
          .max(250)
          .test("duplicateName", (value, options) => {
            return axios
              .post<string>("/projects/check-name", { name: value })
              .then(() => true)
              .catch((error: AxiosError) =>
                options.createError({ message: error.response?.data })
              );
          }),
        description: string().trim().max(250),
      })
    ),
    mode: "onChange",
  });

  const formWebServices = useForm<IWebServicesForm>({
    defaultValues: { factura: "", guia: "", retencion: "" },
    resolver: yupResolver(SchemaFormWebServices),
    mode: "onChange",
  });

  const formCredentials = useForm<ICredentialsForm>({
    defaultValues: { username: "", password: "" },
    resolver: yupResolver(SchemaFormCredentials),
    mode: "onChange",
  });

  const forms = {
    general: formGeneral,
    webServices: formWebServices,
    credentials: formCredentials,
  };

  const stepForms = [forms.general, forms.webServices, forms.credentials];
  const firstInvalidFormIndex = stepForms.findIndex(
    (form) => !form.formState.isValid
  );
  const stepIdReached: StepId =
    firstInvalidFormIndex === -1 ? StepId.Review : firstInvalidFormIndex;

  const steps: WizardStep[] = [
    {
      id: StepId.General,
      name: t("terms.general"),
      component: (
        <WizardStepContainer title={t("terms.general")}>
          <GeneralForm form={forms.general} />
        </WizardStepContainer>
      ),
      enableNext:
        forms.general.formState.isValid &&
        !forms.general.formState.isValidating &&
        !createProjectMutation.isLoading,
      canJumpTo: !createProjectMutation.isLoading,
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
            forms.webServices.formState.isValid &&
            !createProjectMutation.isLoading,
          canJumpTo:
            stepIdReached >= StepId.WebServices &&
            !createProjectMutation.isLoading,
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
            forms.credentials.formState.isValid &&
            !createProjectMutation.isLoading,
          canJumpTo:
            stepIdReached >= StepId.Credentials &&
            !createProjectMutation.isLoading,
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
            allMutationResultsWithErrorTitles={[
              {
                result: createProjectMutation,
                errorTitle: "Can not create project",
              },
            ]}
          />
        </WizardStepContainer>
      ),
      nextButtonText: t("actions.create"),
      enableNext: !createProjectMutation.isLoading,
      canJumpTo:
        stepIdReached >= StepId.Review && !createProjectMutation.isLoading,
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
                  !activeStep.enableNext || createProjectMutation.isLoading
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
                isDisabled={createProjectMutation.isLoading}
              >
                {t("actions.back")}
              </Button>
              <Button
                variant="link"
                onClick={onClose}
                isDisabled={createProjectMutation.isLoading}
              >
                {t("actions.cancel")}
              </Button>
            </>
          );
        }}
      </WizardContextConsumer>
    </WizardFooter>
  );

  const onSaveForm = () => {
    createProjectMutation.mutate({
      name: forms.general.getValues().name,
      description: forms.general.getValues().description,
      sunat: {
        facturaUrl: forms.webServices.getValues().factura,
        guiaUrl: forms.webServices.getValues().guia,
        retencionUrl: forms.webServices.getValues().retencion,
        username: forms.credentials.getValues().username,
        password: forms.credentials.getValues().password,
      },
    });
  };

  return (
    <Wizard
      isOpen
      title={t("actions.create-object", { what: t("terms.project") })}
      steps={steps}
      onSubmit={(event) => event.preventDefault()}
      onSave={onSaveForm}
      onClose={onClose}
      footer={footer}
    />
  );
};

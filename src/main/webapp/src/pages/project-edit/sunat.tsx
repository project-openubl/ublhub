import React, { useEffect } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Controller, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { object, string } from "yup";

import {
  ActionGroup,
  Button,
  Card,
  CardBody,
  Form,
  FormGroup,
  FormSection,
  TextInput,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { ResolvedQueries } from "@migtools/lib-ui";
import { NotificationContext } from "@project-openubl/lib-ui";

import { ProjectDto } from "api/models";
import { useUpdateProjectMutation } from "queries/projects";
import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";

interface ISunatForm {
  facturaURL: string;
  guiaURL: string;
  retencionURL: string;
  username: string;
  password: string;
}

const Sunat: React.FC = () => {
  const { t } = useTranslation();

  const navigate = useNavigate();
  const { pushNotification } = React.useContext(NotificationContext);

  const project = useOutletContext<ProjectDto | null>();
  const updateProjectMutation = useUpdateProjectMutation((p) => {
    pushNotification({
      title: t("info.data-saved"),
      message: "",
      key: p.name,
      variant: "success",
      actionClose: true,
      timeout: 4000,
    });
  });

  const {
    control,
    formState: { errors, isValid, isValidating, isDirty },
    reset,
    getValues,
  } = useForm<ISunatForm>({
    defaultValues: {
      facturaURL: "",
      guiaURL: "",
      retencionURL: "",
      username: "",
      password: "",
    },
    resolver: yupResolver(
      object().shape({
        facturaURL: string().trim().required().max(250),
        guiaURL: string().trim().required().max(250),
        retencionURL: string().trim().required().max(250),
        username: string().trim().required().max(250),
        password: string().trim().required().max(250),
      })
    ),
    mode: "onChange",
  });

  useEffect(() => {
    if (project) {
      reset({
        facturaURL: project.sunat.facturaUrl,
        guiaURL: project.sunat.guiaUrl,
        retencionURL: project.sunat.retencionUrl,
        username: project.sunat.username,
        password: "******",
      });
    }
  }, [project, reset]);

  const save = () => {
    if (!project) {
      return;
    }

    updateProjectMutation.mutate({
      ...project,
      sunat: {
        facturaUrl: getValues().facturaURL,
        guiaUrl: getValues().guiaURL,
        retencionUrl: getValues().retencionURL,
        username: getValues().username,
        password: control.getFieldState("password").isDirty
          ? getValues().password
          : undefined,
      },
    });
  };

  return (
    <Card isPlain>
      <CardBody>
        <Form className={spacing.pbXl}>
          <FormSection title={t("terms.webServices")}>
            <FormGroup
              label="Factura"
              fieldId="factura"
              isRequired={true}
              validated={getValidatedFromError(errors.facturaURL)}
              helperTextInvalid={errors.facturaURL?.message}
            >
              <Controller
                control={control}
                name="facturaURL"
                render={({
                  field: { onChange, onBlur, value, name },
                  fieldState: { isTouched, error },
                }) => (
                  <TextInput
                    type="text"
                    name={name}
                    aria-label="factura"
                    aria-describedby="factura"
                    isRequired={false}
                    onChange={onChange}
                    onBlur={onBlur}
                    value={value}
                    validated={getValidatedFromErrorTouched(error, isTouched)}
                  />
                )}
              />
            </FormGroup>
            <FormGroup
              label="Guia"
              fieldId="guia"
              isRequired={true}
              validated={getValidatedFromError(errors.guiaURL)}
              helperTextInvalid={errors.guiaURL?.message}
            >
              <Controller
                control={control}
                name="guiaURL"
                render={({
                  field: { onChange, onBlur, value, name },
                  fieldState: { isTouched, error },
                }) => (
                  <TextInput
                    type="text"
                    name={name}
                    aria-label="guia"
                    aria-describedby="guia"
                    isRequired={false}
                    onChange={onChange}
                    onBlur={onBlur}
                    value={value}
                    validated={getValidatedFromErrorTouched(error, isTouched)}
                  />
                )}
              />
            </FormGroup>
            <FormGroup
              label="Retención"
              fieldId="retencion"
              isRequired={true}
              validated={getValidatedFromError(errors.retencionURL)}
              helperTextInvalid={errors.retencionURL?.message}
            >
              <Controller
                control={control}
                name="retencionURL"
                render={({
                  field: { onChange, onBlur, value, name },
                  fieldState: { isTouched, error },
                }) => (
                  <TextInput
                    type="text"
                    name={name}
                    aria-label="retencion"
                    aria-describedby="retencion"
                    isRequired={false}
                    onChange={onChange}
                    onBlur={onBlur}
                    value={value}
                    validated={getValidatedFromErrorTouched(error, isTouched)}
                  />
                )}
              />
            </FormGroup>
          </FormSection>

          <FormSection title={t("terms.credentials")}>
            <FormGroup
              label={t("terms.username")}
              fieldId="username"
              isRequired={true}
              validated={getValidatedFromError(errors.username)}
              helperTextInvalid={errors.username?.message}
            >
              <Controller
                control={control}
                name="username"
                render={({
                  field: { onChange, onBlur, value, name },
                  fieldState: { isTouched, error },
                }) => (
                  <TextInput
                    type="text"
                    name={name}
                    aria-label="username"
                    aria-describedby="username"
                    isRequired={false}
                    onChange={onChange}
                    onBlur={onBlur}
                    value={value}
                    validated={getValidatedFromErrorTouched(error, isTouched)}
                  />
                )}
              />
            </FormGroup>
            <FormGroup
              label={t("terms.password")}
              fieldId="password"
              isRequired={true}
              validated={getValidatedFromError(errors.password)}
              helperTextInvalid={errors.password?.message}
            >
              <Controller
                control={control}
                name="password"
                render={({
                  field: { onChange, onBlur, value, name },
                  fieldState: { isTouched, error },
                }) => (
                  <TextInput
                    type="password"
                    name={name}
                    aria-label="password"
                    aria-describedby="password"
                    isRequired={false}
                    onChange={onChange}
                    onBlur={onBlur}
                    value={value}
                    validated={getValidatedFromErrorTouched(error, isTouched)}
                  />
                )}
              />
            </FormGroup>
          </FormSection>

          <ActionGroup>
            <Button
              variant="primary"
              onClick={save}
              isDisabled={
                !isDirty ||
                !isValid ||
                isValidating ||
                updateProjectMutation.isLoading
              }
            >
              {t("actions.save")}
            </Button>
            <Button variant="link" onClick={() => navigate("/projects")}>
              {t("actions.cancel")}
            </Button>
          </ActionGroup>

          <ResolvedQueries
            resultsWithErrorTitles={[
              {
                result: updateProjectMutation,
                errorTitle: "Could not save data",
              },
            ]}
            spinnerMode="inline"
          />
        </Form>
      </CardBody>
    </Card>
  );
};

export default Sunat;

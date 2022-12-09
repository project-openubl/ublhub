import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";

import { ResolvedQueries } from "@migtools/lib-ui";
import { NotificationContext } from "@project-openubl/lib-ui";

import { Controller, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { object, string } from "yup";

import {
  ActionGroup,
  Button,
  Flex,
  FlexItem,
  Form,
  FormGroup,
  FormSection,
  TextInput,
} from "@patternfly/react-core";

import {
  SUNAT_BETA_CREDENTIALS,
  SUNAT_BETA_URLS,
  SUNAT_PROD_URLS,
  SunatURls,
} from "Constants";
import { useUpdateCompanyMutation } from "queries/companies";
import { CompanyDto, ProjectDto } from "api/models";
import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";

interface ISunatForm {
  factura: string;
  guia: string;
  retencion: string;
  username: string;
  password: string;
}

interface ISunatFormProps {
  project: ProjectDto;
  company: CompanyDto;
  onSaved: (company: CompanyDto) => void;
  onCancel: () => void;
}

export const SunatForm: React.FC<ISunatFormProps> = ({
  project,
  company,
  onSaved,
  onCancel,
}) => {
  const { t } = useTranslation();

  const { pushNotification } = React.useContext(NotificationContext);

  const updateCompanyMutation = useUpdateCompanyMutation(
    project.id || null,
    (p) => {
      pushNotification({
        title: t("info.data-saved"),
        message: "",
        key: p.name,
        variant: "success",
        actionClose: true,
        timeout: 4000,
      });
      onSaved(p);
    }
  );

  const {
    formState: { errors, isDirty, isValid, isValidating },
    control,
    getValues,
    reset,
  } = useForm<ISunatForm>({
    defaultValues: {
      factura: "",
      guia: "",
      retencion: "",
      username: "",
      password: "",
    },
    resolver: yupResolver(
      object().shape({
        factura: string().trim().required().max(250),
        guia: string().trim().required().max(250),
        retencion: string().trim().required().max(250),
        username: string().trim().required().max(250),
        password: string().trim().required().max(250),
      })
    ),
    mode: "onChange",
  });

  useEffect(() => {
    reset({
      factura: company.sunat?.facturaUrl || "",
      guia: company.sunat?.guiaUrl || "",
      retencion: company.sunat?.retencionUrl || "",
      username: company.sunat?.username || "",
      password: company.sunat ? "******" : "",
    });
  }, [company, reset]);

  const fillForm = (
    urls: SunatURls,
    credentials: { username: string; password: string }
  ) => {
    reset(
      {
        ...urls,
        ...credentials,
      },
      { keepDefaultValues: true }
    );
  };

  const onSaveForm = () => {
    const values = getValues();
    updateCompanyMutation.mutate({
      ...company,
      sunat: {
        facturaUrl: values.factura,
        guiaUrl: values.guia,
        retencionUrl: values.retencion,
        username: values.username,
        password: values.password,
      },
    });
  };

  return (
    <Form>
      <FormSection>
        <Flex>
          <FlexItem>
            <Button
              variant="secondary"
              onClick={() =>
                fillForm(SUNAT_PROD_URLS, {
                  username: "",
                  password: "",
                })
              }
            >
              {t("terms.production")}
            </Button>
          </FlexItem>
          <FlexItem>
            <Button
              variant="secondary"
              onClick={() => fillForm(SUNAT_BETA_URLS, SUNAT_BETA_CREDENTIALS)}
            >
              {t("terms.beta")}
            </Button>
          </FlexItem>
        </Flex>
      </FormSection>
      <FormSection title={t("terms.webServices")}>
        <FormGroup
          label="Factura"
          fieldId="factura"
          isRequired={true}
          validated={getValidatedFromError(errors.factura)}
          helperTextInvalid={errors.factura?.message}
        >
          <Controller
            control={control}
            name="factura"
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
          validated={getValidatedFromError(errors.guia)}
          helperTextInvalid={errors.guia?.message}
        >
          <Controller
            control={control}
            name="guia"
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
          validated={getValidatedFromError(errors.guia)}
          helperTextInvalid={errors.guia?.message}
        >
          <Controller
            control={control}
            name="guia"
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
          onClick={onSaveForm}
          isDisabled={
            !isDirty ||
            !isValid ||
            isValidating ||
            updateCompanyMutation.isLoading
          }
        >
          {t("actions.save")}
        </Button>
        <Button variant="link" onClick={onCancel}>
          {t("actions.cancel")}
        </Button>
      </ActionGroup>

      <ResolvedQueries
        resultsWithErrorTitles={[
          {
            result: updateCompanyMutation,
            errorTitle: "Could not save data",
          },
        ]}
        spinnerMode="inline"
      />
    </Form>
  );
};

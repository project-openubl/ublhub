import React from "react";
import { useTranslation } from "react-i18next";

import { ResolvedQueries } from "@migtools/lib-ui";
import { NotificationContext } from "@project-openubl/lib-ui";

import { Controller, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { object, string } from "yup";

import {
  ActionGroup,
  Button,
  Form,
  FormGroup,
  TextArea,
  TextInput,
} from "@patternfly/react-core";

import { useCompaniesQuery, useUpdateCompanyMutation } from "queries/companies";

import { CompanyDto, ProjectDto } from "api/models";
import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";

interface ICompanyForm {
  ruc: string;
  name: string;
  description: string;
}

interface IGeneralFormProps {
  project: ProjectDto;
  company: CompanyDto;
  onSaved: (company: CompanyDto) => void;
  onCancel: () => void;
}

export const GeneralForm: React.FC<IGeneralFormProps> = ({
  project,
  company,
  onSaved,
  onCancel,
}) => {
  const { t } = useTranslation();

  const { pushNotification } = React.useContext(NotificationContext);

  const companiesQuery = useCompaniesQuery(project.id || null);
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
  } = useForm<ICompanyForm>({
    defaultValues: {
      ruc: company.ruc,
      name: company.name,
      description: company.description,
    },
    resolver: yupResolver(
      object().shape({
        ruc: string()
          .trim()
          .required()
          .min(11)
          .max(11)
          .test("duplicateRuc", (value, options) => {
            return !companiesQuery.data?.find(
              (f) => f.ruc === value && f.id !== company.id
            )
              ? true
              : options.createError({ message: "RUC already registered" });
          }),
        name: string().trim().required().max(250),
        description: string().trim().max(250),
      })
    ),
    mode: "onChange",
  });

  const onSaveForm = () => {
    const values = getValues();
    updateCompanyMutation.mutate({
      ...company,
      ruc: values.ruc,
      name: values.name,
      description: values.description,
    });
  };

  return (
    <Form>
      <FormGroup
        label={t("terms.ruc")}
        fieldId="ruc"
        isRequired={true}
        validated={getValidatedFromError(errors.ruc)}
        helperTextInvalid={errors.ruc?.message}
      >
        <Controller
          control={control}
          name="ruc"
          render={({
            field: { onChange, onBlur, value, name },
            fieldState: { isTouched, error },
          }) => (
            <TextInput
              type="text"
              name={name}
              aria-label="ruc"
              aria-describedby="ruc"
              isRequired={false}
              onChange={onChange}
              onBlur={onBlur}
              value={value}
              validated={!isTouched ? "default" : error ? "error" : "success"}
            />
          )}
        />
      </FormGroup>
      <FormGroup
        label={t("terms.name")}
        fieldId="name"
        isRequired={true}
        validated={getValidatedFromError(errors.name)}
        helperTextInvalid={errors.name?.message}
      >
        <Controller
          control={control}
          name="name"
          render={({
            field: { onChange, onBlur, value, name },
            fieldState: { isTouched, error },
          }) => (
            <TextInput
              type="text"
              name={name}
              aria-label="name"
              aria-describedby="name"
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
        label={t("terms.description")}
        fieldId="description"
        isRequired={false}
        validated={getValidatedFromError(errors.description)}
        helperTextInvalid={errors.description?.message}
      >
        <Controller
          control={control}
          name="description"
          render={({
            field: { onChange, onBlur, value, name },
            fieldState: { isTouched, error },
          }) => (
            <TextArea
              type="text"
              name={name}
              aria-label="description"
              aria-describedby="description"
              isRequired={false}
              onChange={onChange}
              onBlur={onBlur}
              value={value}
              validated={getValidatedFromErrorTouched(error, isTouched)}
            />
          )}
        />
      </FormGroup>
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

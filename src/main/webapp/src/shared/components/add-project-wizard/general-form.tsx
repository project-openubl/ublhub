import React from "react";
import { useTranslation } from "react-i18next";
import { Controller, UseFormReturn } from "react-hook-form";

import { Form, FormGroup, TextArea, TextInput } from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";

import { IGeneralForm } from "./add-project-wizard";

interface IGeneralFormProps {
  form: UseFormReturn<IGeneralForm>;
}

export const GeneralForm: React.FunctionComponent<IGeneralFormProps> = ({
  form,
}: IGeneralFormProps) => {
  const { t } = useTranslation();

  const {
    control,
    formState: { errors },
  } = form;

  return (
    <Form className={spacing.pbXl}>
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
              validated={!isTouched ? "default" : error ? "error" : "success"}
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
    </Form>
  );
};

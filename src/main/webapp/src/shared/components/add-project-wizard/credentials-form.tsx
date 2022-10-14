import React from "react";
import { useTranslation } from "react-i18next";
import { Button, Flex, FlexItem, Form, FormGroup, FormSection, TextInput } from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { Controller, UseFormReturn } from "react-hook-form";
import { object, string } from "yup";

import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";
import { SUNAT_BETA_CREDENTIALS } from "Constants";

export interface ICredentialsForm {
  username: string;
  password: string;
}

export const SchemaFormCredentials = object().shape({
  username: string().trim().required().max(250),
  password: string().trim().required().max(250),
});

interface ICredentialsFormProps {
  form: UseFormReturn<ICredentialsForm>;
}

export const CredentialsForm: React.FunctionComponent<
  ICredentialsFormProps
> = ({ form }: ICredentialsFormProps) => {
  const { t } = useTranslation();

  const {
    control,
    formState: { errors },
    reset
  } = form;

  const fillForm = () => {    
    reset(SUNAT_BETA_CREDENTIALS, { keepDefaultValues: true });
  };

  return (
    <Form className={spacing.pbXl}>
      <FormSection>
        <Flex>
          <FlexItem>
            <Button variant="secondary" onClick={() => fillForm()}>
              {t("terms.beta")}
            </Button>
          </FlexItem>
        </Flex>
      </FormSection>
      <FormSection>
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
    </Form>
  );
};

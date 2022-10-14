import React from "react";
import {
  Button,
  Flex,
  FlexItem,
  Form,
  FormGroup,
  FormSection,
  TextInput,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { Controller, UseFormReturn } from "react-hook-form";
import { object, string } from "yup";

import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";
import { t } from "i18next";

export interface IWebServicesForm {
  factura: string;
  guia: string;
  retencion: string;
}

export const SchemaFormWebServices = object().shape({
  factura: string().trim().required().max(250),
  guia: string().trim().required().max(250),
  retencion: string().trim().required().max(250),
});

export interface SunatURls {
  factura: string;
  guia: string;
  retencion: string;
}

export const SUNAT_PROD_URLS: SunatURls = {
  factura: "https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl",
  guia: "https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl",
  retencion:
    "https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl",
};

export const SUNAT_BETA_URLS: SunatURls = {
  factura: "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService",
  guia: "https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService",
  retencion:
    "https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService",
};

interface IWebServicesFormProps {
  form: UseFormReturn<IWebServicesForm>;
}

export const WebServicesForm: React.FunctionComponent<
  IWebServicesFormProps
> = ({ form }: IWebServicesFormProps) => {
  const {
    control,
    formState: { errors },
    reset,
  } = form;

  const fillForm = (data: SunatURls) => {
    reset({
      factura: data.factura,
      guia: data.guia,
      retencion: data.retencion,
    });
  };

  return (
    <Form className={spacing.pbXl}>
      <FormSection>
        <Flex>
          <FlexItem>
            <Button variant="secondary" onClick={() => fillForm(SUNAT_PROD_URLS)}>
              {t("terms.production")}
            </Button>
          </FlexItem>
          <FlexItem>
            <Button variant="secondary" onClick={() => fillForm(SUNAT_BETA_URLS)}>
              {t("terms.beta")}
            </Button>
          </FlexItem>
        </Flex>
      </FormSection>
      <FormSection>
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
          validated={getValidatedFromError(errors.retencion)}
          helperTextInvalid={errors.retencion?.message}
        >
          <Controller
            control={control}
            name="retencion"
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
    </Form>
  );
};

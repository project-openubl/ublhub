import React, { useState } from "react";
import { useTranslation } from "react-i18next";

import { Controller, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { object, string } from "yup";

import { ResolvedQueries } from "@migtools/lib-ui";

import {
  ActionGroup,
  Button,
  FileUpload,
  Form,
  FormFieldGroupExpandable,
  FormFieldGroupHeader,
  FormGroup,
  FormSection,
  Grid,
  GridItem,
  TextArea,
  TextInput,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { useCompaniesQuery, useCreateCompanyMutation } from "queries/companies";

import { CompanyDto } from "api/models";
import {
  SUNAT_BETA_CREDENTIALS,
  SUNAT_BETA_URLS,
  SUNAT_PROD_URLS,
} from "Constants";
import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";

interface ICompanyForm {
  ruc: string;
  name: string;
  description: string;
  logo: string;
  sunat: {
    factura: string;
    guia: string;
    retencion: string;
    username: string;
    password: string;
  };
}

interface IAddCompanyFormProps {
  projectId: string;
  onSaved: (instance: CompanyDto) => void;
  onCancel: () => void;
}

export const AddCompanyForm: React.FC<IAddCompanyFormProps> = ({
  projectId,
  onSaved,
  onCancel,
}) => {
  const { t } = useTranslation();

  const [logoFile, setLogoFile] = useState<File>();

  const companiesQuery = useCompaniesQuery(projectId);
  const createCompanyMutation = useCreateCompanyMutation(
    projectId,
    (company) => {
      onSaved(company);
    }
  );

  const {
    formState: { errors, isDirty, isValid, isValidating },
    control,
    getValues,
    reset,
  } = useForm<ICompanyForm>({
    defaultValues: {
      ruc: "",
      name: "",
      description: "",
      logo: "",
      sunat: {
        factura: "",
        guia: "",
        retencion: "",
        username: "",
        password: "",
      },
    },
    resolver: yupResolver(
      object().shape({
        ruc: string()
          .trim()
          .required()
          .min(11)
          .max(11)
          .test("duplicateRuc", (value, options) => {
            return !companiesQuery.data?.find((f) => f.ruc === value)
              ? true
              : options.createError({ message: "RUC already registered" });
          }),
        name: string().trim().required().max(250),
        description: string().trim().max(250),
        logo: string(),
        sunat: object()
          .shape({
            factura: string().trim().max(250),
            guia: string().trim().max(250),
            retencion: string().trim().max(250),
            username: string().trim().max(250),
            password: string().trim().max(250),
          })
          .test("allOrNoneRequired", (value, options) => {
            const fieldsLength = [
              value.factura?.length ?? 0,
              value.guia?.length ?? 0,
              value.retencion?.length ?? 0,
              value.username?.length ?? 0,
              value.password?.length ?? 0,
            ];
            const totalLength = fieldsLength.reduce(
              (partialSum, a) => partialSum + a,
              0
            );

            return totalLength !== 0 && fieldsLength.some((f) => f === 0)
              ? options.createError({
                  message: "All or none fields must be filled",
                  path: "sunat",
                })
              : true;
          }),
      })
    ),
    mode: "onChange",
  });

  const onSaveForm = () => {
    const values = getValues();
    createCompanyMutation.mutate({
      ruc: values.ruc,
      name: values.name,
      description: values.description,
      logo: values.logo,
      sunat: values.sunat.factura
        ? {
            facturaUrl: values.sunat.factura,
            guiaUrl: values.sunat.guia,
            retencionUrl: values.sunat.retencion,
            username: values.sunat.username,
            password: values.sunat.password,
          }
        : undefined,
    });
  };

  return (
    <Form className={spacing.pbXl}>
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
      <FormGroup
        label={t("terms.logo")}
        fieldId="logo"
        isRequired={false}
        validated={getValidatedFromError(errors.logo)}
        helperTextInvalid={errors.logo?.message}
      >
        <Grid hasGutter>
          <GridItem md={8}>
            <Controller
              control={control}
              name="logo"
              render={({ field: { onChange } }) => (
                <FileUpload
                  id="company-logo"
                  type="dataURL"
                  value={logoFile}
                  filename={logoFile?.name}
                  filenamePlaceholder="Drag and drop a file or upload one"
                  onFileInputChange={(_, file) => {
                    setLogoFile(file);
                  }}
                  onClearClick={() => {
                    onChange(undefined);
                    setLogoFile(undefined);
                  }}
                  onDataChange={(base64Image) => {
                    onChange(base64Image);
                  }}
                  allowEditingUploadedText={false}
                  browseButtonText="Upload"
                  hideDefaultPreview
                ></FileUpload>
              )}
            />
          </GridItem>
          <GridItem md={4}>
            {logoFile && (
              <div>
                <img
                  src={URL.createObjectURL(logoFile)}
                  alt="Logo"
                  style={{ maxHeight: 36 }}
                />
              </div>
            )}
          </GridItem>
        </Grid>
      </FormGroup>
      <FormFieldGroupExpandable
        toggleAriaLabel="sunat"
        header={
          <FormFieldGroupHeader
            titleText={{
              id: "sunat-group",
              text: "SUNAT",
            }}
            actions={
              <>
                <Button
                  variant="secondary"
                  onClick={() => {
                    reset(
                      {
                        ...getValues(),
                        sunat: {
                          ...SUNAT_BETA_CREDENTIALS,
                          ...SUNAT_PROD_URLS,
                          password: "",
                        },
                      },
                      { keepDirty: true }
                    );
                  }}
                >
                  {t("terms.production")}
                </Button>{" "}
                <Button
                  variant="secondary"
                  onClick={() => {
                    reset(
                      {
                        ...getValues(),
                        sunat: {
                          ...SUNAT_BETA_CREDENTIALS,
                          ...SUNAT_BETA_URLS,
                        },
                      },
                      { keepDirty: true }
                    );
                  }}
                >
                  {t("terms.beta")}
                </Button>
              </>
            }
          />
        }
      >
        <FormSection title={t("terms.webServices")}>
          <FormGroup
            label="Factura"
            fieldId="factura"
            isRequired={true}
            validated={getValidatedFromError(errors.sunat)}
            helperTextInvalid={
              "errors.sunat?.factura?.message || errors.sunat?.message"
            }
          >
            <Controller
              control={control}
              name="sunat.factura"
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
            validated={getValidatedFromError(errors.sunat?.guia)}
            helperTextInvalid={errors.sunat?.guia?.message}
          >
            <Controller
              control={control}
              name="sunat.guia"
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
            validated={getValidatedFromError(errors.sunat?.retencion)}
            helperTextInvalid={errors.sunat?.retencion?.message}
          >
            <Controller
              control={control}
              name="sunat.retencion"
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
            validated={getValidatedFromError(errors.sunat?.username)}
            helperTextInvalid={errors.sunat?.username?.message}
          >
            <Controller
              control={control}
              name="sunat.username"
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
            validated={getValidatedFromError(errors.sunat?.password)}
            helperTextInvalid={errors.sunat?.password?.message}
          >
            <Controller
              control={control}
              name="sunat.password"
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
      </FormFieldGroupExpandable>

      <ActionGroup>
        <Button
          variant="primary"
          onClick={onSaveForm}
          isDisabled={
            !isDirty ||
            !isValid ||
            isValidating ||
            createCompanyMutation.isLoading
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
            result: createCompanyMutation,
            errorTitle: "Could not save data",
          },
        ]}
        spinnerMode="inline"
      />
    </Form>
  );
};

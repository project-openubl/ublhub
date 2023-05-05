import React, { useState } from "react";
import { useTranslation } from "react-i18next";

import { ResolvedQueries } from "@migtools/lib-ui";
import { NotificationContext } from "@project-openubl/lib-ui";

import { Controller, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { object, string } from "yup";

import {
  ActionGroup,
  Button,
  FileUpload,
  Form,
  FormGroup,
  Grid,
  GridItem,
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
  logo: string;
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
  const [logoFile, setLogoFile] = useState<File>();

  const { pushNotification } = React.useContext(NotificationContext);

  const companiesQuery = useCompaniesQuery(project.name || null);
  const updateCompanyMutation = useUpdateCompanyMutation(
    project.name || null,
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
      logo: "",
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
      logo: values.logo,
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
              isDisabled
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

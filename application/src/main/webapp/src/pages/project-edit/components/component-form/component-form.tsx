import React, { useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";

import { Controller, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { BaseSchema, boolean, object, string } from "yup";

import { ResolvedQueries } from "@migtools/lib-ui";

import {
  ActionGroup,
  Button,
  FileUpload,
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Popover,
  Switch,
  TextInput,
} from "@patternfly/react-core";
import { HelpIcon } from "@patternfly/react-icons";

import { ComponentDto, ComponentTypeDto } from "api/models";
import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";
import {
  useCreateComponentMutation,
  useUpdateComponentMutation,
} from "queries/keys";

interface IComponentForm {
  [key: string]: string | boolean;
}

interface IComponentFormProps {
  projectName: string;
  companyRuc?: string;
  componentType: ComponentTypeDto;
  component?: ComponentDto;
  onSaved: (instance: ComponentDto) => void;
  onCancel: () => void;
}

export const ComponentForm: React.FC<IComponentFormProps> = ({
  projectName,
  companyRuc,
  componentType,
  component,
  onSaved,
  onCancel,
}) => {
  const { t } = useTranslation();

  const createComponentMutation = useCreateComponentMutation(
    projectName,
    companyRuc || null,
    (component) => {
      onSaved(component);
    }
  );
  const updateComponentMutation = useUpdateComponentMutation(
    projectName,
    companyRuc || null,
    (component) => {
      onSaved(component);
    }
  );

  const formData = useMemo(() => {
    return componentType.properties.reduce(
      (prev, current) => {
        let fieldSchema: BaseSchema;
        let fieldDefaultValue: string | boolean | File;
        switch (current.type) {
          case "boolean":
            fieldSchema = boolean().required();
            fieldDefaultValue = current.defaultValue === "true";
            break;
          case "File":
            fieldSchema = string().trim().required().max(4000);
            fieldDefaultValue = current.defaultValue;
            break;
          default:
            fieldSchema = string().trim().required().max(255);
            fieldDefaultValue = current.defaultValue;
            break;
        }

        return {
          schema: {
            ...prev.schema,
            [current.name]: fieldSchema,
          },
          defaultValues: {
            ...prev.defaultValues,
            [current.name]: fieldDefaultValue,
          },
        };
      },
      { schema: {}, defaultValues: {} }
    );
  }, [componentType]);

  const {
    formState: { errors, isDirty, isValid, isValidating },
    control,
    getValues,
    reset,
  } = useForm<IComponentForm>({
    defaultValues: {
      ...formData.defaultValues,
      name: componentType.id,
    },
    resolver: yupResolver(
      object().shape({
        ...formData.schema,
        name: string().trim().required().min(1).max(255),
      })
    ),
    mode: "onChange",
  });

  useEffect(() => {
    if (component && componentType) {
      const formValues = componentType.properties.reduce((prev, current) => {
        const values = component.config[current.name];
        if (values) {
          const val =
            values[0] !== "true" && values[0] !== "false"
              ? values[0]
              : values[0] === "true";
          return {
            ...prev,
            [current.name]: val,
          };
        } else {
          return { ...prev };
        }
      }, {});

      reset({
        ...formValues,
        name: component.name,
      });
    }
  }, [component, componentType, reset]);

  const onSaveForm = () => {
    const { name, ...rest } = getValues();

    const payload: ComponentDto = {
      ...component,
      name: name as string,
      providerId: componentType.id,
      providerType: "io.github.project.openubl.ublhub.keys.KeyProvider", // Not needed at all
      config: Object.entries(rest).reduce((prev, [key, val]) => {
        return {
          ...prev,
          [key]: [`${val}`],
        };
      }, {}),
    };

    if (!component) {
      createComponentMutation.mutate(payload);
    } else {
      updateComponentMutation.mutate(payload);
    }
  };

  return (
    <Form isHorizontal>
      {component && (
        <FormGroup
          label="Id"
          fieldId="id"
          isRequired={true}
          validated="default"
        >
          <TextInput
            type="text"
            name="id"
            aria-label="id"
            aria-describedby="id"
            isRequired={true}
            value={component.id}
            readOnlyVariant="default"
          />
        </FormGroup>
      )}
      <FormGroup
        label="Name"
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
              value={value as string}
              validated={getValidatedFromErrorTouched(error, isTouched)}
            />
          )}
        />
      </FormGroup>
      {Object.entries(componentType.properties).map(
        ([fieldKey, fieldValue]) => (
          <FormGroup
            key={fieldKey}
            label={fieldValue.label}
            fieldId={fieldValue.name}
            isRequired={true}
            validated={getValidatedFromError(errors[fieldValue.name])}
            helperTextInvalid={errors[fieldValue.name]?.message}
            labelIcon={
              <Popover bodyContent={<div>{fieldValue.helpText}</div>}>
                <button
                  type="button"
                  aria-label="More info for name field"
                  onClick={(e) => e.preventDefault()}
                  className="pf-c-form__group-label-help"
                >
                  <HelpIcon noVerticalAlign />
                </button>
              </Popover>
            }
          >
            {fieldValue.type === "String" && (
              <Controller
                control={control}
                name={fieldValue.name}
                render={({
                  field: { onChange, onBlur, value, name },
                  fieldState: { isTouched, error },
                }) => (
                  <TextInput
                    type={fieldValue.secret ? "password" : "text"}
                    name={name}
                    aria-label={fieldValue.name}
                    aria-describedby={fieldValue.name}
                    isRequired={false}
                    onChange={onChange}
                    onBlur={onBlur}
                    value={value as string}
                    validated={getValidatedFromErrorTouched(error, isTouched)}
                  />
                )}
              />
            )}
            {fieldValue.type === "boolean" && (
              <Controller
                control={control}
                name={fieldValue.name}
                render={({ field: { onChange, value, name } }) => (
                  <Switch
                    name={name}
                    aria-label={fieldValue.name}
                    aria-describedby={fieldValue.name}
                    isChecked={value as boolean}
                    onChange={onChange}
                    label="On"
                    labelOff="Off"
                  />
                )}
              />
            )}
            {fieldValue.type === "List" && (
              <Controller
                control={control}
                name={fieldValue.name}
                render={({ field: { onChange, value, name } }) => (
                  <FormSelect
                    name={name}
                    value={value}
                    onChange={onChange}
                    aria-label={name}
                  >
                    {fieldValue.options.map((option, index) => (
                      <FormSelectOption
                        key={index}
                        value={option}
                        label={option}
                      />
                    ))}
                  </FormSelect>
                )}
              />
            )}
            {fieldValue.type === "File" && (
              <Controller
                control={control}
                name={fieldValue.name}
                render={({ field: { onChange, value } }) => (
                  <FileUpload
                    id={fieldKey}
                    type="text"
                    value={value as string}
                    filename={value ? fieldValue.label : undefined}
                    filenamePlaceholder="Drag a file here or browser to upload"
                    onDataChange={onChange}
                    onTextChange={onChange}
                    onClearClick={() => onChange("")}
                    allowEditingUploadedText={false}
                    browseButtonText="Upload"
                    // hideDefaultPreview
                  />
                )}
              />
            )}
          </FormGroup>
        )
      )}

      <ActionGroup>
        <Button
          variant="primary"
          onClick={onSaveForm}
          isDisabled={
            !isDirty ||
            !isValid ||
            isValidating ||
            createComponentMutation.isLoading ||
            updateComponentMutation.isLoading
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
            result: !component
              ? createComponentMutation
              : updateComponentMutation,
            errorTitle: "Could not save data",
          },
        ]}
        spinnerMode="inline"
      />
    </Form>
  );
};

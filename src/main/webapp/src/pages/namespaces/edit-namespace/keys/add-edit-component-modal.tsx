import React, { useEffect } from "react";
import * as yup from "yup";
import { useTranslation } from "react-i18next";
import { UseQueryResult } from "react-query";
import {
  Button,
  FileUpload,
  Flex,
  Form,
  FormGroup,
  Modal,
  ModalVariant,
  NumberInput,
  Popover,
  Stack,
  Switch,
} from "@patternfly/react-core";
import { HelpIcon } from "@patternfly/react-icons";

import {
  getFormGroupProps,
  ResolvedQueries,
  ResolvedQuery,
  useFormField,
  useFormState,
  ValidatedTextInput,
} from "@konveyor/lib-ui";
import {
  OptionWithValue,
  SimplePlaceholder,
  SimpleSelect,
} from "@project-openubl/lib-ui";

import {
  getComponentNameSchema,
  useComponentsQuery,
  useCreateComponentMutation,
  useUpdateComponentMutation,
} from "queries/components";

import { ComponentRepresentation } from "api/models";
import { usePausedPollingEffect } from "shared/context";

const PROVIDER_IDS = ["rsa", "rsa-generated"] as const;
type ProviderId = typeof PROVIDER_IDS[number];
const PROVIDER_ID_NAMES: Record<ProviderId, string> = {
  rsa: "RSA",
  "rsa-generated": "RSA generated",
};
const PROVIDER_ID_OPTIONS = PROVIDER_IDS.map((type) => ({
  toString: () => PROVIDER_ID_NAMES[type],
  value: type,
})) as OptionWithValue<ProviderId>[];

const ALGORITHM_TYPES = [
  "RS256",
  "RS384",
  "RS512",
  "PS256",
  "PS384",
  "PS512",
] as const;
type AlgorithmType = typeof ALGORITHM_TYPES[number];

const KEY_SIZE_TYPES = ["1024", "2048", "4096"] as const;
type KeySizeType = typeof KEY_SIZE_TYPES[number];

const useComponentFormState = (
  componentsQuery: UseQueryResult<ComponentRepresentation[]>,
  componentBeingPrefilled?: ComponentRepresentation
) => {
  const commonProviderFields = {
    providerId: useFormField<ProviderId | null>(
      null,
      yup
        .mixed()
        .label("Provider id")
        .oneOf([...PROVIDER_IDS]) // Spread necessary because readonly array isn't assignable to mutable any[]
        .required()
    ),
    name: useFormField<string>(
      "",
      getComponentNameSchema(componentsQuery, componentBeingPrefilled)
    ),
    priority: useFormField<number>(
      0,
      yup.number().label("Priority").min(0).required()
    ),
    enabled: useFormField<boolean>(true, yup.boolean().required()),
    algorithm: useFormField<AlgorithmType>(
      "RS256",
      yup
        .mixed()
        .label("Algorithm")
        .oneOf([...ALGORITHM_TYPES])
        .required()
    ),
  };

  return {
    rsa: useFormState({
      ...commonProviderFields,
      privateKey: useFormField<string>("", yup.string().required()),
      certificate: useFormField<string>("", yup.string().required()),
    }),
    "rsa-generated": useFormState({
      ...commonProviderFields,
      keySize: useFormField<KeySizeType | null>(
        "2048",
        yup
          .mixed()
          .oneOf([...KEY_SIZE_TYPES])
          .required()
      ),
    }),
  };
};

export type KeyFormState = ReturnType<typeof useComponentFormState>;

const useComponentFormPrefillEffect = (
  forms: KeyFormState,
  componentBeingPrefilled?: ComponentRepresentation
) => {
  const [isStartedPrefilling, setIsStartedPrefilling] = React.useState(false);
  const [isDonePrefilling, setIsDonePrefilling] = React.useState(
    !componentBeingPrefilled
  );

  useEffect(() => {
    if (!isStartedPrefilling && componentBeingPrefilled) {
      setIsStartedPrefilling(true);

      const providerId = componentBeingPrefilled.providerId as ProviderId;
      const { fields } = forms[providerId];
      fields.providerId.prefill(providerId);
      fields.name.prefill(componentBeingPrefilled.name);

      if (componentBeingPrefilled.config.priority) {
        fields.priority.prefill(
          Number(componentBeingPrefilled.config.priority[0])
        );
      }
      if (componentBeingPrefilled.config.enabled) {
        fields.enabled.prefill(
          componentBeingPrefilled.config.enabled[0] === "true"
        );
      }
      if (componentBeingPrefilled.config.algorithm) {
        fields.algorithm.prefill(
          componentBeingPrefilled.config.algorithm[0] as AlgorithmType
        );
      }

      if (providerId === "rsa") {
        const rsaFieldsType = fields as typeof forms.rsa.fields;

        if (componentBeingPrefilled.config.privateKey) {
          rsaFieldsType.privateKey.prefill(
            componentBeingPrefilled.config.privateKey[0]
          );
        }
        if (componentBeingPrefilled.config.certificate) {
          rsaFieldsType.certificate.prefill(
            componentBeingPrefilled.config.certificate[0]
          );
        }
      }
      if (providerId === "rsa-generated") {
        const rsaGeneratedFields = forms["rsa-generated"].fields;
        const rsaGeneratedFieldsType = fields as typeof rsaGeneratedFields;

        if (componentBeingPrefilled.config.keySize) {
          rsaGeneratedFieldsType.keySize.prefill(
            componentBeingPrefilled.config.keySize[0] as KeySizeType
          );
        }
      }

      // Wait for effects to run based on field changes first
      window.setTimeout(() => {
        setIsDonePrefilling(true);
      }, 0);
    }
  }, [isStartedPrefilling, forms, componentBeingPrefilled]);

  return {
    isDonePrefilling,
  };
};

interface IAddEditComponentModalProps {
  namespaceId: string;
  componentBeingEdited?: ComponentRepresentation;
  onClose: () => void;
}

export const AddEditComponentModal: React.FC<IAddEditComponentModalProps> = ({
  namespaceId,
  componentBeingEdited,
  onClose,
}) => {
  usePausedPollingEffect();

  const { t } = useTranslation();

  const componentsQuery = useComponentsQuery(namespaceId);

  const forms = useComponentFormState(componentsQuery, componentBeingEdited);
  const { isDonePrefilling } = useComponentFormPrefillEffect(
    forms,
    componentBeingEdited
  );

  const providerIdField = forms.rsa.fields.providerId;
  const providerId = providerIdField.value;
  const formValues = providerId ? forms[providerId].values : null;
  const isFormValid = providerId ? forms[providerId].isValid : false;
  const isFormDirty = providerId ? forms[providerId].isDirty : false;

  // Combines fields of all 3 forms into one type with all properties as optional.
  // This way, we can conditionally show fields based on whether they are defined in form state
  // instead of duplicating the logic of which providers have which fields.
  const rsaFields = forms["rsa"].fields;
  const rsaGeneratedFields = forms["rsa-generated"].fields;
  const fields = providerId
    ? (forms[providerId].fields as Partial<
        typeof rsaFields & typeof rsaGeneratedFields
      >)
    : null;

  const createComponentMutation = useCreateComponentMutation(
    namespaceId,
    onClose
  );

  const updateComponentMutation = useUpdateComponentMutation(
    namespaceId,
    onClose
  );

  const mutateComponent = !componentBeingEdited
    ? createComponentMutation.mutate
    : updateComponentMutation.mutate;
  const mutateComponentResult = !componentBeingEdited
    ? createComponentMutation
    : updateComponentMutation;

  const onSave = () => {
    if (!formValues) {
      return;
    }

    const payload: ComponentRepresentation = {
      id: componentBeingEdited?.id,
      name: formValues.name,
      parentId: namespaceId,
      providerId: `${formValues.providerId}`,
      providerType: "io.github.project.openubl.ublhub.keys.KeyProvider",
      subType: componentBeingEdited?.subType,
      config: {
        active: ["true"],
        enabled: [formValues.enabled.toString()],
        algorithm: [formValues.algorithm],
        priority: [formValues.priority.toString()],
      },
    };

    if (providerId === "rsa") {
      payload.config.privateKey = [(formValues as any)["privateKey"]];
      payload.config.certificate = [(formValues as any)["certificate"]];
    }
    if (providerId === "rsa-generated") {
      payload.config.keySize = [(formValues as any)["keySize"]];
    }

    mutateComponent(payload);
  };
  return (
    <Modal
      isOpen
      variant={ModalVariant.small}
      title={
        !componentBeingEdited
          ? t("actions.create-object", { what: "key" })
          : t("actions.edit-object", { what: "key" })
      }
      onClose={() => onClose()}
      footer={
        <Stack hasGutter>
          <ResolvedQuery
            result={mutateComponentResult}
            errorTitle={`Cannot ${
              !componentBeingEdited ? "add" : "edit"
            } provider`}
            spinnerMode="inline"
          />
          <Flex spaceItems={{ default: "spaceItemsSm" }}>
            <Button
              id="modal-confirm-button"
              key="confirm"
              variant="primary"
              isDisabled={
                !isFormDirty || !isFormValid || mutateComponentResult.isLoading
              }
              onClick={onSave}
            >
              {!componentBeingEdited ? t("actions.create") : t("actions.save")}
            </Button>
            <Button
              id="modal-cancel-button"
              key="cancel"
              variant="link"
              onClick={() => onClose()}
              isDisabled={mutateComponentResult.isLoading}
            >
              {t("actions.cancel")}
            </Button>
          </Flex>
        </Stack>
      }
    >
      <ResolvedQueries
        resultsWithErrorTitles={[
          { result: componentsQuery, errorTitle: "Cannot load components" },
        ]}
      >
        {!isDonePrefilling ? (
          <SimplePlaceholder />
        ) : (
          <Form>
            <FormGroup
              label="Type"
              isRequired
              fieldId="provider-id"
              {...getFormGroupProps(providerIdField)}
            >
              <SimpleSelect
                id="provider-id"
                aria-label="Provider id"
                options={PROVIDER_ID_OPTIONS}
                value={[
                  PROVIDER_ID_OPTIONS.find(
                    (option) => option.value === providerId
                  ),
                ]}
                onChange={(selection) => {
                  providerIdField.setValue(
                    (selection as OptionWithValue<ProviderId>).value
                  );
                  providerIdField.setIsTouched(true);
                }}
                placeholderText="Select a provider type..."
                isDisabled={!!componentBeingEdited}
                menuAppendTo="parent"
                maxHeight="40vh"
              />
            </FormGroup>
            {providerId ? (
              <>
                {fields?.name ? (
                  <ValidatedTextInput
                    field={forms[providerId].fields.name}
                    label={t("terms.name")}
                    isRequired
                    fieldId="name"
                  />
                ) : null}
                {fields?.priority ? (
                  <FormGroup
                    label={t("terms.priority")}
                    isRequired
                    fieldId="priority"
                    {...getFormGroupProps(fields.priority)}
                  >
                    <NumberInput
                      value={fields.priority.value}
                      onMinus={() =>
                        fields.priority?.setValue(fields.priority.value - 1)
                      }
                      onChange={(e) =>
                        fields.priority?.setValue(
                          Number((e.target as any).value)
                        )
                      }
                      onPlus={() =>
                        fields.priority?.setValue(fields.priority.value + 1)
                      }
                      inputName="priority"
                      inputAriaLabel="priority"
                      minusBtnAriaLabel="minus"
                      plusBtnAriaLabel="plus"
                    />
                  </FormGroup>
                ) : null}
                {fields?.enabled ? (
                  <FormGroup
                    isRequired
                    fieldId="enabled"
                    {...getFormGroupProps(fields.enabled)}
                  >
                    <Switch
                      id="enabled"
                      label="Enabled"
                      labelOff="Disabled"
                      isChecked={fields.enabled.value}
                      onChange={() =>
                        fields.enabled?.setValue(!fields.enabled.value)
                      }
                    />
                  </FormGroup>
                ) : null}
                {fields?.keySize ? (
                  <FormGroup
                    label="Key size"
                    isRequired
                    fieldId="keySize"
                    {...getFormGroupProps(fields.keySize)}
                  >
                    <SimpleSelect
                      id="keySize"
                      aria-label="Key size"
                      options={[...KEY_SIZE_TYPES]}
                      value={fields.keySize.value || undefined}
                      onChange={(selection) => {
                        fields.keySize?.setValue(selection as KeySizeType);
                        fields.keySize?.setIsTouched(true);
                      }}
                      placeholderText="Select a key size..."
                      menuAppendTo="parent"
                      maxHeight="40vh"
                    />
                  </FormGroup>
                ) : null}
                {fields?.algorithm ? (
                  <FormGroup
                    label={t("terms.algorithm")}
                    isRequired
                    fieldId="algorithm"
                    {...getFormGroupProps(fields.algorithm)}
                  >
                    <SimpleSelect
                      id="algorithm"
                      aria-label="Algorithm"
                      options={[...ALGORITHM_TYPES]}
                      value={fields.algorithm.value || undefined}
                      onChange={(selection) => {
                        fields.algorithm?.setValue(selection as AlgorithmType);
                        fields.algorithm?.setIsTouched(true);
                      }}
                      placeholderText="Select an algorithm..."
                      menuAppendTo="parent"
                      maxHeight="20vh"
                    />
                  </FormGroup>
                ) : null}
                {fields?.privateKey ? (
                  <FormGroup
                    label="Private RSA key"
                    labelIcon={
                      <Popover
                        bodyContent={
                          <div>
                            The private RSA key is the private key of your
                            certificate, it usually has <code>.key</code>{" "}
                            extension.
                          </div>
                        }
                      >
                        <Button
                          variant="plain"
                          aria-label="More info for Private RSA key field"
                          onClick={(e) => e.preventDefault()}
                          aria-describedby="privateKey"
                          className="pf-c-form__group-label-help"
                        >
                          <HelpIcon noVerticalAlign />
                        </Button>
                      </Popover>
                    }
                    fieldId="privateKey"
                    {...getFormGroupProps(fields.privateKey)}
                  >
                    <FileUpload
                      id="privateKey"
                      type="text"
                      value={fields.privateKey.value}
                      filename={fields.privateKey.value}
                      onChange={(value, filename) => {
                        fields.privateKey?.setValue(value as string);
                        fields.privateKey?.setIsTouched(true);
                      }}
                      onBlur={() => fields.privateKey?.setIsTouched(true)}
                      validated={
                        fields.privateKey?.isValid ? "default" : "error"
                      }
                    />
                  </FormGroup>
                ) : null}
                {fields?.certificate ? (
                  <FormGroup
                    label="X509 certificate"
                    labelIcon={
                      <Popover
                        bodyContent={
                          <div>
                            X509 certificate is the certificate you will use to
                            sign, it usually has <code>.key</code> extension.
                          </div>
                        }
                      >
                        <Button
                          variant="plain"
                          aria-label="More info for CA certificate field"
                          onClick={(e) => e.preventDefault()}
                          aria-describedby="caCert"
                          className="pf-c-form__group-label-help"
                        >
                          <HelpIcon noVerticalAlign />
                        </Button>
                      </Popover>
                    }
                    fieldId="certificate"
                    {...getFormGroupProps(fields.certificate)}
                  >
                    <FileUpload
                      id="certificate"
                      type="text"
                      value={fields.certificate.value}
                      filename={fields.certificate.value}
                      onChange={(value, filename) => {
                        fields.certificate?.setValue(value as string);
                        fields.certificate?.setIsTouched(true);
                      }}
                      onBlur={() => fields.certificate?.setIsTouched(true)}
                      validated={
                        fields.certificate?.isValid ? "default" : "error"
                      }
                    />
                  </FormGroup>
                ) : null}
              </>
            ) : null}
          </Form>
        )}
      </ResolvedQueries>
    </Modal>
  );
};

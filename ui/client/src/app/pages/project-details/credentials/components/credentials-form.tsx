import React, { useContext } from "react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { AxiosError } from "axios";
import { object, string, array } from "yup";

import {
  ActionGroup,
  Button,
  ButtonVariant,
  Chip,
  ChipGroup,
  Form,
  FormFieldGroupExpandable,
  FormFieldGroupHeader,
  TextInputGroup,
  TextInputGroupMain,
  TextInputGroupUtilities,
} from "@patternfly/react-core";
import TimesIcon from "@patternfly/react-icons/dist/esm/icons/times-icon";

import { Credentials, New, Project } from "@app/api/models";
import {
  useCreateProjectMutation,
  useFetchProjects,
  useUpdateProjectMutation,
} from "@app/queries/projects";
import { duplicateFieldCheck } from "@app/utils/utils";

import {
  HookFormPFGroupController,
  HookFormPFTextArea,
  HookFormPFTextInput,
} from "@app/components/HookFormPFFields";
import { NotificationsContext } from "@app/components/NotificationsContext";
import {
  useCreateCredentialsMutation,
  useFetchCredentials,
  useUpdateCredentialsMutation,
} from "@app/queries/credentials";
import { KeyDisplayToggle } from "@app/components/KeyDisplayToggle";

export interface FormValues {
  name: string;
  description?: string;
  usernameSol: string;
  passwordSol: string;
  clientId: string;
  clientSecret: string;
  urlInvoice: string;
  urlDespatch: string;
  urlPerceptionRetention: string;
  supplierIdsAppliedTo: string[];
}

export interface ICredentialsFormProps {
  projectId: string | number;
  credentials?: Credentials;
  onClose: () => void;
}

export const CredentialsForm: React.FC<ICredentialsFormProps> = ({
  projectId,
  credentials,
  onClose,
}) => {
  const { pushNotification } = useContext(NotificationsContext);

  const [supplierId, setSupplierId] = React.useState("");
  const supplierIdRef = React.useRef<HTMLInputElement>();

  const [isPasswordHidden, setIsPasswordHidden] = React.useState(true);
  const toggleHidePassword = (e: React.FormEvent<HTMLElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsPasswordHidden(!isPasswordHidden);
  };

  const { credentials: credentialsList } = useFetchCredentials(projectId);

  const validationSchema = object().shape(
    {
      supplier_ids_applied_to: array().of(string().trim().required()).min(1),
      name: string()
        .trim()
        .required()
        .min(3)
        .max(120)
        .matches(/[a-z0-9]([-a-z0-9]*[a-z0-9])?/)
        .test(
          "Duplicate name",
          "A credentials with this name already exists. Use a different name.",
          (value) =>
            duplicateFieldCheck(
              "name",
              credentialsList,
              credentials || null,
              value || ""
            )
        ),
      description: string().defined().trim().max(250),

      usernameSol: string()
        .ensure()
        .when(["passwordSol", "urlInvoice", "urlPerceptionRetention"], {
          is: (a: string, b: string, c: string) => a || b || c,
          then: (schema) => schema.required().min(1).max(250),
        }),
      passwordSol: string()
        .ensure()
        .when(["usernameSol", "urlInvoice", "urlPerceptionRetention"], {
          is: (a: string, b: string, c: string) => a || b || c,
          then: (schema) => schema.required().min(1).max(250),
        }),
      urlInvoice: string()
        .ensure()
        .when(["usernameSol", "passwordSol", "urlPerceptionRetention"], {
          is: (a: string, b: string, c: string) => a || b || c,
          then: (schema) => schema.required().min(3).max(250).url(),
        }),
      urlPerceptionRetention: string()
        .ensure()
        .when(["usernameSol", "passwordSol", "urlInvoice"], {
          is: (a: string, b: string, c: string) => a || b || c,
          then: (schema) => schema.required().min(3).max(250).url(),
        }),

      clientId: string()
        .ensure()
        .when(["clientSecret", "urlDespatch"], {
          is: (a: string, b: string) => a || b,
          then: (schema) => schema.required().min(1).max(250),
        }),
      clientSecret: string()
        .ensure()
        .when(["clientId", "urlDespatch"], {
          is: (a: string, b: string) => a || b,
          then: (schema) => schema.required().min(1).max(250),
        }),
      urlDespatch: string()
        .ensure()
        .when(["clientId", "clientSecret"], {
          is: (a: string, b: string) => a || b,
          then: (schema) => schema.required().min(3).max(250).url(),
        }),
    },
    [
      ["usernameSol", "passwordSol"],
      ["passwordSol", "urlInvoice"],
      ["urlInvoice", "urlPerceptionRetention"],

      ["usernameSol", "urlInvoice"],
      ["usernameSol", "urlPerceptionRetention"],
      ["passwordSol", "urlPerceptionRetention"],

      ["clientSecret", "urlDespatch"],
      ["clientId", "urlDespatch"],
      ["clientId", "clientSecret"],
    ]
  );

  const {
    handleSubmit,
    formState: { isSubmitting, isValidating, isValid, isDirty },
    getValues,
    control,
    resetField,
    setValue,
    trigger,
  } = useForm<FormValues>({
    defaultValues: {
      name: credentials?.name || "",
      description: credentials?.description || "",

      supplierIdsAppliedTo: credentials?.supplier_ids_applied_to || [],

      usernameSol: credentials?.soap?.username_sol || "",
      passwordSol: credentials?.soap?.password_sol || "",
      urlInvoice: credentials?.soap?.url_invoice || "",
      urlPerceptionRetention: credentials?.soap?.url_perception_retention || "",

      clientId: credentials?.rest?.client_id || "",
      clientSecret: credentials?.rest?.client_secret || "",
      urlDespatch: credentials?.rest?.url_despatch || "",
    },
    resolver: yupResolver(validationSchema),
    mode: "onChange",
  });

  const onCreateCredentialsSuccess = (_: Credentials) =>
    pushNotification({
      title: "Credenciales creadas",
      variant: "success",
    });

  const onCreateCredentialsError = (error: AxiosError) => {
    pushNotification({
      title: "Error al crear las credenciales",
      variant: "danger",
    });
  };

  const { mutate: createCredentials } = useCreateCredentialsMutation(
    projectId,
    onCreateCredentialsSuccess,
    onCreateCredentialsError
  );

  const onUpdateCredentialsSuccess = (_: Credentials) =>
    pushNotification({
      title: "Credenciales actualizadas",
      variant: "success",
    });

  const onUpdateCredentialsError = (error: AxiosError) => {
    pushNotification({
      title: "Error al actualizar las credenciales",
      variant: "danger",
    });
  };
  const { mutate: updateCredentials } = useUpdateCredentialsMutation(
    projectId,
    onUpdateCredentialsSuccess,
    onUpdateCredentialsError
  );

  const onSubmit = (formValues: FormValues) => {
    const payload: New<Credentials> = {
      name: formValues.name.trim(),
      description: formValues.description?.trim(),

      supplier_ids_applied_to: formValues.supplierIdsAppliedTo,

      soap:
        formValues.usernameSol ||
        formValues.passwordSol ||
        formValues.urlInvoice ||
        formValues.urlPerceptionRetention
          ? {
              username_sol: formValues.usernameSol?.trim(),
              password_sol: formValues.passwordSol?.trim(),
              url_invoice: formValues.urlInvoice?.trim(),
              url_perception_retention:
                formValues.urlPerceptionRetention?.trim(),
            }
          : undefined,
      rest:
        formValues.clientId || formValues.clientSecret || formValues.urlDespatch
          ? {
              client_id: formValues.clientId?.trim(),
              client_secret: formValues.clientSecret?.trim(),
              url_despatch: formValues.urlDespatch?.trim(),
            }
          : undefined,
    };
    if (credentials) {
      updateCredentials({ id: credentials.id, ...payload });
    } else {
      createCredentials(payload);
    }
    onClose();
  };

  const values = getValues();
  const isPasswordEncrypted =
    credentials?.soap?.password_sol === values.passwordSol;

  return (
    <Form onSubmit={handleSubmit(onSubmit)}>
      <HookFormPFGroupController
        control={control}
        name="supplierIdsAppliedTo"
        label="RUC"
        isRequired
        fieldId="supplierIdsAppliedTo"
        renderInput={({ field: { value, name, onChange } }) => {
          return (
            <TextInputGroup>
              <TextInputGroupMain
                id="supplier-id"
                autoComplete="off"
                name={name}
                aria-controls="select-supplier-id"
                innerRef={supplierIdRef}
                value={supplierId}
                onChange={(_, val) => setSupplierId(val)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    e.stopPropagation();

                    onChange([...value, supplierId]);
                    setSupplierId("");
                  }
                }}
                onBlur={() => {
                  if (supplierId && supplierId.trim().length > 0) {
                    onChange([...value, supplierId]);
                    setSupplierId("");
                  }
                }}
              >
                <ChipGroup aria-label="Current selections">
                  {value.map((val, index) => (
                    <Chip
                      key={index}
                      onClick={(ev) => {
                        ev.stopPropagation();
                        onChange(value.filter((e) => e !== val));
                      }}
                    >
                      {val}
                    </Chip>
                  ))}
                </ChipGroup>
              </TextInputGroupMain>
              <TextInputGroupUtilities>
                {value.length > 0 && (
                  <Button
                    variant="plain"
                    onClick={() => {
                      setSupplierId("");
                      setValue("supplierIdsAppliedTo", []);
                      supplierIdRef?.current?.focus();
                    }}
                    aria-label="Clear input value"
                  >
                    <TimesIcon aria-hidden />
                  </Button>
                )}
              </TextInputGroupUtilities>
            </TextInputGroup>
          );
        }}
      />

      <HookFormPFTextInput
        control={control}
        name="name"
        label="Nombre"
        fieldId="name"
        isRequired
      />
      <HookFormPFTextArea
        control={control}
        name="description"
        label="Descripción"
        fieldId="description"
        resizeOrientation="vertical"
      />

      <FormFieldGroupExpandable
        toggleAriaLabel="SOAP"
        isExpanded
        header={
          <FormFieldGroupHeader
            titleText={{
              id: "field-group-soap",
              text: "SOAP",
            }}
            actions={
              <>
                <Button
                  variant="link"
                  onClick={() => {
                    setValue("usernameSol", "");
                    setValue("passwordSol", "");
                    setValue("urlInvoice", "");
                    setValue("urlPerceptionRetention", "");

                    trigger([
                      "usernameSol",
                      "passwordSol",
                      "urlInvoice",
                      "urlPerceptionRetention",
                    ]);
                  }}
                >
                  Limpiar todo
                </Button>{" "}
                <Button
                  variant="secondary"
                  onClick={() => {
                    setValue("usernameSol", "12345678959MODDATOS");
                    setValue("passwordSol", "MODDATOS");
                    setValue(
                      "urlInvoice",
                      "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService"
                    );
                    setValue(
                      "urlPerceptionRetention",
                      "https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService"
                    );

                    trigger([
                      "usernameSol",
                      "passwordSol",
                      "urlInvoice",
                      "urlPerceptionRetention",
                    ]);
                  }}
                >
                  Pruebas Beta
                </Button>{" "}
                <Button
                  variant="secondary"
                  onClick={() => {
                    setValue("usernameSol", "");
                    setValue("passwordSol", "");
                    setValue(
                      "urlInvoice",
                      "https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService"
                    );
                    setValue(
                      "urlPerceptionRetention",
                      "https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService"
                    );

                    trigger([
                      "usernameSol",
                      "passwordSol",
                      "urlInvoice",
                      "urlPerceptionRetention",
                    ]);
                  }}
                >
                  Producción
                </Button>
              </>
            }
          />
        }
      >
        <HookFormPFTextInput
          control={control}
          name="usernameSol"
          label="Usuario SOL"
          fieldId="usernameSol"
          isRequired
        />
        <HookFormPFTextInput
          control={control}
          name="passwordSol"
          label="Contraseña SOL"
          fieldId="passwordSol"
          isRequired
          formGroupProps={{
            labelIcon: !isPasswordEncrypted ? (
              <KeyDisplayToggle
                keyName="password"
                isKeyHidden={isPasswordHidden}
                onClick={toggleHidePassword}
              />
            ) : undefined,
          }}
          type={isPasswordHidden ? "password" : "text"}
          onFocus={() => resetField("passwordSol")}
        />
        <HookFormPFTextInput
          control={control}
          name="urlInvoice"
          label="Invoice URL"
          fieldId="urlInvoice"
          isRequired
        />
        <HookFormPFTextInput
          control={control}
          name="urlPerceptionRetention"
          label="Perception/Retention URL"
          fieldId="urlPerceptionRetention"
          isRequired
        />
      </FormFieldGroupExpandable>

      <FormFieldGroupExpandable
        toggleAriaLabel="REST"
        header={
          <FormFieldGroupHeader
            titleText={{
              id: "field-group-rest",
              text: "REST",
            }}
            actions={
              <>
                <Button
                  variant="link"
                  onClick={() => {
                    setValue("clientId", "");
                    setValue("clientSecret", "");
                    setValue("urlDespatch", "");

                    trigger(["clientId", "clientSecret", "urlDespatch"]);
                  }}
                >
                  Limpiar todo
                </Button>{" "}
                <Button
                  variant="secondary"
                  onClick={() => {
                    setValue("clientId", "");
                    setValue("clientSecret", "");
                    setValue(
                      "urlDespatch",
                      "https://api-cpe.sunat.gob.pe/v1/contribuyente/gem"
                    );

                    trigger(["clientId", "clientSecret", "urlDespatch"]);
                  }}
                >
                  Producción
                </Button>
              </>
            }
          />
        }
      >
        <HookFormPFTextInput
          control={control}
          name="clientId"
          label="Client ID"
          fieldId="clientId"
          isRequired
        />
        <HookFormPFTextInput
          control={control}
          name="clientSecret"
          label="Client Secret"
          fieldId="clientSecret"
          isRequired
          formGroupProps={{
            labelIcon: !isPasswordEncrypted ? (
              <KeyDisplayToggle
                keyName="password"
                isKeyHidden={isPasswordHidden}
                onClick={toggleHidePassword}
              />
            ) : undefined,
          }}
          type={isPasswordHidden ? "password" : "text"}
          onFocus={() => resetField("clientSecret")}
        />
        <HookFormPFTextInput
          control={control}
          name="urlDespatch"
          label="Despatch URL"
          fieldId="urlDespatch"
          isRequired
        />
      </FormFieldGroupExpandable>

      <ActionGroup>
        <Button
          type="submit"
          aria-label="submit"
          id="organization-form-submit"
          variant={ButtonVariant.primary}
          isDisabled={!isValid || isSubmitting || isValidating || !isDirty}
        >
          {!credentials ? "Crear" : "Guardar"}
        </Button>
        <Button
          type="button"
          id="cancel"
          aria-label="cancel"
          variant={ButtonVariant.link}
          isDisabled={isSubmitting || isValidating}
          onClick={onClose}
        >
          Cancelar
        </Button>
      </ActionGroup>
    </Form>
  );
};

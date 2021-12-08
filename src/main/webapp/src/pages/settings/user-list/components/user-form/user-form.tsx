import React, { useState } from "react";
import { useFormik, FormikProvider, FormikHelpers } from "formik";
import { object, string, array } from "yup";

import {
  ActionGroup,
  Alert,
  Button,
  ButtonVariant,
  Form,
  FormGroup,
  InputGroup,
  TextInput,
} from "@patternfly/react-core";
import { EditAltIcon } from "@patternfly/react-icons";

import { useCreateUserMutation, useUpdateUserMutation } from "queries/users";
import { FormikSelectMultiple } from "shared/components";

import { User } from "api/models";
import {
  getAxiosErrorMessage,
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";
import { ALL_PERMISSIONS, Permission } from "Constants";

export interface FormValues {
  fullName: string;
  username: string;
  password: string;
  permissions: Permission[];
}

export interface UserFormProps {
  user?: User;
  onSaved: (user: User) => void;
  onCancel: () => void;
}

export const UserForm: React.FC<UserFormProps> = ({
  user,
  onSaved,
  onCancel,
}) => {
  const createUserMutation = useCreateUserMutation();
  const updateUserMutation = useUpdateUserMutation();

  const [error, setError] = useState();
  const [isEditingPassword, setIsEditingPassword] = useState(false);

  const initialValues: FormValues = {
    fullName: user?.fullName || "",
    username: user?.username || "",
    password: "",
    permissions: user?.permissions || [],
  };

  const validationSchema = object().shape({
    fullName: string().trim().max(250),
    username: string()
      .trim()
      .required()
      .min(3)
      .max(120)
      .matches(/^[a-zA-Z0-9._-]{3,}$/),
    password:
      user && !isEditingPassword
        ? string().max(250)
        : string().required().min(3).max(250),
    permissions: array().required().min(1),
  });

  const onSubmit = (
    formValues: FormValues,
    formikHelpers: FormikHelpers<FormValues>
  ) => {
    const payload: User = {
      fullName: formValues.fullName.trim(),
      username: formValues.username,
      password: (formValues.password !== ""
        ? formValues.password
        : undefined) as any,
      permissions: formValues.permissions,
    };
    let promise: Promise<User>;
    if (user) {
      promise = updateUserMutation.mutateAsync({ ...user, ...payload });
    } else {
      promise = createUserMutation.mutateAsync(payload);
    }
    promise
      .then((response) => {
        formikHelpers.setSubmitting(false);
        onSaved(response);
      })
      .catch((error) => {
        formikHelpers.setSubmitting(false);
        setError(error);
      });
  };

  let formik = useFormik({
    enableReinitialize: true,
    initialValues: initialValues,
    validationSchema: validationSchema,
    onSubmit: onSubmit,
  });

  const onChangeField = (value: string, event: React.FormEvent<any>) => {
    formik.handleChange(event);
  };

  return (
    <FormikProvider value={formik}>
      <Form onSubmit={formik.handleSubmit}>
        {error && (
          <Alert
            variant="danger"
            isInline
            title={getAxiosErrorMessage(error)}
          />
        )}
        <FormGroup
          label="Nombre"
          fieldId="fullName"
          isRequired={false}
          validated={getValidatedFromError(formik.errors.fullName)}
          helperTextInvalid={formik.errors.fullName}
        >
          <TextInput
            type="text"
            name="fullName"
            aria-label="fullName"
            aria-describedby="fullName"
            isRequired={false}
            onChange={onChangeField}
            onBlur={formik.handleBlur}
            value={formik.values.fullName}
            validated={getValidatedFromErrorTouched(
              formik.errors.fullName,
              formik.touched.fullName
            )}
          />
        </FormGroup>
        <FormGroup
          label="Permisos"
          fieldId="permissions"
          isRequired={true}
          validated={getValidatedFromError(formik.errors.permissions)}
          helperTextInvalid={formik.errors.permissions}
        >
          <FormikSelectMultiple
            fieldConfig={{ name: "permissions" }}
            selectConfig={{
              variant: "checkbox",
              "aria-label": "permissions",
              "aria-describedby": "permissions",
              placeholderText: "Permisos asignados",
            }}
            options={ALL_PERMISSIONS}
            isEqual={(a, b) => a === b}
          />
        </FormGroup>
        <FormGroup
          label="Usuario"
          fieldId="username"
          isRequired={true}
          validated={getValidatedFromError(formik.errors.username)}
          helperTextInvalid={formik.errors.username}
        >
          <TextInput
            type="text"
            name="username"
            aria-label="username"
            aria-describedby="username"
            isRequired={true}
            onChange={onChangeField}
            onBlur={formik.handleBlur}
            value={formik.values.username}
            validated={getValidatedFromErrorTouched(
              formik.errors.username,
              formik.touched.username
            )}
            autoComplete="off"
          />
        </FormGroup>
        {user ? (
          <FormGroup
            label="Contraseña"
            fieldId="password"
            isRequired={isEditingPassword}
            validated={
              isEditingPassword
                ? getValidatedFromError(formik.errors.password)
                : "default"
            }
            helperTextInvalid={isEditingPassword ? formik.errors.password : ""}
          >
            <InputGroup>
              <TextInput
                type="password"
                name="password"
                aria-label="password"
                aria-describedby="password"
                isRequired={isEditingPassword}
                onChange={onChangeField}
                onBlur={formik.handleBlur}
                value={isEditingPassword ? formik.values.password : "******"}
                validated={
                  isEditingPassword
                    ? getValidatedFromErrorTouched(
                        formik.errors.password,
                        formik.touched.password
                      )
                    : "default"
                }
                isDisabled={!isEditingPassword}
              />
              <Button
                variant="control"
                aria-label="change-password"
                onClick={() => {
                  setIsEditingPassword((current) => {
                    formik.setFieldValue("password", "");
                    return !current;
                  });
                }}
              >
                <EditAltIcon />
              </Button>
            </InputGroup>
          </FormGroup>
        ) : (
          <FormGroup
            label="Contraseña"
            fieldId="password"
            isRequired={true}
            validated={getValidatedFromError(formik.errors.password)}
            helperTextInvalid={formik.errors.password}
          >
            <TextInput
              type="password"
              name="password"
              aria-label="password"
              aria-describedby="password"
              isRequired={true}
              onChange={onChangeField}
              onBlur={formik.handleBlur}
              value={formik.values.password}
              validated={getValidatedFromErrorTouched(
                formik.errors.password,
                formik.touched.password
              )}
            />
          </FormGroup>
        )}

        <ActionGroup>
          <Button
            type="submit"
            aria-label="submit"
            variant={ButtonVariant.primary}
            isDisabled={
              !formik.isValid ||
              !formik.dirty ||
              formik.isSubmitting ||
              formik.isValidating
            }
          >
            {!user ? "Crear" : "Guardar"}
          </Button>
          <Button
            type="button"
            aria-label="cancel"
            variant={ButtonVariant.link}
            isDisabled={formik.isSubmitting || formik.isValidating}
            onClick={onCancel}
          >
            Cancelar
          </Button>
        </ActionGroup>
      </Form>
    </FormikProvider>
  );
};

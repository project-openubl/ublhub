import React, { useEffect } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import axios, { AxiosError } from "axios";
import { useTranslation } from "react-i18next";

import { Controller, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { object, string } from "yup";

import {
  ActionGroup,
  Button,
  Card,
  CardBody,
  Form,
  FormGroup,
  TextArea,
  TextInput,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { ResolvedQueries } from "@migtools/lib-ui";
import {
  NotificationContext
} from "@project-openubl/lib-ui";

import { ProjectDto } from "api/models";
import { useUpdateProjectMutation } from "queries/projects";
import {
  getValidatedFromError,
  getValidatedFromErrorTouched,
} from "utils/modelUtils";

interface IGeneralForm {
  name: string;
  description: string;
}

const General: React.FC = () => {
  const { t } = useTranslation();

  const navigate = useNavigate();
  const { pushNotification } = React.useContext(NotificationContext);

  const project = useOutletContext<ProjectDto | null>();
  const updateProjectMutation = useUpdateProjectMutation((p) => {
    pushNotification({
      title: t("info.data-saved"),
      message: "",
      key: p.name,
      variant: "success",
      actionClose: true,
      timeout: 4000,
    });
  });

  const {
    control,
    formState: { errors, isValid, isValidating, isDirty },
    reset,
    getValues,
  } = useForm<IGeneralForm>({
    defaultValues: { name: "", description: "" },
    resolver: yupResolver(
      object().shape({
        name: string()
          .trim()
          .required()
          .max(250)
          .test("duplicateName", (value, options) => {
            return axios
              .post<string>("/projects/check-name", { name: value })
              .then(() => true)
              .catch((error: AxiosError) => {
                return value === project?.name
                  ? true
                  : options.createError({ message: error.response?.data });
              });
          }),
        description: string().trim().max(250),
      })
    ),
    mode: "onChange",
  });

  useEffect(() => {
    if (project) {
      reset({ name: project.name, description: project.description });
    }
  }, [project, reset]);

  const save = () => {
    if (!project) {
      return;
    }

    updateProjectMutation.mutate({
      ...project,
      ...getValues(),
    });
  };

  return (
    <Card>
      <CardBody>
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
                  validated={
                    !isTouched ? "default" : error ? "error" : "success"
                  }
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
              onClick={save}
              isDisabled={
                !isDirty ||
                !isValid ||
                isValidating ||
                updateProjectMutation.isLoading
              }
            >
              {t("actions.save")}
            </Button>
            <Button variant="link" onClick={() => navigate("/projects")}>
              {t("actions.cancel")}
            </Button>
          </ActionGroup>

          <ResolvedQueries
            resultsWithErrorTitles={[
              {
                result: updateProjectMutation,
                errorTitle: "Could not save data",
              },
            ]}
            spinnerMode="inline"
          />
        </Form>
      </CardBody>
    </Card>
  );
};

export default General;

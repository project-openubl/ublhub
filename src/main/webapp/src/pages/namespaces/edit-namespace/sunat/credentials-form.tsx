import React, { useEffect } from "react";
import * as yup from "yup";
import { useTranslation } from "react-i18next";

import {
  ActionGroup,
  Button,
  Form,
  Grid,
  GridItem,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import {
  useFormField,
  useFormState,
  ValidatedTextInput,
} from "@konveyor/lib-ui";
import { SimplePlaceholder } from "@project-openubl/lib-ui";

import { QuerySpinnerMode, ResolvedQueries } from "shared/components";

import {
  useNamespacesQuery,
  useUpdateNamespaceMutation,
} from "queries/namespaces";
import { LONG_LOADING_MESSAGE } from "queries/constants";

import { Namespace } from "api/models";

const useCredentialsFormState = () => {
  const form = useFormState({
    sunatUsername: useFormField<string>(
      "",
      yup.string().trim().min(3).max(250)
    ),
    sunatPassword: useFormField<string>("", yup.string().min(3).max(250)),
  });

  return form;
};

export type CredentialsFormState = ReturnType<typeof useCredentialsFormState>;

const useCredentialsPrefillEffect = (
  form: CredentialsFormState,
  namespaceBeingPrefilled: Namespace | null
) => {
  const [isStartedPrefilling, setIsStartedPrefilling] = React.useState(false);
  const [isDonePrefilling, setIsDonePrefilling] = React.useState(false);

  useEffect(() => {
    if (!isStartedPrefilling && namespaceBeingPrefilled) {
      setIsStartedPrefilling(true);

      form.fields.sunatUsername.prefill(
        namespaceBeingPrefilled.credentials.username
      );

      // Wait for effects to run based on field changes first
      window.setTimeout(() => {
        setIsDonePrefilling(true);
      }, 0);
    }
  }, [isStartedPrefilling, form, namespaceBeingPrefilled]);

  return {
    isDonePrefilling,
  };
};

interface ICredentialsFormProps {
  namespaceId: string;
}

export const CredentialsForm: React.FunctionComponent<ICredentialsFormProps> =
  ({ namespaceId }: ICredentialsFormProps) => {
    const { t } = useTranslation();

    const namespacesQuery = useNamespacesQuery();
    const mutateNamespace = useUpdateNamespaceMutation();

    const namespaceBeingPrefilled =
      namespacesQuery.data?.find((ns) => ns.id === namespaceId) || null;

    const form = useCredentialsFormState();
    const { isDonePrefilling } = useCredentialsPrefillEffect(
      form,
      namespaceBeingPrefilled
    );

    const allMutationResults = [mutateNamespace];
    const allMutationErrorTitles = ["Can not update namespace"];

    const onSave = () => {
      mutateNamespace.mutate({
        ...namespaceBeingPrefilled!,
        credentials: {
          username: form.fields.sunatUsername.value,
          password: form.fields.sunatPassword.value,
        },
      });
    };

    return (
      <ResolvedQueries
        results={[namespacesQuery]}
        errorTitles={["Cannot load namespaces"]}
        errorsInline={false}
        className={spacing.mMd}
        emptyStateBody={LONG_LOADING_MESSAGE}
      >
        {!isDonePrefilling ? (
          <SimplePlaceholder />
        ) : (
          <Grid md={4}>
            <GridItem>
              <br />
              <Form className={spacing.pbXl} onSubmit={onSave}>
                <ValidatedTextInput
                  field={form.fields.sunatUsername}
                  label="Usuario"
                  isRequired
                  fieldId="sunatUsername"
                />
                <ValidatedTextInput
                  field={form.fields.sunatPassword}
                  label="Contraseña"
                  isRequired
                  fieldId="sunatPassword"
                  type="password"
                />

                <ResolvedQueries
                  results={allMutationResults}
                  errorTitles={allMutationErrorTitles}
                  spinnerMode={QuerySpinnerMode.Inline}
                />
                <ActionGroup>
                  <Button
                    variant="primary"
                    type="submit"
                    isDisabled={
                      !form.isDirty ||
                      !form.isValid ||
                      mutateNamespace.isLoading
                    }
                  >
                    {t("actions.save")}
                  </Button>
                </ActionGroup>
              </Form>
            </GridItem>
          </Grid>
        )}
      </ResolvedQueries>
    );
  };

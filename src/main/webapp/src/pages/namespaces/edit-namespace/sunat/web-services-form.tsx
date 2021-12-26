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

const useWebServicesFormState = () => {
  const form = useFormState({
    urlFactura: useFormField<string>(
      "",
      yup.string().required().trim().url().max(250)
    ),
    urlGuiaRemision: useFormField<string>(
      "",
      yup.string().required().trim().url().max(250)
    ),
    urlPercepcionRetencion: useFormField<string>(
      "",
      yup.string().required().trim().url().max(250)
    ),
  });

  return form;
};

export type WebServicesFormState = ReturnType<typeof useWebServicesFormState>;

const useWebServicesPrefillEffect = (
  form: WebServicesFormState,
  namespaceBeingPrefilled: Namespace | null
) => {
  const [isStartedPrefilling, setIsStartedPrefilling] = React.useState(false);
  const [isDonePrefilling, setIsDonePrefilling] = React.useState(false);

  useEffect(() => {
    if (!isStartedPrefilling && namespaceBeingPrefilled) {
      setIsStartedPrefilling(true);

      form.fields.urlFactura.prefill(
        namespaceBeingPrefilled.webServices.factura
      );
      form.fields.urlGuiaRemision.prefill(
        namespaceBeingPrefilled.webServices.guia
      );
      form.fields.urlPercepcionRetencion.prefill(
        namespaceBeingPrefilled.webServices.retenciones
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

interface IWebServicesFormProps {
  namespaceId: string;
}

export const WebServicesForm: React.FunctionComponent<IWebServicesFormProps> =
  ({ namespaceId }) => {
    const { t } = useTranslation();

    const namespacesQuery = useNamespacesQuery();
    const mutateNamespace = useUpdateNamespaceMutation();

    const namespaceBeingPrefilled =
      namespacesQuery.data?.find((ns) => ns.id === namespaceId) || null;

    const form = useWebServicesFormState();
    const { isDonePrefilling } = useWebServicesPrefillEffect(
      form,
      namespaceBeingPrefilled
    );

    const allMutationResults = [mutateNamespace];
    const allMutationErrorTitles = ["Can not update namespace"];

    const onSave = () => {
      mutateNamespace.mutate({
        ...namespaceBeingPrefilled!,
        webServices: {
          factura: form.fields.urlFactura.value,
          guia: form.fields.urlGuiaRemision.value,
          retenciones: form.fields.urlPercepcionRetencion.value,
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
          <Grid md={6}>
            <GridItem>
              <br />
              <Form className={spacing.pbXl} onSubmit={onSave}>
                <ValidatedTextInput
                  field={form.fields.urlFactura}
                  label="Factura electrónica"
                  isRequired
                  fieldId="urlFactura"
                />
                <ValidatedTextInput
                  field={form.fields.urlGuiaRemision}
                  label="Guía de remisión electrónica"
                  isRequired
                  fieldId="urlGuiaRemision"
                />
                <ValidatedTextInput
                  field={form.fields.urlPercepcionRetencion}
                  label="Retención y percepción"
                  isRequired
                  fieldId="urlPercepcionRetencion"
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

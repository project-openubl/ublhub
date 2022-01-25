import React from "react";
import { useTranslation } from "react-i18next";
import { Form } from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";
import { ValidatedTextInput } from "@konveyor/lib-ui";

import { usePausedPollingEffect } from "shared/context";
import { NamespaceWizardFormState } from "./add-namespace-wizard";

interface ICredentialsFormProps {
  form: NamespaceWizardFormState["credentials"];
}

export const CredentialsForm: React.FunctionComponent<ICredentialsFormProps> =
  ({ form }: ICredentialsFormProps) => {
    const { t } = useTranslation();

    usePausedPollingEffect();

    return (
      <Form className={spacing.pbXl}>
        <ValidatedTextInput
          field={form.fields.sunatUsername}
          label={t("terms.username")}
          isRequired
          fieldId="sunatUsername"
        />
        <ValidatedTextInput
          field={form.fields.sunatPassword}
          label={t("terms.password")}
          isRequired
          fieldId="sunatPassword"
          type="password"
        />
      </Form>
    );
  };

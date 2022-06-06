import React from "react";
import { Form, TextArea } from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";
import { ValidatedTextInput } from "@konveyor/lib-ui";

import { NamespaceWizardFormState } from "./add-namespace-wizard";

interface IGeneralFormProps {
  form: NamespaceWizardFormState["general"];
}

export const GeneralForm: React.FunctionComponent<IGeneralFormProps> = ({
  form,
}: IGeneralFormProps) => {
  return (
    <Form className={spacing.pbXl}>
      <ValidatedTextInput
        field={form.fields.name}
        label="Nombre"
        isRequired
        fieldId="name"
      />
      <ValidatedTextInput
        component={TextArea}
        field={form.fields.description}
        label="Descripción"
        fieldId="description"
      />
    </Form>
  );
};

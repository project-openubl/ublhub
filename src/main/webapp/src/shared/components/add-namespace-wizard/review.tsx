import React from "react";
import {
  TextContent,
  Text,
  Form,
  DescriptionList,
  DescriptionListGroup,
  DescriptionListTerm,
  DescriptionListDescription,
} from "@patternfly/react-core";

import { usePausedPollingEffect } from "shared/context";
import { NamespaceWizardFormState } from "./add-namespace-wizard";

interface IReviewProps {
  forms: NamespaceWizardFormState;
}

export const Review: React.FunctionComponent<IReviewProps> = ({
  forms,
}: IReviewProps) => {
  usePausedPollingEffect();

  return (
    <Form>
      <TextContent>
        <Text component="p">
          Revisa la información mostrada y haga click en Finalizar para crear tu
          Namespace. Use el botón Atrás para hacer cambios.
        </Text>
      </TextContent>
      <DescriptionList isHorizontal>
        <DescriptionListGroup>
          <DescriptionListTerm>Nombre</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.general.values.name}
          </DescriptionListDescription>
        </DescriptionListGroup>
        {forms.general.values.description ? (
          <DescriptionListGroup>
            <DescriptionListTerm>Descripción</DescriptionListTerm>
            <DescriptionListDescription>
              {forms.general.values.description}
            </DescriptionListDescription>
          </DescriptionListGroup>
        ) : null}

        <DescriptionListGroup>
          <DescriptionListTerm>SUNAT factura URL</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.webServices.values.urlFactura}
          </DescriptionListDescription>
        </DescriptionListGroup>
        <DescriptionListGroup>
          <DescriptionListTerm>SUNAT guía de remisión URL</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.webServices.values.urlGuiaRemision}
          </DescriptionListDescription>
        </DescriptionListGroup>
        <DescriptionListGroup>
          <DescriptionListTerm>
            SUNAT percepción y retención URL
          </DescriptionListTerm>
          <DescriptionListDescription>
            {forms.webServices.values.urlPercepcionRetencion}
          </DescriptionListDescription>
        </DescriptionListGroup>

        <DescriptionListGroup>
          <DescriptionListTerm>SUNAT usuario</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.credentials.values.sunatUsername}
          </DescriptionListDescription>
        </DescriptionListGroup>
        <DescriptionListGroup>
          <DescriptionListTerm>SUNAT contraseña</DescriptionListTerm>
          <DescriptionListDescription>******</DescriptionListDescription>
        </DescriptionListGroup>
      </DescriptionList>
    </Form>
  );
};

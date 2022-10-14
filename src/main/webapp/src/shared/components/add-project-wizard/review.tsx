import React from "react";
import { UseFormReturn } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { ResolvedQueries } from "@migtools/lib-ui";

import {
  TextContent,
  Text,
  Form,
  DescriptionList,
  DescriptionListGroup,
  DescriptionListTerm,
  DescriptionListDescription,
} from "@patternfly/react-core";

import { UnknownResult } from "api/models";

import { IWebServicesForm } from "./web-services-form";
import {
  ICredentialsForm,
} from "./credentials-form";
import { IGeneralForm } from "./add-project-wizard";

interface IReviewProps {
  forms: {
    general: UseFormReturn<IGeneralForm>;
    webServices: UseFormReturn<IWebServicesForm>;
    credentials: UseFormReturn<ICredentialsForm>;
  };
  allMutationResultsWithErrorTitles: {
    result: UnknownResult;
    errorTitle: string;
  }[];
}

export const Review: React.FunctionComponent<IReviewProps> = ({
  forms,
  allMutationResultsWithErrorTitles,
}: IReviewProps) => {
  const { t } = useTranslation();

  return (
    <Form>
      <TextContent>
        <Text component="p">
          Revisa la información mostrada y haga click en "Crear" para crear tu
          Proyecto. Use el botón "Atrás" para hacer cambios.
        </Text>
      </TextContent>
      <DescriptionList isHorizontal>
        <DescriptionListGroup>
          <DescriptionListTerm>{t("terms.name")}</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.general.getValues().name}
          </DescriptionListDescription>
        </DescriptionListGroup>
        {forms.general.getValues().description ? (
          <DescriptionListGroup>
            <DescriptionListTerm>{t("terms.description")}</DescriptionListTerm>
            <DescriptionListDescription>
              {forms.general.getValues().description}
            </DescriptionListDescription>
          </DescriptionListGroup>
        ) : null}

        <DescriptionListGroup>
          <DescriptionListTerm>Factura</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.webServices.getValues().factura}
          </DescriptionListDescription>
        </DescriptionListGroup>
        <DescriptionListGroup>
          <DescriptionListTerm>Guia</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.webServices.getValues().guia}
          </DescriptionListDescription>
        </DescriptionListGroup>
        <DescriptionListGroup>
          <DescriptionListTerm>Retención</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.webServices.getValues().retencion}
          </DescriptionListDescription>
        </DescriptionListGroup>

        <DescriptionListGroup>
          <DescriptionListTerm>{t("terms.username")}</DescriptionListTerm>
          <DescriptionListDescription>
            {forms.credentials.getValues().username}
          </DescriptionListDescription>
        </DescriptionListGroup>
        <DescriptionListGroup>
          <DescriptionListTerm>{t("terms.password")}</DescriptionListTerm>
          <DescriptionListDescription>******</DescriptionListDescription>
        </DescriptionListGroup>

        <ResolvedQueries
          resultsWithErrorTitles={allMutationResultsWithErrorTitles}
          spinnerMode="inline"
        />
      </DescriptionList>
    </Form>
  );
};

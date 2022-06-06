import React from "react";
import {
  Button,
  Flex,
  FlexItem,
  Form,
  FormSection,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { ValidatedTextInput } from "@konveyor/lib-ui";

import { usePausedPollingEffect } from "shared/context";

import { NamespaceWizardFormState } from "./add-namespace-wizard";

interface SunatURls {
  factura: string;
  guiaRemision: string;
  percepcionRetencion: string;
}

const sunatProdUrls: SunatURls = {
  factura: "https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl",
  guiaRemision:
    "https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl",
  percepcionRetencion:
    "https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl",
};

const sunatBetaUrls: SunatURls = {
  factura: "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService",
  guiaRemision:
    "https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService",
  percepcionRetencion:
    "https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService",
};

interface IWebServicesFormProps {
  form: NamespaceWizardFormState["webServices"];
}

export const WebServicesForm: React.FunctionComponent<IWebServicesFormProps> =
  ({ form }: IWebServicesFormProps) => {
    usePausedPollingEffect();

    const fillForm = (data: SunatURls) => {
      form.fields.urlFactura.setValue(data.factura);
      form.fields.urlGuiaRemision.setValue(data.guiaRemision);
      form.fields.urlPercepcionRetencion.setValue(data.percepcionRetencion);
    };

    return (
      <Form className={spacing.pbXl}>
        <FormSection>
          <Flex>
            <FlexItem>
              <Button
                variant="secondary"
                onClick={() => fillForm(sunatProdUrls)}
              >
                SUNAT Producción
              </Button>
            </FlexItem>
            <FlexItem>
              <Button
                variant="secondary"
                onClick={() => fillForm(sunatBetaUrls)}
              >
                SUNAT Beta
              </Button>
            </FlexItem>
          </Flex>
        </FormSection>
        <FormSection>
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
        </FormSection>
      </Form>
    );
  };

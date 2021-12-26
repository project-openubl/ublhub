import React from "react";
import { useParams } from "react-router-dom";
import {
  Card,
  CardBody,
  DescriptionList,
  DescriptionListDescription,
  DescriptionListGroup,
  DescriptionListTerm,
} from "@patternfly/react-core";
import { useNamespacesQuery } from "queries/namespaces";

import { INamespaceParams } from "../edit-namespace";

export const Overview: React.FC = () => {
  const routeParams = useParams<INamespaceParams>();
  const namespacesQuery = useNamespacesQuery();

  const prefillNamespaceId = routeParams.namespaceId;
  const namespaceBeingPrefilled =
    namespacesQuery.data?.find((ns) => ns.id === prefillNamespaceId) || null;

  return (
    <Card>
      <CardBody>
        <DescriptionList>
          <DescriptionListGroup>
            <DescriptionListTerm>Nombre</DescriptionListTerm>
            <DescriptionListDescription>
              {namespaceBeingPrefilled?.name}
            </DescriptionListDescription>
          </DescriptionListGroup>
          {namespaceBeingPrefilled?.description ? (
            <DescriptionListGroup>
              <DescriptionListTerm>Descripción</DescriptionListTerm>
              <DescriptionListDescription>
                {namespaceBeingPrefilled?.description}
              </DescriptionListDescription>
            </DescriptionListGroup>
          ) : null}

          <DescriptionListGroup>
            <DescriptionListTerm>SUNAT factura URL</DescriptionListTerm>
            <DescriptionListDescription>
              {namespaceBeingPrefilled?.webServices.factura}
            </DescriptionListDescription>
          </DescriptionListGroup>
          <DescriptionListGroup>
            <DescriptionListTerm>
              SUNAT guía de remisión URL
            </DescriptionListTerm>
            <DescriptionListDescription>
              {namespaceBeingPrefilled?.webServices.guia}
            </DescriptionListDescription>
          </DescriptionListGroup>
          <DescriptionListGroup>
            <DescriptionListTerm>
              SUNAT percepción y retención URL
            </DescriptionListTerm>
            <DescriptionListDescription>
              {namespaceBeingPrefilled?.webServices.retenciones}
            </DescriptionListDescription>
          </DescriptionListGroup>

          <DescriptionListGroup>
            <DescriptionListTerm>SUNAT usuario</DescriptionListTerm>
            <DescriptionListDescription>
              {namespaceBeingPrefilled?.credentials.username}
            </DescriptionListDescription>
          </DescriptionListGroup>
          <DescriptionListGroup>
            <DescriptionListTerm>SUNAT contraseña</DescriptionListTerm>
            <DescriptionListDescription>******</DescriptionListDescription>
          </DescriptionListGroup>
        </DescriptionList>{" "}
      </CardBody>
    </Card>
  );
};

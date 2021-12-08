import React, { useState } from "react";

import { SimplePlaceholder, ConditionalRender } from "@project-openubl/lib-ui";

import {
  Card,
  CardBody,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  PageSection,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import { SearchIcon } from "@patternfly/react-icons";

import { useContribuyenteQuery } from "queries/contribuyentes";
import {
  SimplePageSection,
  SearchInput,
  ContribuyenteDetails,
} from "shared/components";

export const ConsultaRuc: React.FC = () => {
  const [numeroDocumento, setNumeroDocumento] = useState("");
  const contribuyente = useContribuyenteQuery(numeroDocumento);

  const handleOnSearch = (numeroDocumento: string) => {
    if (numeroDocumento.trim().length > 0) {
      setNumeroDocumento(numeroDocumento);
    }
  };

  return (
    <>
      <SimplePageSection
        title="Buscar por 'Número de documento'"
        description="Ingresa el número de RUC o DNI que deseas consultar."
      />
      <PageSection>
        <div
          style={{ backgroundColor: "var(--pf-global--BackgroundColor--100)" }}
        >
          <Toolbar>
            <ToolbarContent>
              <ToolbarGroup>
                <ToolbarItem>
                  <SearchInput onSearch={handleOnSearch} />
                </ToolbarItem>
              </ToolbarGroup>
            </ToolbarContent>
          </Toolbar>
          <Card>
            <CardBody>
              <ConditionalRender
                when={contribuyente.isFetching}
                then={<SimplePlaceholder />}
              >
                <ConditionalRender
                  when={!!contribuyente.isError}
                  then={
                    <EmptyState variant={EmptyStateVariant.small}>
                      <EmptyStateIcon icon={SearchIcon} />
                      <Title headingLevel="h2" size="lg">
                        No se encontraron resultados
                      </Title>
                      <EmptyStateBody>
                        No se encontraron resultados que coincidan con el
                        criterio de búsqueda.
                      </EmptyStateBody>
                    </EmptyState>
                  }
                >
                  {contribuyente.data && (
                    <ContribuyenteDetails value={contribuyente.data} />
                  )}
                </ConditionalRender>
              </ConditionalRender>
            </CardBody>
          </Card>
        </div>
      </PageSection>
    </>
  );
};

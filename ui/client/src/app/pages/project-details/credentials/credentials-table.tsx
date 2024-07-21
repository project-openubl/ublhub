import React from "react";
import {
  Button,
  ButtonVariant,
  Modal,
  ModalVariant,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import { ActionsColumn, Td as TdAction } from "@patternfly/react-table";

import { Credentials } from "@app/api/models";
import { useFetchCredentials } from "@app/queries/credentials";
import {
  ConditionalTableBody,
  FilterType,
  useClientTableBatteries,
} from "@mturley-latest/react-table-batteries";
import { CredentialsForm } from "./components/credentials-form";

interface ICredentialsTableProps {
  projectId: string | number;
}

export const CredentialsTable: React.FC<ICredentialsTableProps> = ({
  projectId,
}) => {
  const [createUpdateModalState, setCreateUpdateModalState] = React.useState<
    "create" | Credentials | null
  >(null);
  const isCreateUpdateModalOpen = createUpdateModalState !== null;
  const credentialsToUpdate =
    createUpdateModalState !== "create" ? createUpdateModalState : null;

  const [isConfirmDialogOpen, setIsConfirmDialogOpen] =
    React.useState<boolean>(false);
  const [credentialsToDelete, setCredentialsToDelete] =
    React.useState<Credentials>();

  const { credentials, isFetching, fetchError, refetch } =
    useFetchCredentials(projectId);

  const tableControls = useClientTableBatteries({
    persistTo: "state",
    idProperty: "id",
    items: credentials,
    isLoading: isFetching,
    columnNames: {
      name: "Nombre",
      description: "Descripción",
      supplierIds: "RUCs",
    },
    hasActionsColumn: true,
    filter: {
      isEnabled: true,
      filterCategories: [
        {
          key: "q",
          title: "Nombre",
          type: FilterType.search,
          placeholderText: "Filtrar por nombre...",
          getItemValue: (item) => item.name || "",
        },
      ],
    },
    sort: {
      isEnabled: true,
      sortableColumns: ["name"],
      getSortValues: (project) => ({
        name: project?.name || "",
      }),
    },
    pagination: { isEnabled: true },
  });

  const {
    currentPageItems,
    numRenderedColumns,
    components: {
      Table,
      Thead,
      Tr,
      Th,
      Tbody,
      Td,
      Toolbar,
      FilterToolbar,
      PaginationToolbarItem,
      Pagination,
    },
  } = tableControls;

  const closeCreateUpdateModal = () => {
    setCreateUpdateModalState(null);
    refetch;
  };

  const deleteRow = (row: Credentials) => {
    setCredentialsToDelete(row);
    setIsConfirmDialogOpen(true);
  };

  return (
    <>
      <Toolbar>
        <ToolbarContent>
          <FilterToolbar id="credentials-toolbar" />
          <ToolbarGroup variant="button-group">
            <ToolbarItem>
              <Button
                type="button"
                id="create-credentials"
                aria-label="Create new credential"
                variant={ButtonVariant.primary}
                onClick={() => setCreateUpdateModalState("create")}
              >
                Crear Credenciales
              </Button>
            </ToolbarItem>
          </ToolbarGroup>
          <PaginationToolbarItem>
            <Pagination
              variant="top"
              isCompact
              widgetId="credentials-pagination-top"
            />
          </PaginationToolbarItem>
        </ToolbarContent>
      </Toolbar>

      <Table aria-label="Credentials table">
        <Thead>
          <Tr isHeaderRow>
            <Th columnKey="name" />
            <Th columnKey="description" />
            <Th columnKey="supplierIds" />
          </Tr>
        </Thead>
        <ConditionalTableBody
          isLoading={isFetching}
          isError={!!fetchError}
          isNoData={credentials.length === 0}
          numRenderedColumns={numRenderedColumns}
        >
          <Tbody>
            {currentPageItems?.map((item, rowIndex) => {
              return (
                <Tr key={item.name} item={item} rowIndex={rowIndex}>
                  <Td width={20} columnKey="name">
                    {item.name}
                  </Td>
                  <Td width={30} modifier="truncate" columnKey="description">
                    {item.description}
                  </Td>
                  <Td width={50} modifier="truncate" columnKey="supplierIds">
                    {item.supplier_ids_applied_to}
                  </Td>
                  <TdAction isActionCell>
                    <ActionsColumn
                      items={[
                        {
                          title: "Editar",
                          onClick: () => setCreateUpdateModalState(item),
                        },
                        {
                          title: "Eliminar",
                          onClick: () => deleteRow(item),
                        },
                      ]}
                    />
                  </TdAction>
                </Tr>
              );
            })}
          </Tbody>
        </ConditionalTableBody>
      </Table>
      <Pagination
        variant="bottom"
        isCompact
        widgetId="projects-pagination-bottom"
      />

      <Modal
        id="create-edit-credentials-modal"
        title={
          credentialsToUpdate ? "Actualizar credenciales" : "Nueva credencial"
        }
        variant={ModalVariant.medium}
        isOpen={isCreateUpdateModalOpen}
        onClose={closeCreateUpdateModal}
      >
        <CredentialsForm
          projectId={projectId}
          credentials={credentialsToUpdate ? credentialsToUpdate : undefined}
          onClose={closeCreateUpdateModal}
        />
      </Modal>
    </>
  );
};

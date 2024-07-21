import React, { useContext, useState } from "react";
import { NavLink } from "react-router-dom";
import { AxiosError } from "axios";

import {
  Button,
  ButtonVariant,
  Modal,
  ModalVariant,
  PageSection,
  PageSectionVariants,
  Text,
  TextContent,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import { ActionsColumn, Td as TdAction } from "@patternfly/react-table";
import {
  useClientTableBatteries,
  ConditionalTableBody,
  FilterType,
} from "@mturley-latest/react-table-batteries";

import {
  useFetchProjects,
  useDeleteProjectMutation,
} from "@app/queries/projects";

import { Project } from "@app/api/models";
import { ProjectForm } from "./components/project-form";
import { ConfirmDialog } from "@app/components/ConfirmDialog";
import { NotificationsContext } from "@app/components/NotificationsContext";
import { getAxiosErrorMessage } from "@app/utils/utils";

export const ProjectList: React.FC = () => {
  const { pushNotification } = useContext(NotificationsContext);

  const [isConfirmDialogOpen, setIsConfirmDialogOpen] =
    useState<boolean>(false);
  const [projectToDelete, setProjectToDelete] = React.useState<Project>();

  const [createUpdateModalState, setCreateUpdateModalState] = useState<
    "create" | Project | null
  >(null);
  const isCreateUpdateModalOpen = createUpdateModalState !== null;
  const projectToUpdate =
    createUpdateModalState !== "create" ? createUpdateModalState : null;

  const onDeleteOrgSuccess = () => {
    pushNotification({
      title: "Proyecto eliminado",
      variant: "success",
    });
  };

  const onDeleteOrgError = (error: AxiosError) => {
    pushNotification({
      title: getAxiosErrorMessage(error),
      variant: "danger",
    });
  };

  const { projects, isFetching, fetchError, refetch } = useFetchProjects();

  const { mutate: deleteproject } = useDeleteProjectMutation(
    onDeleteOrgSuccess,
    onDeleteOrgError
  );

  const tableControls = useClientTableBatteries({
    persistTo: "state",
    idProperty: "id",
    items: projects,
    isLoading: isFetching,
    columnNames: {
      name: "Nombre",
      description: "Descripción",
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

  const deleteRow = (row: Project) => {
    setProjectToDelete(row);
    setIsConfirmDialogOpen(true);
  };

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">Proyectos</Text>
        </TextContent>
      </PageSection>
      <PageSection>
        <div
          style={{
            backgroundColor: "var(--pf-v5-global--BackgroundColor--100)",
          }}
        >
          <Toolbar>
            <ToolbarContent>
              <FilterToolbar id="project-toolbar" />
              <ToolbarGroup variant="button-group">
                <ToolbarItem>
                  <Button
                    type="button"
                    id="create-project"
                    aria-label="Create new project"
                    variant={ButtonVariant.primary}
                    onClick={() => setCreateUpdateModalState("create")}
                  >
                    Crear Proyecto
                  </Button>
                </ToolbarItem>
              </ToolbarGroup>
              <PaginationToolbarItem>
                <Pagination
                  variant="top"
                  isCompact
                  widgetId="projects-pagination-top"
                />
              </PaginationToolbarItem>
            </ToolbarContent>
          </Toolbar>

          <Table aria-label="Projects table">
            <Thead>
              <Tr isHeaderRow>
                <Th columnKey="name" />
                <Th columnKey="description" />
              </Tr>
            </Thead>
            <ConditionalTableBody
              isLoading={isFetching}
              isError={!!fetchError}
              isNoData={projects.length === 0}
              numRenderedColumns={numRenderedColumns}
            >
              <Tbody>
                {currentPageItems?.map((item, rowIndex) => {
                  return (
                    <Tr key={item.name} item={item} rowIndex={rowIndex}>
                      <Td width={15} columnKey="name">
                        <NavLink to={`/projects/${item.id}`}>
                          {item.name}
                        </NavLink>
                      </Td>
                      <Td
                        width={20}
                        modifier="truncate"
                        columnKey="description"
                      >
                        {item.description}
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
        </div>
      </PageSection>

      <Modal
        id="create-edit-project-modal"
        title={projectToUpdate ? "Actualizar proyecto" : "Nuevo proyecto"}
        variant={ModalVariant.medium}
        isOpen={isCreateUpdateModalOpen}
        onClose={closeCreateUpdateModal}
      >
        <ProjectForm
          project={projectToUpdate ? projectToUpdate : undefined}
          onClose={closeCreateUpdateModal}
        />
      </Modal>

      {isConfirmDialogOpen && (
        <ConfirmDialog
          title="Eliminar proyecto"
          isOpen={true}
          titleIconVariant={"warning"}
          message="¿Estas seguro de querer eliminar el proyecto?"
          confirmBtnVariant={ButtonVariant.danger}
          confirmBtnLabel="Eliminar"
          cancelBtnLabel="Cancelar"
          onCancel={() => setIsConfirmDialogOpen(false)}
          onClose={() => setIsConfirmDialogOpen(false)}
          onConfirm={() => {
            if (projectToDelete) {
              deleteproject(projectToDelete.id);
              setProjectToDelete(undefined);
            }
            setIsConfirmDialogOpen(false);
          }}
        />
      )}
    </>
  );
};

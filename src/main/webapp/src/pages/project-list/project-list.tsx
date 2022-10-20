import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation, Trans } from "react-i18next";

import {
  useTableControls,
  SimpleTableWithToolbar,
  useTable,
  useModal,
  useConfirmationContext,
} from "@project-openubl/lib-ui";

import {
  Button,
  ButtonVariant,
  PageSection,
  PageSectionVariants,
  SearchInput,
  Text,
  TextContent,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import {
  cellWidth,
  IActions,
  ICell,
  IExtraData,
  IRow,
  IRowData,
  sortable,
} from "@patternfly/react-table";

import { useProjectsQuery, useDeleteProjectMutation } from "queries/projects";
import { AddProjectWizard } from "shared/components";
import { ProjectDto } from "api/models";

const ROW_FIELD = "row_field";
const getRow = (rowData: IRowData): ProjectDto => {
  return rowData[ROW_FIELD];
};

const itemsToRow = (items: ProjectDto[]) => {
  return items.map((item) => ({
    [ROW_FIELD]: item,
    cells: [
      {
        title: <Link to={`/projects/${item.id}`}>{item.name}</Link>,
      },
      {
        title: item.description,
      },
    ],
  }));
};

export const compareByColumnIndex = (
  a: ProjectDto,
  b: ProjectDto,
  columnIndex?: number
) => {
  switch (columnIndex) {
    case 0: // name
      return a.name.localeCompare(b.name);
    default:
      return 0;
  }
};

export const filterByText = (filterText: string, item: ProjectDto) => {
  return (
    item.name.toString().toLowerCase().indexOf(filterText.toLowerCase()) !== -1
  );
};

export const ProjectList: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const confirmationModal = useConfirmationContext();
  const modal = useModal<"ADD", ProjectDto>();

  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls({ sortBy: { index: 0, direction: "asc" } });

  //
  const projectsQuery = useProjectsQuery();
  const deleteProjectMutation = useDeleteProjectMutation(() => {
    confirmationModal.close();
  });

  const { pageItems, filteredItems } = useTable<ProjectDto>({
    items: projectsQuery.data || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => filterByText(filterText, item),
  });

  const columns: ICell[] = [
    { title: t("terms.name"), transforms: [sortable, cellWidth(40)] },
    { title: t("terms.description"), transforms: [cellWidth(60)] },
  ];

  const rows: IRow[] = itemsToRow(pageItems || []);

  const actions: IActions = [
    {
      title: t("actions.edit"),
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData,
        extraData: IExtraData
      ) => {
        const row: ProjectDto = getRow(rowData);
        navigate(`/projects/${row.id}`);
      },
    },
    {
      title: t("actions.delete"),
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData,
        extraData: IExtraData
      ) => {
        const row: ProjectDto = getRow(rowData);

        confirmationModal.open({
          title: t("modal.confirm-delete.title", {
            what: t("terms.project").toLowerCase(),
          }),
          titleIconVariant: "warning",
          message: (
            <Trans
              i18nKey="modal.confirm-delete.body"
              values={{ type: t("terms.project"), name: row.name }}
            >
              ¿Estas seguro de querer eliminar este(a) <b>type</b>? Esta acción
              eliminará <b>name</b> permanentemente.
            </Trans>
          ),
          confirmBtnVariant: ButtonVariant.danger,
          confirmBtnLabel: t("actions.delete"),
          cancelBtnLabel: t("actions.cancel"),
          onConfirm: () => {
            confirmationModal.enableProcessing();
            deleteProjectMutation.mutate(row);
          },
        });
      },
    },
  ];

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">{t("terms.projects")}</Text>
          <Text component="p">Lista de Proyectos disponibles.</Text>
        </TextContent>
      </PageSection>
      <PageSection variant="light" type="wizard">
        <SimpleTableWithToolbar
          hasTopPagination
          hasBottomPagination
          totalCount={filteredItems.length}
          // Sorting
          sortBy={currentSortBy}
          onSort={onChangeSortBy}
          // Pagination
          currentPage={currentPage}
          onPageChange={onPageChange}
          // Table
          rows={rows}
          cells={columns}
          actions={actions}
          // Fech data
          isLoading={projectsQuery.isLoading}
          loadingVariant="skeleton"
          fetchError={projectsQuery.isError}
          // Toolbar filters
          filtersApplied={filterText.trim().length > 0}
          toolbarToggle={
            <ToolbarItem variant="search-filter">
              <SearchInput value={filterText} onChange={setFilterText} />
            </ToolbarItem>
          }
          toolbarActions={
            <ToolbarGroup variant="button-group">
              <ToolbarItem>
                <Button
                  type="button"
                  aria-label="new-project"
                  variant={ButtonVariant.primary}
                  onClick={() => modal.open("ADD")}
                >
                  {t("actions.create-object", {
                    what: t("terms.project").toLowerCase(),
                  })}
                </Button>
              </ToolbarItem>
            </ToolbarGroup>
          }
        />
      </PageSection>

      {modal.isOpen && modal.isAction("ADD") && (
        <AddProjectWizard
          onSave={(project) => {
            modal.close();
            navigate(`/projects/${project.id}`);
          }}
          onClose={modal.close}
        />
      )}
    </>
  );
};

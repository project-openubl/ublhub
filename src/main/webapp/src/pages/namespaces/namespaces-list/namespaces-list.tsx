import React, { useEffect, useState } from "react";

import {
  useTableControls,
  SimpleTableWithToolbar,
  useTable,
  useModal,
} from "@project-openubl/lib-ui";

import {
  Button,
  ButtonVariant,
  PageSection,
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

import { useNamespacesQuery } from "queries/namespaces";

import {
  SearchInput,
  SimplePageSection,
  AddNamespaceWizard,
} from "shared/components";

import { Namespace } from "api/models";

const ROW_FIELD = "row_field";
const getRow = (rowData: IRowData): Namespace => {
  return rowData[ROW_FIELD];
};

const itemsToRow = (items: Namespace[]) => {
  return items.map((item) => ({
    [ROW_FIELD]: item,
    cells: [
      {
        title: item.name,
      },
      {
        title: item.description,
      },
    ],
  }));
};

export const compareByColumnIndex = (
  a: Namespace,
  b: Namespace,
  columnIndex?: number
) => {
  switch (columnIndex) {
    case 0: // name
      return a.name.localeCompare(b.name);
    default:
      return 0;
  }
};

export const filterByText = (filterText: string, item: Namespace) => {
  return (
    item.name.toString().toLowerCase().indexOf(filterText.toLowerCase()) !== -1
  );
};

export const NamespacesList: React.FC = () => {
  const namespaceModal = useModal<Namespace>();

  //
  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls({ sortBy: { index: 0, direction: "asc" } });

  //
  const namespaces = useNamespacesQuery();

  useEffect(() => {
    console.log(namespaces);
  }, [namespaces]);

  const { pageItems, filteredItems } = useTable<Namespace>({
    items: namespaces.data || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => filterByText(filterText, item),
  });

  //
  const columns: ICell[] = [
    { title: "Nombre", transforms: [sortable, cellWidth(40)] },
    { title: "descripcion", transforms: [cellWidth(60)] },
  ];

  const rows: IRow[] = itemsToRow(pageItems || []);

  const actions: IActions = [
    {
      title: "Eliminar",
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData,
        extraData: IExtraData
      ) => {
        const row: Namespace = getRow(rowData);
        console.log("Write code to delete the row", row);
      },
    },
  ];

  //

  return (
    <>
      <SimplePageSection
        title="Namespaces"
        description="Crea multiples empresas dentro de un namespace."
      />
      <PageSection>
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
          isLoading={namespaces.isLoading}
          loadingVariant="skeleton"
          fetchError={namespaces.isError}
          // Toolbar filters
          filtersApplied={filterText.trim().length > 0}
          toolbarToggle={
            <ToolbarGroup variant="filter-group">
              <ToolbarItem>
                <SearchInput onSearch={setFilterText} />
              </ToolbarItem>
            </ToolbarGroup>
          }
          toolbarActions={
            <ToolbarGroup variant="button-group">
              <ToolbarItem>
                <Button
                  type="button"
                  aria-label="new-namespace"
                  variant={ButtonVariant.primary}
                  onClick={() => namespaceModal.open("add")}
                >
                  Nuevo Namespace
                </Button>
              </ToolbarItem>
            </ToolbarGroup>
          }
        />
      </PageSection>

      {namespaceModal.isOpen && namespaceModal.actionKey === "add" && (
        <AddNamespaceWizard onClose={namespaceModal.close} />
      )}
    </>
  );
};

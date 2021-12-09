import React, { useEffect, useState } from "react";

import {
  useTableControls,
  SimpleTableWithToolbar,
} from "@project-openubl/lib-ui";

import { PageSection, ToolbarGroup, ToolbarItem } from "@patternfly/react-core";
import {
  cellWidth,
  IActions,
  ICell,
  IExtraData,
  IRow,
  IRowData,
  sortable,
} from "@patternfly/react-table";

// import { useDispatch } from "react-redux";

import {
  INamespaceParams,
  INamespaceParamsBuilder,
  NamespaceSortByKey,
  useNamespacesQuery,
} from "queries/namespaces";

import { SearchInput, SimplePageSection } from "shared/components";

import { Namespace, SortByQuery } from "api/models";

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

const toSortByQuery = (sortBy?: {
  index: number;
  direction: "asc" | "desc";
}): SortByQuery | undefined => {
  if (!sortBy) {
    return undefined;
  }

  let field: string;
  switch (sortBy.index) {
    case 0:
      field = NamespaceSortByKey.name;
      break;
    default:
      return undefined;
  }

  return {
    orderBy: field,
    orderDirection: sortBy.direction,
  };
};

export const NamespacesList: React.FC = () => {
  // const dispatch = useDispatch();

  //
  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  //
  const [queryParams, setQueryParams] = useState<INamespaceParams>(
    new INamespaceParamsBuilder()
      .withFilterText(filterText)
      .withPagination(currentPage)
      .withSorting(toSortByQuery(currentSortBy))
      .build()
  );
  const namespaces = useNamespacesQuery(queryParams);

  //
  useEffect(() => {
    const params = new INamespaceParamsBuilder()
      .withFilterText(filterText)
      .withPagination(currentPage)
      .withSorting(toSortByQuery(currentSortBy))
      .build();
    setQueryParams(params);
  }, [filterText, currentPage, currentSortBy]);

  //
  const columns: ICell[] = [
    { title: "Nombre", transforms: [sortable, cellWidth(40)] },
    { title: "descripcion", transforms: [cellWidth(60)] },
  ];

  const rows: IRow[] = itemsToRow(namespaces.data?.data || []);

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
          totalCount={namespaces.data?.meta.count || 0}
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
          // toolbarActions={
          //   <ToolbarGroup variant="button-group">
          //     <ToolbarItem>
          //       <Button
          //         type="button"
          //         aria-label="new-namespace"
          //         variant={ButtonVariant.primary}
          //         // onClick={onNewVersion}
          //       >
          //         Nuevo Namespace
          //       </Button>
          //     </ToolbarItem>
          //   </ToolbarGroup>
          // }
        />
      </PageSection>
    </>
  );
};

import React, { useState } from "react";
import Moment from "react-moment";

import {
  useTable,
  useTableControls,
  SimpleTableWithToolbar,
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
  IAction,
  ICell,
  IExtraData,
  IRow,
  IRowData,
  ISeparator,
  sortable,
} from "@patternfly/react-table";

import {
  useCreateVersionMutation,
  useDeleteVersionMutation,
  useVersionsQuery,
} from "queries/versions";
import {
  SearchInput,
  SimplePageSection,
  VersionStatusIcon,
} from "shared/components";

import { useDispatch } from "react-redux";
import { deleteDialogActions } from "store/deleteDialog";
import { alertActions } from "store/alert";

import { Version } from "api/models";
import { formatNumber, getAxiosErrorMessage } from "utils/modelUtils";

const columns: ICell[] = [
  { title: "Id", transforms: [sortable, cellWidth(10)] },
  { title: "Creado", transforms: [sortable, cellWidth(30)] },
  { title: "Records", transforms: [cellWidth(30)] },
  { title: "Estado", transforms: [cellWidth(30)] },
];

const VERSION_FIELD = "version";

const itemsToRow = (items: Version[]) => {
  return items.map((item) => ({
    [VERSION_FIELD]: item,
    cells: [
      {
        title: `#${item.id}`,
      },
      {
        title: <Moment fromNow>{item.createdAt}</Moment>,
      },
      {
        title: formatNumber(item.records, 0),
      },
      {
        title: <VersionStatusIcon value={item.status} />,
      },
    ],
  }));
};

const getRow = (rowData: IRowData): Version => {
  return rowData[VERSION_FIELD];
};

export const compareByColumnIndex = (
  a: Version,
  b: Version,
  columnIndex?: number
) => {
  switch (columnIndex) {
    case 0: // id
      return a.id - b.id;
    case 1: // createdAt
      return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
    default:
      return 0;
  }
};

export const filterByText = (filterText: string, item: Version) => {
  return (
    item.id.toString().toLowerCase().indexOf(filterText.toLowerCase()) !== -1
  );
};

export const VersionList: React.FC = () => {
  const dispatch = useDispatch();

  const versions = useVersionsQuery();
  const versionMutation = useCreateVersionMutation();
  const deleleteVersionMutation = useDeleteVersionMutation();

  const [filterText, setFilterText] = useState("");

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<Version>({
    items: versions.data || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => filterByText(filterText, item),
  });

  const actionResolver = (rowData: IRowData): (IAction | ISeparator)[] => {
    const row: Version = getRow(rowData);

    const actions: (IAction | ISeparator)[] = [];

    if (row.status !== "COMPLETED" && row.status !== "ERROR") {
      // actions.push({
      //   title: "Cancel",
      //   onClick: (_, rowIndex: number, rowData: IRowData) => {
      //   },
      // });
    } else {
      actions.push({
        title: "Delete",
        onClick: (
          event: React.MouseEvent,
          rowIndex: number,
          rowData: IRowData,
          extraData: IExtraData
        ) => {
          const row: Version = getRow(rowData);
          dispatch(
            deleteDialogActions.openModal({
              name: `version #${row.id}`,
              type: "version",
              onDelete: () => {
                dispatch(deleteDialogActions.processing());
                deleleteVersionMutation
                  .mutateAsync(row)
                  .catch((error) => {
                    dispatch(
                      alertActions.addAlert(
                        "danger",
                        "Error",
                        getAxiosErrorMessage(error)
                      )
                    );
                  })
                  .finally(() => {
                    dispatch(deleteDialogActions.closeModal());
                  });
              },
            })
          );
        },
      });
    }

    return actions;
  };

  const areActionsDisabled = (): boolean => {
    return false;
  };

  const rows: IRow[] = itemsToRow(pageItems || []);

  const onNewVersion = () => {
    versionMutation.mutateAsync().catch((error) => {
      dispatch(
        alertActions.addAlert("danger", "Error", getAxiosErrorMessage(error))
      );
    });
  };

  return (
    <>
      <SimplePageSection
        title="Versiones"
        description="Las Versiones representan una versión específica del 'padrón reducido' de la SUNAT."
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
          actionResolver={actionResolver}
          areActionsDisabled={areActionsDisabled}
          // Fech data
          isLoading={versions.isFetching}
          loadingVariant="none"
          fetchError={versions.isError}
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
                  aria-label="new-version"
                  variant={ButtonVariant.primary}
                  onClick={onNewVersion}
                >
                  Nueva versión
                </Button>
              </ToolbarItem>
            </ToolbarGroup>
          }
        />
      </PageSection>
    </>
  );
};

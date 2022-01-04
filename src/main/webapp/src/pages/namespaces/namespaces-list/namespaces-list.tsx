import React, { useState } from "react";
import { Link, useHistory } from "react-router-dom";
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

import { useDispatch } from "react-redux";
import { alertActions } from "store/alert";

import {
  useNamespacesQuery,
  useDeleteNamespaceMutation,
} from "queries/namespaces";

import {
  SearchInput,
  SimplePageSection,
  AddNamespaceWizard,
} from "shared/components";

import { Namespace } from "api/models";
import { getAxiosErrorMessage } from "utils/modelUtils";
import { formatPath, Paths } from "Paths";

const ROW_FIELD = "row_field";
const getRow = (rowData: IRowData): Namespace => {
  return rowData[ROW_FIELD];
};

const itemsToRow = (items: Namespace[]) => {
  return items.map((item) => ({
    [ROW_FIELD]: item,
    cells: [
      {
        title: (
          <Link
            to={formatPath(Paths.namespaces_edit, { namespaceId: item.id })}
          >
            {item.name}
          </Link>
        ),
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
  const { t } = useTranslation();

  const history = useHistory();

  const dispatch = useDispatch();
  const confirmationModal = useConfirmationContext();

  //
  enum ModalAction {
    ADD,
  }
  const modal = useModal<ModalAction, Namespace>();

  //
  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls({ sortBy: { index: 0, direction: "asc" } });

  //
  const namespacesQuery = useNamespacesQuery();
  const deleteNamespaceMutation = useDeleteNamespaceMutation();

  const { pageItems, filteredItems } = useTable<Namespace>({
    items: namespacesQuery.data || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => filterByText(filterText, item),
  });

  //
  const columns: ICell[] = [
    { title: t("terms.name"), transforms: [sortable, cellWidth(40)] },
    { title: t("terms.description"), transforms: [cellWidth(60)] },
  ];

  const rows: IRow[] = itemsToRow(pageItems || []);

  const actions: IActions = [
    {
      title: t("actions.delete"),
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData,
        extraData: IExtraData
      ) => {
        const row: Namespace = getRow(rowData);

        confirmationModal.open({
          title: t("modal.confirm-delete.title", {
            what: "namespace",
          }),
          titleIconVariant: "warning",
          message: (
            <Trans
              i18nKey="modal.confirm-delete.body"
              values={{ type: "namespace", name: row.name }}
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
            deleteNamespaceMutation
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
                confirmationModal.close();
              });
          },
        });
      },
    },
  ];

  //

  return (
    <>
      <SimplePageSection title="Namespaces" />
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
          isLoading={namespacesQuery.isLoading}
          loadingVariant="skeleton"
          fetchError={namespacesQuery.isError}
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
                  variant={ButtonVariant.secondary}
                  onClick={() => modal.open(ModalAction.ADD)}
                >
                  {t("actions.create-object", { what: "namespace" })}
                </Button>
              </ToolbarItem>
            </ToolbarGroup>
          }
        />
      </PageSection>

      {modal.isOpen && modal.isAction(ModalAction.ADD) && (
        <AddNamespaceWizard
          onSave={(ns) => {
            modal.close();
            history.push(
              formatPath(Paths.namespaces_edit, { namespaceId: ns.id })
            );
          }}
          onClose={modal.close}
        />
      )}
    </>
  );
};

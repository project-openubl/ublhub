import React, { useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";

import { ResolvedQueries } from "@konveyor/lib-ui";
import {
  SimpleTableWithToolbar,
  useConfirmationContext,
  useModal,
  useTable,
  useTableControls,
} from "@project-openubl/lib-ui";

import {
  Button,
  ButtonVariant,
  Card,
  CardBody,
  Label,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import {
  cellWidth,
  expandable,
  IActions,
  ICell,
  IExtraData,
  IRow,
  IRowData,
  sortable,
  truncate,
} from "@patternfly/react-table";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { useDispatch } from "react-redux";
import { alertActions } from "store/alert";

import { SearchInput } from "shared/components";
import {
  useKeysQuery,
  useComponentsQuery,
  useDeleteComponentMutation,
} from "queries/components";
import { LONG_LOADING_MESSAGE } from "queries/constants";
import { getAggregateQueryStatus } from "queries/helpers";

import { ComponentRepresentation, KeyMetadataRepresentation } from "api/models";
import { getAxiosErrorMessage } from "utils/modelUtils";

import { INamespaceParams } from "../edit-namespace";
import { AddEditComponentModal } from "./add-edit-component-modal";

const ROW_FIELD = "row_field";
const getRow = (rowData: IRowData): ComponentRepresentation => {
  return rowData[ROW_FIELD];
};

const itemsToRow = (
  items: ComponentRepresentation[],
  keyMetadataMap: Map<string, KeyMetadataRepresentation>
) => {
  let statusNode;

  return items.map((item) => {
    const status = keyMetadataMap?.get(item.id!)?.status;
    if (status === "ACTIVE") {
      statusNode = (
        <Label color="green" isCompact>
          Active
        </Label>
      );
    } else if (status === "PASSIVE") {
      statusNode = (
        <Label color="cyan" isCompact>
          Passive
        </Label>
      );
    } else {
      statusNode = (
        <Label color="grey" isCompact>
          Disabled
        </Label>
      );
    }

    return {
      [ROW_FIELD]: item,
      cells: [
        {
          title: item.name,
        },
        {
          title: item.providerId,
        },
        {
          title: keyMetadataMap?.get(item.id!)?.algorithm,
        },
        {
          title: keyMetadataMap?.get(item.id!)?.type,
        },
        {
          title: keyMetadataMap?.get(item.id!)?.kid,
        },
        {
          title: keyMetadataMap?.get(item.id!)?.providerPriority,
        },
        {
          title: statusNode,
        },
      ],
    };
  });
};

export const compareByColumnIndex = (
  a: ComponentRepresentation,
  b: ComponentRepresentation,
  columnIndex?: number
) => {
  switch (columnIndex) {
    case 0: // name
      return a.name.localeCompare(b.name);
    default:
      return 0;
  }
};

export const filterByText = (
  filterText: string,
  item: ComponentRepresentation
) => {
  return (
    item.name.toString().toLowerCase().indexOf(filterText.toLowerCase()) !== -1
  );
};

export const Keys: React.FC = () => {
  const { t } = useTranslation();

  const dispatch = useDispatch();
  const confirmationModal = useConfirmationContext();

  const routeParams = useParams<INamespaceParams>();
  const prefillNamespaceId = routeParams.namespaceId;

  //
  const keyModal = useModal<ComponentRepresentation>();

  //
  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls({ sortBy: { index: 0, direction: "asc" } });

  //
  const keysQuery = useKeysQuery(prefillNamespaceId);
  const componentsQuery = useComponentsQuery(prefillNamespaceId);
  const deleteComponentMutation =
    useDeleteComponentMutation(prefillNamespaceId);

  const { pageItems, filteredItems } = useTable<ComponentRepresentation>({
    items: componentsQuery.data || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => filterByText(filterText, item),
  });

  //
  const columns: ICell[] = [
    {
      title: t("terms.name"),
      transforms: [cellWidth(20), sortable],
      cellTransforms: [expandable, truncate],
    },
    {
      title: t("terms.provider"),
      transforms: [cellWidth(10)],
      cellTransforms: [expandable, truncate],
    },
    { title: t("terms.algorithm"), transforms: [cellWidth(10)] },
    { title: t("terms.type"), transforms: [cellWidth(10)] },
    {
      title: t("terms.kid"),
      transforms: [cellWidth(40)],
      cellTransforms: [truncate],
    },
    { title: t("terms.priority"), transforms: [cellWidth(10)] },
    { title: t("terms.status"), transforms: [cellWidth(10)] },
  ];

  const queryStatus = getAggregateQueryStatus([keysQuery, componentsQuery]);
  const rows: IRow[] = useMemo(() => {
    const keysMedatadata: Map<string, KeyMetadataRepresentation> = new Map();
    keysQuery.data?.keys.forEach((e) => keysMedatadata.set(e.providerId, e));

    if (queryStatus === "success") {
      return itemsToRow(pageItems || [], keysMedatadata);
    } else {
      return itemsToRow(pageItems || [], keysMedatadata);
    }
  }, [queryStatus, keysQuery, pageItems]);

  const actions: IActions = [
    {
      title: t("actions.edit"),
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData,
        extraData: IExtraData
      ) => {
        const row: ComponentRepresentation = getRow(rowData);
        keyModal.open("add-edit", row);
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
        const row: ComponentRepresentation = getRow(rowData);

        confirmationModal.open({
          title: t("modal.confirm-delete.title", {
            what: "key",
          }),
          titleIconVariant: "warning",
          message: (
            <Trans
              i18nKey="modal.confirm-delete.body"
              values={{ type: "key", name: row.name }}
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
            deleteComponentMutation
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

  return (
    <Card>
      <CardBody>
        <ResolvedQueries
          resultsWithErrorTitles={[
            { result: keysQuery, errorTitle: "Cannot load keys" },
            { result: componentsQuery, errorTitle: "Cannot load components" },
          ]}
          errorsInline={false}
          className={spacing.mMd}
          emptyStateBody={LONG_LOADING_MESSAGE}
        >
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
            isLoading={componentsQuery.isLoading}
            loadingVariant="skeleton"
            fetchError={componentsQuery.isError}
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
                    onClick={() => keyModal.open("add-edit")}
                  >
                    {t("actions.create-object", { what: "key" })}
                  </Button>
                </ToolbarItem>
              </ToolbarGroup>
            }
          />
        </ResolvedQueries>

        {keyModal.isOpen && keyModal.actionKey === "add-edit" && (
          <AddEditComponentModal
            namespaceId={prefillNamespaceId}
            componentBeingEdited={keyModal.data}
            onClose={keyModal.close}
          />
        )}
      </CardBody>
    </Card>
  );
};

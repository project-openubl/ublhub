import React, { useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { useTranslation } from "react-i18next";

import {
  Button,
  ButtonVariant,
  Modal,
  SearchInput,
  Split,
  SplitItem,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import { cellWidth, IActions, ICell, IExtraData, IRow, IRowData, sortable } from "@patternfly/react-table";

import {
  NotificationContext, SimpleTableWithToolbar, useConfirmationContext, useModal, useTable, useTableControls
} from "@project-openubl/lib-ui";

import { KeyMetadataDto, ProjectDto } from "api/models";
import { useKeysQuery } from "queries/keys";

import { ProviderCell } from "./provider-cell";

const ROW_FIELD = "row_field";
const getRow = (rowData: IRowData): KeyMetadataDto => {
  return rowData[ROW_FIELD];
};

export const compareByColumnIndex = (
  a: KeyMetadataDto,
  b: KeyMetadataDto,
  columnIndex?: number
) => {
  switch (columnIndex) {
    default:
      return 0;
  }
};

export const filterByText = (filterText: string, item: KeyMetadataDto) => {
  return (
    item.algorithm.toString().toLowerCase().indexOf(filterText.toLowerCase()) !== -1
  );
};

const Certificates: React.FC = () => {
  const { t } = useTranslation();

  const navigate = useNavigate();
  const { pushNotification } = React.useContext(NotificationContext);

  const project = useOutletContext<ProjectDto | null>();
  const keysQuery = useKeysQuery(project?.id || null, null);

  const confirmationModal = useConfirmationContext();
  const keyModal = useModal<"PUBLIC-KEY" | " CERTIFICATE", string>();

  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls({ sortBy: { index: 0, direction: "asc" } });

  const { pageItems, filteredItems } = useTable<KeyMetadataDto>({
    items: keysQuery.data?.keys || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => filterByText(filterText, item),
  });

  const columns: ICell[] = [
    { title: t("terms.algorithm"), transforms: [sortable, cellWidth(10)] },
    { title: t("terms.type"), transforms: [cellWidth(10)] },
    { title: t("terms.kid"), transforms: [cellWidth(40)] },
    { title: t("terms.provider"), transforms: [cellWidth(20)] },
    { title: t("terms.public-keys"), transforms: [cellWidth(20)] },
  ];

  const itemsToRow = (items: KeyMetadataDto[]) => {
    return items.map((item) => ({
      [ROW_FIELD]: item,
      cells: [
        {
          title: item.algorithm,
        },
        {
          title: item.type,
        },
        {
          title: item.kid,
        },
        {
          title: (
            <>
              {project && project.id && (
                <ProviderCell
                  projectId={project.id}
                  companyId={null}
                  keyId={item.providerId}
                />
              )}
            </>
          ),
        },
        {
          title: (
            <Split hasGutter>
              {item.publicKey && (
                <SplitItem>
                  <Button
                    variant="secondary"
                    onClick={() => keyModal.open("PUBLIC-KEY", item.publicKey)}
                  >
                    {t("terms.public-key")}
                  </Button>
                </SplitItem>
              )}
              {item.certificate && (
                <SplitItem>
                  <Button
                    variant="secondary"
                    onClick={() =>
                      keyModal.open(" CERTIFICATE", item.certificate)
                    }
                  >
                    {t("terms.certificate")}
                  </Button>
                </SplitItem>
              )}
            </Split>
          ),
        },
      ],
    }));
  };

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
        const row: KeyMetadataDto = getRow(rowData);
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
        const row: KeyMetadataDto = getRow(rowData);
      },
    },
  ];

  return (
    <>
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
        isLoading={keysQuery.isLoading}
        loadingVariant="skeleton"
        fetchError={keysQuery.isError}
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
              >
                New
              </Button>
            </ToolbarItem>
          </ToolbarGroup>
        }
      />

      <Modal
        variant="small"
        title={
          keyModal.action === "PUBLIC-KEY"
            ? t("terms.public-key")
            : t("terms.certificate")
        }
        isOpen={keyModal.isOpen}
        onClose={keyModal.close}
        actions={[
          <Button key="close" variant="primary" onClick={keyModal.close}>
            {t("actions.close")}
          </Button>,
          <Button key="cancel" variant="link" onClick={keyModal.close}>
            {t("actions.cancel")}
          </Button>,
        ]}
      >
        {keyModal.data}
      </Modal>
    </>
  );
};

export default Certificates;

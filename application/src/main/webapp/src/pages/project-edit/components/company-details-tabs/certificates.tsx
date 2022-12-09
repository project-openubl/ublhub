import React, { useReducer, useState } from "react";
import { Trans, useTranslation } from "react-i18next";

import {
  Button,
  ButtonVariant,
  Dropdown,
  DropdownItem,
  DropdownToggle,
  Modal,
  SearchInput,
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
  truncate,
} from "@patternfly/react-table";

import {
  SimpleTableWithToolbar,
  useConfirmationContext,
  useModal,
  useTable,
  useTableControls,
} from "@project-openubl/lib-ui";

import {
  CompanyDto,
  ComponentDto,
  ComponentTypeDto,
  KeyMetadataDto,
  ProjectDto,
} from "api/models";

import {
  useComponentsQuery,
  useDeleteComponentMutation,
  useKeysQuery,
} from "queries/keys";
import { useServerInfoQuery } from "queries/server-info";

import { KEY_PROVIDERS } from "Constants";

import { ComponentForm } from "../component-form";

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
    item.algorithm
      .toString()
      .toLowerCase()
      .indexOf(filterText.toLowerCase()) !== -1
  );
};

interface ICertificatesProps {
  project: ProjectDto;
  company: CompanyDto;
}

export const Certificates: React.FC<ICertificatesProps> = ({
  project,
  company,
}) => {
  const { t } = useTranslation();
  const confirmationModal = useConfirmationContext();

  const keysQuery = useKeysQuery(project?.id || null, company.id || null);
  const serverInfoQuery = useServerInfoQuery();
  const componentskeysQuery = useComponentsQuery(
    project?.id || null,
    company.id || null
  );
  const deleteComponentMutation = useDeleteComponentMutation(
    project?.id || null,
    company.id || null,
    () => {
      confirmationModal.close();
    }
  );

  const viewKeyModal = useModal<"PUBLIC-KEY" | " CERTIFICATE", string>();
  const componentFormModal = useModal<
    "create" | "edit",
    { componentType: ComponentTypeDto; component?: ComponentDto }
  >();

  const [isNewKeyBtnOpen, toggleIsNewKeyBtnOpen] = useReducer(
    (state) => !state,
    false
  );

  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls({
    sortBy: { index: 0, direction: "asc" },
    page: { page: 1, perPage: 5 },
  });

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
    {
      title: t("terms.kid"),
      transforms: [cellWidth(40)],
      cellTransforms: [truncate],
    },
    { title: t("terms.provider"), transforms: [cellWidth(20)] },
    { title: t("terms.public-key"), transforms: [cellWidth(10)] },
    { title: t("terms.certificate"), transforms: [cellWidth(10)] },
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
          title: componentskeysQuery.data?.find((e) => e.id === item.providerId)
            ?.name,
        },
        {
          title: (
            <Button
              variant="secondary"
              onClick={() => viewKeyModal.open("PUBLIC-KEY", item.publicKey)}
            >
              {t("terms.public-key")}
            </Button>
          ),
        },
        {
          title: (
            <Button
              variant="secondary"
              onClick={() =>
                viewKeyModal.open(" CERTIFICATE", item.certificate)
              }
            >
              {t("terms.certificate")}
            </Button>
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

        const component = componentskeysQuery.data?.find(
          (e) => e.id === row.providerId
        );
        const componentType = serverInfoQuery.data?.componentTypes[
          KEY_PROVIDERS
        ].find((e) => e.id === component?.providerId);

        if (component && componentType) {
          componentFormModal.open("edit", {
            componentType,
            component,
          });
        }
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

        confirmationModal.open({
          title: t("modal.confirm-delete.title", {
            what: t("terms.certificate"),
          }),
          titleIconVariant: "warning",
          message: (
            <Trans
              i18nKey="modal.confirm-delete.body"
              values={{ type: t("terms.certificate"), name: row.kid }}
            >
              ¿Estas seguro de querer eliminar este(a) <b>type</b>? Esta acción
              eliminará <b>name</b> permanentemente.
            </Trans>
          ),
          confirmBtnVariant: ButtonVariant.danger,
          confirmBtnLabel: t("actions.delete"),
          cancelBtnLabel: t("actions.cancel"),
          onConfirm: () => {
            const component = componentskeysQuery.data?.find(
              (e) => e.id === row.providerId
            );
            if (component) {
              confirmationModal.enableProcessing();
              deleteComponentMutation.mutate(component);
            }
          },
        });
      },
    },
  ];

  return (
    <>
      <SimpleTableWithToolbar
        variant="compact"
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
              <Dropdown
                onSelect={toggleIsNewKeyBtnOpen}
                toggle={
                  <DropdownToggle
                    id="toggle-basic"
                    onToggle={toggleIsNewKeyBtnOpen}
                    toggleVariant="primary"
                  >
                    {t("actions.create-object", {
                      what: t("terms.certificate"),
                    })}
                  </DropdownToggle>
                }
                isOpen={isNewKeyBtnOpen}
                dropdownItems={serverInfoQuery.data?.componentTypes[
                  KEY_PROVIDERS
                ].map((e) => (
                  <DropdownItem
                    key={e.id}
                    component="button"
                    onClick={() => {
                      componentFormModal.open("create", {
                        componentType: e,
                      });
                    }}
                  >
                    {e.id}
                  </DropdownItem>
                ))}
              />
            </ToolbarItem>
          </ToolbarGroup>
        }
      />

      <Modal
        variant="small"
        title={
          viewKeyModal.action === "PUBLIC-KEY"
            ? t("terms.public-key")
            : t("terms.certificate")
        }
        isOpen={viewKeyModal.isOpen}
        onClose={viewKeyModal.close}
        actions={[
          <Button key="close" variant="primary" onClick={viewKeyModal.close}>
            {t("actions.close")}
          </Button>,
          <Button key="cancel" variant="link" onClick={viewKeyModal.close}>
            {t("actions.cancel")}
          </Button>,
        ]}
      >
        {viewKeyModal.data}
      </Modal>

      <Modal
        variant="medium"
        title={
          componentFormModal.action === "create"
            ? t("actions.create-object", {
                what: t("terms.certificate").toLowerCase(),
              })
            : t("actions.edit-object", {
                what: t("terms.certificate").toLowerCase(),
              })
        }
        isOpen={componentFormModal.isOpen}
        onClose={componentFormModal.close}
      >
        {project && project.id && componentFormModal.data && (
          <ComponentForm
            projectId={project.id}
            companyId={company.id}
            componentType={componentFormModal.data.componentType}
            component={componentFormModal.data.component}
            onSaved={componentFormModal.close}
            onCancel={componentFormModal.close}
          />
        )}
      </Modal>
    </>
  );
};

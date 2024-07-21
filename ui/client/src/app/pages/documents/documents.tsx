import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import {
  Button,
  ButtonVariant,
  PageSection,
  PageSectionVariants,
  Text,
  TextContent,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import { ActionsColumn, Td as TdAction } from "@patternfly/react-table";

import { getHubRequestParams } from "@app/hooks/table-controls";

import { useFetchUblDocuments } from "@app/queries/ubl-documents";
import {
  ConditionalTableBody,
  FilterType,
  useTablePropHelpers,
  useTableState,
} from "@mturley-latest/react-table-batteries";
import { UploadFilesDrawer } from "./upload-files-drawer";

export const Projects: React.FC = () => {
  const { t } = useTranslation();
  const { projectId } = useParams();

  const [uploadFilesToProjectId, setUploadFilesToProjectId] = useState<
    string | number | null
  >(null);

  const tableState = useTableState({
    persistTo: "urlParams",
    persistenceKeyPrefix: "d",
    columnNames: {
      supplierId: "Supplier ID",
      documentType: "Type",
      documentId: "ID",
    },
    filter: {
      isEnabled: true,
      filterCategories: [
        {
          key: "type",
          title: "Type",
          type: FilterType.multiselect,
          selectOptions: [
            { key: "Invoice", value: "Invoice" },
            { key: "CreditNote", value: "Credit note" },
            { key: "DebitNote", value: "Debit note" },
          ]
        },
        {
          key: "supplier_id",
          title: "Supplier",
          type: FilterType.multiselect,
          selectOptions: [
            { key: "Invoice", value: "Invoice" },
            { key: "CreditNote", value: "Credit note" },
            { key: "DebitNote", value: "Debit note" },
          ]
        },
        {
          key: "documentId",
          title: "ID",
          type: FilterType.search,
          placeholderText: "Search by ID...",
        },
      ],
    },
    sort: {
      isEnabled: true,
      sortableColumns: [],
    },
    pagination: { isEnabled: true },
  });

  const { filter, cacheKey } = tableState;
  const hubRequestParams = React.useMemo(() => {
    return getHubRequestParams({
      ...tableState,
      filterCategories: filter.filterCategories,
    });
  }, [cacheKey]);

  const { isFetching, result, fetchError } = useFetchUblDocuments(
    projectId,
    hubRequestParams
  );

  const tableProps = useTablePropHelpers({
    ...tableState,
    idProperty: "id",
    isLoading: isFetching,
    currentPageItems: result.data,
    totalItemCount: result.total,
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
  } = tableProps;

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">{t("terms.documents")}</Text>
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
              <FilterToolbar id="document-toolbar" {...{ showFiltersSideBySide: true }} />
              <ToolbarGroup variant="button-group">
                <ToolbarItem>
                  <Button
                    type="button"
                    id="upload-files"
                    aria-label="Upload files"
                    variant={ButtonVariant.secondary}
                    onClick={() => setUploadFilesToProjectId(projectId || null)}
                  >
                    Upload files
                  </Button>
                </ToolbarItem>
              </ToolbarGroup>
              <PaginationToolbarItem>
                <Pagination
                  variant="top"
                  isCompact
                  widgetId="documents-pagination-top"
                />
              </PaginationToolbarItem>
            </ToolbarContent>
          </Toolbar>

          <Table aria-label="Documents table">
            <Thead>
              <Tr isHeaderRow>
                <Th columnKey="supplierId" />
                <Th columnKey="documentType" />
                <Th columnKey="documentId" />
              </Tr>
            </Thead>
            <ConditionalTableBody
              isLoading={isFetching}
              isError={!!fetchError}
              isNoData={result.data.length === 0}
              numRenderedColumns={numRenderedColumns}
            >
              <Tbody>
                {currentPageItems?.map((item, rowIndex) => {
                  return (
                    <Tr key={item.id} item={item} rowIndex={rowIndex}>
                      <Td width={15} columnKey="supplierId">
                        {item.supplier_id}
                      </Td>
                      <Td width={15} columnKey="documentType">
                        {item.document_type}
                      </Td>
                      <Td width={15} columnKey="documentId">
                        {item.document_id}
                      </Td>
                      <TdAction isActionCell>
                        <ActionsColumn items={[]} />
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
            widgetId="documents-pagination-bottom"
          />
        </div>

        <UploadFilesDrawer
          projectId={uploadFilesToProjectId}
          onCloseClick={() => setUploadFilesToProjectId(null)}
        />
      </PageSection>
    </>
  );
};

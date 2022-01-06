import React, { useEffect, useState } from "react";
import {
  Redirect,
  useHistory,
  useParams,
  useRouteMatch,
} from "react-router-dom";
import { useTranslation } from "react-i18next";

import {
  Button,
  ButtonVariant,
  Card,
  CardBody,
  PageSection,
  ToolbarGroup,
  ToolbarItem,
  Skeleton,
  ProgressStepper,
  ProgressStep,
  DescriptionList,
  DescriptionListGroup,
  DescriptionListTerm,
  DescriptionListDescription,
  Stack,
  StackItem,
  Bullseye,
  ProgressStepProps,
} from "@patternfly/react-core";
import {
  cellWidth,
  IAction,
  ICell,
  IExtraData,
  IRow,
  IRowData,
  ISeparator,
  truncate,
} from "@patternfly/react-table";
import { CheckCircleIcon, SpinnerAltIcon } from "@patternfly/react-icons";

import { useSelectionState } from "@konveyor/lib-ui";
import {
  ConditionalRender,
  SimpleTableWithToolbar,
  useModal,
  useTableControls,
} from "@project-openubl/lib-ui";

import {
  NamespaceContextSelector,
  SearchInput,
  SimplePageSection,
  useNamespaceContext,
} from "shared/components";

import {
  IDocumentsParams,
  IDocumentsParamsBuilder,
  useDocumentsQuery,
} from "queries/documents";

import { SortByQuery } from "api/models";
import { UBLDocument } from "api/ublhub";
import { documentsPath, formatPath, INamespaceParams, Paths } from "Paths";
import { formatTimestamp } from "utils/dateUtils";

import sunatImage from "images/sunat.png";
import { PreviewFile } from "./preview-file";

const toSortByQuery = (sortBy?: {
  index: number;
  direction: "asc" | "desc";
}): SortByQuery | undefined => {
  if (!sortBy) {
    return undefined;
  }

  let field: string;
  switch (sortBy.index) {
    case 1:
      field = "nombre";
      break;
    default:
      return undefined;
  }

  return {
    orderBy: field,
    orderDirection: sortBy.direction,
  };
};

const ROW_FIELD = "row_field";

const getRow = (rowData: IRowData): UBLDocument => {
  return rowData[ROW_FIELD];
};

const StepXML: React.FC<Partial<Omit<ProgressStepProps, "ref">>> = ({
  children,
  ...props
}) => (
  <ProgressStep
    id="xml"
    variant="success"
    titleId="xml-created"
    aria-label="xml status"
    description="XML creado y firmado"
    {...props}
  >
    {children}
  </ProgressStep>
);

const StepTicket: React.FC<
  Partial<Omit<ProgressStepProps, "ref">> & { ticket?: string }
> = ({ ticket, children, ...props }) => (
  <>
    {ticket && (
      <ProgressStep
        id="ticket"
        variant="success"
        titleId="ticket"
        aria-label="ticket status"
        description={ticket}
        {...props}
      >
        {children}
      </ProgressStep>
    )}
  </>
);

const StepSUNAT: React.FC<
  Partial<Omit<ProgressStepProps, "ref">> & {
    inProgress: boolean;
    sunatStatus?:
      | "ACEPTADO"
      | "RECHAZADO"
      | "EXCEPCION"
      | "BAJA"
      | "EN_PROCESO";
    sunatDescripcion?: string;
  }
> = ({ inProgress, sunatStatus, sunatDescripcion, children, ...props }) => (
  <>
    <ProgressStep
      id="sunat"
      isCurrent
      variant={
        inProgress === true
          ? "info"
          : sunatStatus === "ACEPTADO"
          ? "success"
          : sunatStatus === "RECHAZADO"
          ? "danger"
          : "warning"
      }
      titleId="sunat"
      aria-label="sunat status"
      description={sunatDescripcion}
      {...props}
    >
      {children}
    </ProgressStep>
  </>
);

const itemsToRow = (
  items: UBLDocument[],
  isRowCollapsed: (item: UBLDocument) => boolean
) => {
  const rows: IRow[] = [];

  items.forEach((item) => {
    const isCollapsed = isRowCollapsed(item);

    // Main Row
    rows.push({
      [ROW_FIELD]: item,
      isOpen: isCollapsed,
      cells: [
        {
          title: (
            <Button variant="link">
              {item.inProgress ? <SpinnerAltIcon /> : <CheckCircleIcon />}{" "}
              {item.id?.substring(0, 8)}
            </Button>
          ),
        },
        {
          title: (
            <ConditionalRender
              when={item.inProgress}
              then={<Skeleton width="100%" />}
            >
              {item.fileContent?.ruc}
            </ConditionalRender>
          ),
        },
        {
          title: (
            <ConditionalRender
              when={item.inProgress}
              then={<Skeleton width="100%" />}
            >
              {item.fileContent?.documentType}
            </ConditionalRender>
          ),
        },
        {
          title: (
            <ConditionalRender
              when={item.inProgress}
              then={<Skeleton width="100%" />}
            >
              {item.fileContent?.documentID}
            </ConditionalRender>
          ),
        },
        {
          title: (
            <ProgressStepper isCompact>
              <StepXML />
              <StepTicket ticket={item.sunat?.ticket} />
              <StepSUNAT
                inProgress={item.inProgress}
                sunatStatus={item.sunat?.status}
                sunatDescripcion={item.sunat?.description}
              />
            </ProgressStepper>
          ),
        },
        {
          title: (
            <ConditionalRender
              when={item.inProgress}
              then={<Skeleton width="100%" />}
            >
              <img src={sunatImage} height={20} width={20} alt="SUNAT logo" />
              {item.sunat?.status}
            </ConditionalRender>
          ),
        },
        {
          title: formatTimestamp(item.createdOn),
        },
      ],
    });

    // Expanded areas
    if (isCollapsed) {
      rows.push({
        parent: rows.length - 1,
        fullWidth: true,
        cells: [
          {
            title: (
              <div className="pf-c-table__expandable-row-content">
                <Stack>
                  <StackItem>
                    <Bullseye>
                      <ProgressStepper isCenterAligned>
                        <StepXML>XML</StepXML>
                        <StepTicket ticket={item.sunat?.ticket}>
                          Ticket
                        </StepTicket>
                        <StepSUNAT
                          inProgress={item.inProgress}
                          sunatStatus={item.sunat?.status}
                          sunatDescripcion={item.sunat?.description}
                        >
                          {item.sunat?.status || "Loading..."}
                        </StepSUNAT>
                      </ProgressStepper>
                    </Bullseye>
                  </StackItem>
                  <StackItem>
                    <DescriptionList columnModifier={{ lg: "3Col" }}>
                      <DescriptionListGroup>
                        <DescriptionListTerm>Retry count</DescriptionListTerm>
                        <DescriptionListDescription>
                          {item.retryCount}
                        </DescriptionListDescription>
                      </DescriptionListGroup>
                      {item.error && (
                        <DescriptionListGroup>
                          <DescriptionListTerm>Error</DescriptionListTerm>
                          <DescriptionListDescription>
                            {item.error}
                          </DescriptionListDescription>
                        </DescriptionListGroup>
                      )}
                    </DescriptionList>
                  </StackItem>
                </Stack>
              </div>
            ),
          },
        ],
      });
    }
  });

  return rows;
};

export const DocumentsList: React.FC = () => {
  const { t } = useTranslation();

  const history = useHistory();

  //
  const { namespaceId } = useParams<INamespaceParams>();
  const documentsRouteMatch = useRouteMatch<INamespaceParams>({
    path: Paths.documents,
    exact: true,
  });

  const namespaceContext = useNamespaceContext();

  //
  enum ModalAction {
    VIEW_UBL_FILE,
    VIEW_CDR_FILE,
  }
  const modal = useModal<ModalAction, UBLDocument>();

  //
  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  //
  const [queryParams, setQueryParams] = useState<IDocumentsParams>(
    new IDocumentsParamsBuilder()
      .withFilterText(filterText)
      .withPagination(currentPage)
      .withSorting(toSortByQuery(currentSortBy))
      .build()
  );

  const documentsQuery = useDocumentsQuery(namespaceId || "", queryParams);

  useEffect(() => {
    const params = new IDocumentsParamsBuilder()
      .withFilterText(filterText)
      .withPagination(currentPage)
      .withSorting(toSortByQuery(currentSortBy))
      .build();
    setQueryParams(params);
  }, [filterText, currentPage, currentSortBy]);

  //
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<UBLDocument>({
    items: documentsQuery?.data?.items || [],
    isEqual: (a, b) => a.id === b.id,
  });

  const onCollapseRow = (
    event: React.MouseEvent,
    rowIndex: number,
    isOpen: boolean,
    rowData: IRowData,
    extraData: IExtraData
  ) => {
    const row = getRow(rowData);
    toggleRowExpanded(row);
  };

  const columns: ICell[] = [
    {
      title: t("terms.id"),
      transforms: [cellWidth(10)],
      cellTransforms: [],
    },
    {
      title: "RUC",
      transforms: [cellWidth(15)],
      cellTransforms: [truncate],
    },
    {
      title: t("terms.type"),
      transforms: [cellWidth(15)],
      cellTransforms: [truncate],
    },
    {
      title: t("terms.document"),
      transforms: [cellWidth(15)],
      cellTransforms: [truncate],
    },
    {
      title: t("terms.progress"),
      transforms: [cellWidth(15)],
    },
    {
      title: "SUNAT",
      transforms: [cellWidth(15)],
    },
    {
      title: t("terms.created-on"),
      transforms: [cellWidth(15)],
    },
  ];

  const rows: IRow[] = itemsToRow(
    documentsQuery.data?.items || [],
    isRowExpanded
  );

  const actionResolver = (rowData: IRowData): (IAction | ISeparator)[] => {
    const row: UBLDocument = getRow(rowData);
    if (!row) {
      return [];
    }

    const actions: (IAction | ISeparator)[] = [];
    actions.push({
      title: "Ver XML",
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData
      ) => {
        const row: UBLDocument = getRow(rowData);
        modal.open(ModalAction.VIEW_UBL_FILE, row);
      },
    });

    if (row.sunat?.hasCdr) {
      actions.push({
        title: "Ver CDR",
        onClick: (
          event: React.MouseEvent,
          rowIndex: number,
          rowData: IRowData
        ) => {
          const row: UBLDocument = getRow(rowData);
          modal.open(ModalAction.VIEW_CDR_FILE, row);
        },
      });
    }

    return actions;
  };

  //
  const onCreateDocument = () => {
    history.push(
      formatPath(Paths.documents_ns_create, {
        namespaceId,
      })
    );
  };

  return (
    <>
      {documentsRouteMatch?.isExact && namespaceContext ? (
        <Redirect to={documentsPath(namespaceContext.id)} />
      ) : (
        <NamespaceContextSelector redirect={(ns) => documentsPath(ns.id)}>
          <SimplePageSection title="Documents" />
          <PageSection>
            <Card>
              <CardBody>
                <SimpleTableWithToolbar
                  hasTopPagination
                  hasBottomPagination
                  totalCount={documentsQuery.data?.meta.count || 0}
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
                  onCollapse={onCollapseRow}
                  // Fech data
                  isLoading={documentsQuery.isLoading}
                  loadingVariant="skeleton"
                  fetchError={documentsQuery.isError}
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
                          aria-label="create-document"
                          variant={ButtonVariant.secondary}
                          onClick={onCreateDocument}
                        >
                          {t("actions.create-object", { what: "document" })}
                        </Button>
                      </ToolbarItem>
                    </ToolbarGroup>
                  }
                />
              </CardBody>
            </Card>
          </PageSection>
        </NamespaceContextSelector>
      )}

      {modal.isOpen &&
        modal.data &&
        (modal.isAction(ModalAction.VIEW_UBL_FILE) ||
          modal.isAction(ModalAction.VIEW_CDR_FILE)) && (
          <PreviewFile
            namespaceId={namespaceId}
            documentBeingEdited={modal.data}
            onClose={modal.close}
            variant={modal.isAction(ModalAction.VIEW_UBL_FILE) ? "ubl" : "cdr"}
          />
        )}
    </>
  );
};

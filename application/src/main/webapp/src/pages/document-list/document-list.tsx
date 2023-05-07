import React, { useEffect, useState } from "react";
import { useMatch, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { StatusIcon, StatusType, useSelectionState } from "@migtools/lib-ui";
import {
  ConditionalRender,
  SimpleTableWithToolbar,
  useModal,
  useTableControls,
} from "@project-openubl/lib-ui";

import {
  Bullseye,
  Button,
  ButtonVariant,
  Card,
  CardBody,
  CardTitle,
  ContextSelectorFooter,
  DescriptionList,
  DescriptionListDescription,
  DescriptionListGroup,
  DescriptionListTerm,
  Divider,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  Grid,
  GridItem,
  Icon,
  List,
  ListItem,
  Modal,
  PageSection,
  PageSectionVariants,
  SearchInput,
  Text,
  TextContent,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
  Truncate,
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
import { ArrowUpIcon, ShieldAltIcon } from "@patternfly/react-icons";

import {
  IDocumentsQueryParams,
  IDocumentsQueryParamsBuilder,
  useDocumentsQuery,
} from "queries/documents";

import { ContextOption, ProjectContextSelector } from "shared/context";

import { formatTimestamp } from "utils/dateUtils";
import { DocumentDto } from "api/models";

import { DocumentEditor } from "./components/document-editor";
import { XmlCdrPreview } from "./components/xml-cdr-preview";
import { PdfPreview } from "./components/pdf-preview";

const getStatusType = (status?: string): StatusType => {
  if (status === "ACEPTADO") {
    return "Ok";
  } else if (status === "BAJA") {
    return "Warning";
  } else if (status === "EN_PROCESO") {
    return "Paused";
  } else if (status === "EXCEPCION") {
    return "Error";
  } else if (status === "RECHAZADO") {
    return "Error";
  } else {
    return "Unknown";
  }
};

const ROW_FIELD = "row_field";
const getRow = (rowData: IRowData): DocumentDto => {
  return rowData[ROW_FIELD];
};

const itemsToRow = (
  items: DocumentDto[],
  isRowCollapsed: (item: DocumentDto) => boolean
) => {
  const rows: IRow[] = [];

  items.forEach((item) => {
    const isCollapsed = isRowCollapsed(item);

    // Main row
    rows.push({
      [ROW_FIELD]: item,
      isOpen: isCollapsed,
      cells: [
        {
          title: (
            <TextContent>
              <Icon isInline isInProgress={item.status.inProgress}>
                <ShieldAltIcon />
              </Icon>{" "}
              <Truncate content={item.id} />
            </TextContent>
          ),
        },
        {
          title: formatTimestamp(item.created),
        },
        {
          title: (
            <ConditionalRender
              when={item.status.inProgress}
              then={<span>-</span>}
            >
              <div>{item.status.xmlData?.ruc}</div>
            </ConditionalRender>
          ),
        },
        {
          title: (
            <ConditionalRender
              when={item.status.inProgress}
              then={<span>-</span>}
            >
              <div>
                {item.status.xmlData?.tipoDocumento}:{" "}
                {item.status.xmlData?.serieNumero}
              </div>
            </ConditionalRender>
          ),
        },
        {
          title: (
            <ConditionalRender
              when={item.status.inProgress}
              then={<span>-</span>}
            >
              <StatusIcon
                status={getStatusType(item.status.sunat?.status)}
                label={item.status.sunat?.status}
              />
            </ConditionalRender>
          ),
        },
        {
          title: (
            <ConditionalRender
              when={item.status.inProgress}
              then={<span>-</span>}
            >
              {item.status.sunat?.description}
            </ConditionalRender>
          ),
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
              <Grid className="pf-c-table__expandable-row-content">
                <GridItem>
                  <Card>
                    <CardTitle>SUNAT</CardTitle>
                    <CardBody>
                      <DescriptionList>
                        <DescriptionListGroup>
                          <DescriptionListTerm>Descripción</DescriptionListTerm>
                          <DescriptionListDescription>
                            {item.status.sunat?.description}
                          </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                          <DescriptionListTerm>Código</DescriptionListTerm>
                          <DescriptionListDescription>
                            {item.status.sunat?.code}
                          </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                          <DescriptionListTerm>
                            Observaciones
                          </DescriptionListTerm>
                          <DescriptionListDescription>
                            <List>
                              {item.status.sunat?.notes.map((e) => (
                                <ListItem>{e}</ListItem>
                              ))}
                            </List>
                          </DescriptionListDescription>
                        </DescriptionListGroup>
                        <DescriptionListGroup>
                          <DescriptionListTerm>Ticket</DescriptionListTerm>
                          <DescriptionListDescription>
                            {item.status.sunat?.ticket}
                          </DescriptionListDescription>
                        </DescriptionListGroup>
                      </DescriptionList>
                    </CardBody>
                  </Card>
                </GridItem>
              </Grid>
            ),
          },
        ],
      });
    }
  });

  return rows;
};

export const DocumentList: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const matchSingleProjectPage = useMatch("/documents/projects/:projectName");
  const projectName = matchSingleProjectPage?.params.projectName;

  const onProjectContextChange = (context: ContextOption) => {
    navigate("/documents/projects/" + context.key);
  };

  const documentModal = useModal<"ADD", DocumentDto>();
  const rowModal = useModal<"xml" | "cdr" | "pdf", DocumentDto>();

  const [filterTextTemp, setFilterTextTemp] = useState("");
  const [filterText, setFilterText] = useState("");
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls({ sortBy: { index: 0, direction: "asc" } });

  // Queries
  const [queryParams, setQueryParams] = useState<IDocumentsQueryParams>(
    new IDocumentsQueryParamsBuilder()
      .withFilterText(filterText)
      .withPagination(currentPage)
      .build()
  );

  useEffect(() => {
    const filters: any = filterText.split(" ").reduce((prev, current) => {
      const keyValue = current.split(":");
      let filter;
      if (keyValue.length === 2) {
        filter = { [keyValue[0]]: keyValue[1] };
      } else {
        const filterTextVal = [prev.filterText, keyValue[0]].join(" ");
        return { filterText: filterTextVal.trim() };
      }

      return { ...prev, ...filter };
    }, {} as any);

    const params = new IDocumentsQueryParamsBuilder()
      .withFilterText(filters.filterText)
      .withRuc(filters.ruc)
      .withPagination(currentPage)
      .build();
    setQueryParams(params);
  }, [filterText, currentPage]);

  const documentsQuery = useDocumentsQuery(projectName || null, queryParams);

  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<DocumentDto>({
    items: documentsQuery?.data?.items || [],
    isEqual: (a, b) => a.id === b.id,
  });

  const columns: ICell[] = [
    {
      title: "ID",
      transforms: [cellWidth(15)],
      cellTransforms: [],
    },
    { title: t("terms.created-on"), transforms: [cellWidth(15)] },
    { title: "RUC", transforms: [cellWidth(10)] },
    { title: t("terms.document"), transforms: [cellWidth(20)] },
    { title: "SUNAT", transforms: [cellWidth(10)] },
    { title: "Descripción", transforms: [], cellTransforms: [truncate] },
  ];

  const rows: IRow[] = itemsToRow(
    documentsQuery.data?.items || [],
    isRowExpanded
  );

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

  const actionResolver = (rowData: IRowData): (IAction | ISeparator)[] => {
    const row: DocumentDto = getRow(rowData);
    if (!row) {
      return [];
    }

    const actions: (IAction | ISeparator)[] = [];

    actions.push({
      title: "Ver PDF",
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData
      ) => {
        const row: DocumentDto = getRow(rowData);
        rowModal.open("pdf", row);
      },
    });

    if (row.status.xmlData) {
      actions.push({
        title: "Ver XML",
        onClick: (
          event: React.MouseEvent,
          rowIndex: number,
          rowData: IRowData
        ) => {
          const row: DocumentDto = getRow(rowData);
          rowModal.open("xml", row);
        },
      });
    }

    if (row.status.sunat?.hasCdr) {
      actions.push({
        title: "Ver CDR",
        onClick: (
          event: React.MouseEvent,
          rowIndex: number,
          rowData: IRowData
        ) => {
          const row: DocumentDto = getRow(rowData);
          rowModal.open("cdr", row);
        },
      });
    }

    return actions;
  };

  let modalTitle;
  switch (rowModal.action) {
    case "xml":
      modalTitle = "Ver XML";
      break;
    case "cdr":
      modalTitle = "Ver CDR";
      break;
    case "pdf":
      modalTitle = "Ver PDF";
      break;
    default:
      modalTitle = "...";
      break;
  }
  return (
    <>
      <PageSection
        variant={PageSectionVariants.light}
        padding={{ default: "noPadding" }}
      >
        <Toolbar>
          <ToolbarContent>
            <ToolbarItem>{t("terms.projects")}:</ToolbarItem>
            <ToolbarItem>
              <ProjectContextSelector
                contextKeyFromURL={projectName}
                onChange={onProjectContextChange}
                props={{
                  footer: (
                    <ContextSelectorFooter>
                      <Button
                        variant="secondary"
                        isInline
                        onClick={() => navigate("/projects")}
                      >
                        {t("actions.create-object", {
                          what: t("terms.project").toLowerCase(),
                        })}
                      </Button>
                    </ContextSelectorFooter>
                  ),
                }}
              />
            </ToolbarItem>
          </ToolbarContent>
        </Toolbar>
      </PageSection>
      <Divider />
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">{t("terms.documents")}</Text>
          <Text component="p">Lista de documentos disponibles.</Text>
        </TextContent>
      </PageSection>
      <Divider />
      <PageSection variant="light" type="nav">
        <ConditionalRender
          when={!projectName}
          then={
            <Bullseye>
              <EmptyState>
                <EmptyStateIcon icon={ArrowUpIcon} />
                <Title headingLevel="h4" size="lg">
                  Selecciona un proyecto
                </Title>
                <EmptyStateBody>
                  Selecciona el proyecto al cual deseas acceder.
                </EmptyStateBody>
              </EmptyState>
            </Bullseye>
          }
        >
          <SimpleTableWithToolbar
            hasTopPagination
            hasBottomPagination
            totalCount={documentsQuery.data?.count || 0}
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
              <ToolbarItem variant="search-filter">
                <SearchInput
                  value={filterTextTemp}
                  onChange={(_, value) => setFilterTextTemp(value)}
                  onSearch={() => setFilterText(filterTextTemp)}
                  attributes={[{ attr: "ruc", display: "RUC" }]}
                  advancedSearchDelimiter={":"}
                  hasWordsAttrLabel="Filter text"
                />
              </ToolbarItem>
            }
            toolbarActions={
              <ToolbarGroup variant="button-group">
                <ToolbarItem>
                  <Button
                    type="button"
                    aria-label="new-document"
                    variant={ButtonVariant.primary}
                    onClick={() => documentModal.open("ADD")}
                  >
                    {t("actions.create-object", {
                      what: t("terms.document").toLowerCase(),
                    })}
                  </Button>
                </ToolbarItem>
              </ToolbarGroup>
            }
          />
        </ConditionalRender>
      </PageSection>

      <Modal
        variant="default"
        title="Crear documento"
        disableFocusTrap
        isOpen={documentModal.isOpen}
        onClose={documentModal.close}
      >
        {projectName && (
          <DocumentEditor
            projectName={projectName}
            onSaved={documentModal.close}
            onCancel={documentModal.close}
          />
        )}
      </Modal>

      <Modal
        variant="large"
        title={modalTitle}
        isOpen={rowModal.isOpen}
        onClose={rowModal.close}
        actions={[
          <Button key="close" variant="primary" onClick={rowModal.close}>
            {t("actions.close")}
          </Button>,
        ]}
      >
        {projectName &&
          rowModal.data &&
          (rowModal.action === "xml" || rowModal.action === "cdr") && (
            <XmlCdrPreview
              projectName={projectName}
              document={rowModal.data}
              variant={rowModal.action === "xml" ? "xml" : "cdr"}
            />
          )}
        {projectName && rowModal.data && rowModal.action === "pdf" && (
          <PdfPreview projectName={projectName} document={rowModal.data} />
        )}
      </Modal>
    </>
  );
};

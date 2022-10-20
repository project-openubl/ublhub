import React, { useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import { useTranslation, Trans } from "react-i18next";

import {
  Bullseye,
  Button,
  ButtonVariant,
  DataList,
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Drawer,
  DrawerActions,
  DrawerCloseButton,
  DrawerContent,
  DrawerContentBody,
  DrawerHead,
  DrawerPanelContent,
  Dropdown,
  DropdownItem,
  DropdownPosition,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  Icon,
  KebabToggle,
  Modal,
  SearchInput,
  Stack,
  StackItem,
  Text,
  TextContent,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
  ToolbarToggleGroup,
} from "@patternfly/react-core";
import {
  FilterIcon,
  BuildingIcon,
  InfoAltIcon,
  CheckCircleIcon,
  InfrastructureIcon,
} from "@patternfly/react-icons";

import { useSelectionState } from "@migtools/lib-ui";
import {
  ConditionalRender,
  useConfirmationContext,
  useModal,
  useTable,
  useTableControls,
} from "@project-openubl/lib-ui";

import { CompanyDto, ProjectDto } from "api/models";
import { useCompaniesQuery, useDeleteCompanyMutation } from "queries/companies";

import { AddCompanyForm } from "./components/add-company-form";
import { CompanyDetailsTabs } from "./components/company-details-tabs";

export const compareByColumnIndex = (
  a: CompanyDto,
  b: CompanyDto,
  columnIndex?: number
) => {
  switch (columnIndex) {
    default:
      return 0;
  }
};

export const filterByText = (filterText: string, item: CompanyDto) => {
  const text = filterText.toLowerCase();
  return (
    item.ruc.toString().toLowerCase().indexOf(text) !== -1 ||
    item.name.toString().toLowerCase().indexOf(text) !== -1 ||
    item.description?.toString().toLowerCase().indexOf(text) !== -1
  );
};

const Companies: React.FC = () => {
  const { t } = useTranslation();

  const confirmationModal = useConfirmationContext();

  const project = useOutletContext<ProjectDto | null>();
  const companiesQuery = useCompaniesQuery(project?.id || null);
  const deleteCompanyMutation = useDeleteCompanyMutation(
    project?.id || null,
    () => {
      confirmationModal.close();
    }
  );

  const companyModal = useModal<"ADD", CompanyDto>();
  const drawerModal = useModal<"VIEW", CompanyDto>();

  const {
    isItemSelected: isActionsKebabExpanded,
    toggleItemSelected: toggleActionsKebab,
  } = useSelectionState<CompanyDto>({
    items: companiesQuery.data || [],
    isEqual: (a, b) => {
      return a.id === b.id;
    },
  });

  const [filterText, setFilterText] = useState("");
  const { page: currentPage, sortBy: currentSortBy } = useTableControls({
    page: { page: 1, perPage: 1_000 },
    sortBy: { index: 0, direction: "asc" },
  });

  const { filteredItems } = useTable<CompanyDto>({
    items: companiesQuery.data || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => filterByText(filterText, item),
  });

  const deleteCompany = (row: CompanyDto) => {
    confirmationModal.open({
      title: t("modal.confirm-delete.title", {
        what: t("terms.company"),
      }),
      titleIconVariant: "warning",
      message: (
        <Trans
          i18nKey="modal.confirm-delete.body"
          values={{ type: t("terms.company"), name: row.name }}
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
        deleteCompanyMutation.mutate(row);
      },
    });
  };

  return (
    <>
      <div
        style={{
          backgroundColor: "var(--pf-global--BackgroundColor--100)",
        }}
      >
        <Drawer isExpanded={drawerModal.isOpen} isInline>
          <DrawerContent
            panelContent={
              <DrawerPanelContent defaultSize="50%">
                <DrawerHead>
                  <TextContent>
                    <Text component="h1">{drawerModal.data?.name}</Text>
                  </TextContent>
                  <DrawerActions>
                    <DrawerCloseButton onClick={drawerModal.close} />
                  </DrawerActions>
                </DrawerHead>
                <div>
                  {project && drawerModal.data && (
                    <CompanyDetailsTabs
                      project={project}
                      company={drawerModal.data}
                    />
                  )}
                </div>
              </DrawerPanelContent>
            }
          >
            <DrawerContentBody>
              <Toolbar
                className="pf-m-toggle-group-container"
                collapseListedFiltersBreakpoint="xl"
              >
                <ToolbarContent>
                  <ToolbarToggleGroup
                    toggleIcon={<FilterIcon />}
                    breakpoint="xl"
                  >
                    <ToolbarItem variant="search-filter">
                      <SearchInput
                        value={filterText}
                        onChange={setFilterText}
                      />
                    </ToolbarItem>
                  </ToolbarToggleGroup>
                  <ToolbarGroup variant="button-group">
                    <ToolbarItem>
                      <Button
                        type="button"
                        aria-label="new-company"
                        variant={ButtonVariant.primary}
                        onClick={() => companyModal.open("ADD")}
                      >
                        {t("actions.create-object", {
                          what: t("terms.company").toLowerCase(),
                        })}
                      </Button>
                    </ToolbarItem>
                  </ToolbarGroup>
                </ToolbarContent>
              </Toolbar>

              <ConditionalRender
                when={companiesQuery.data?.length === 0}
                then={
                  <Bullseye>
                    <EmptyState>
                      <EmptyStateIcon icon={InfoAltIcon} />
                      <Title headingLevel="h4" size="lg">
                        Vacío
                      </Title>
                      <EmptyStateBody>
                        No hay empresas registradas
                      </EmptyStateBody>
                    </EmptyState>
                  </Bullseye>
                }
              >
                <DataList id="data-list" aria-label="Companies list">
                  {filteredItems.map((item) => (
                    <DataListItem
                      key={item.ruc}
                      style={
                        drawerModal.data?.id === item.id
                          ? {
                              borderRightStyle: "solid",
                              borderRightColor: "var(--pf-global--link--Color)",
                            }
                          : undefined
                      }
                    >
                      <DataListItemRow>
                        <DataListItemCells
                          dataListCells={[
                            <DataListCell key="icon" isIcon>
                              <BuildingIcon />
                            </DataListCell>,
                            <DataListCell key="primary content">
                              <div>{item.name}</div>
                              <small>{item.description}</small>
                            </DataListCell>,
                            <DataListCell key="secondary content">
                              <span>RUC: {item.ruc}</span>
                            </DataListCell>,
                            <DataListCell key="sunat content">
                              {item.sunat ? (
                                <div>
                                  <div>
                                    <Text component="p">
                                      <Icon isInline status="success">
                                        <CheckCircleIcon />
                                      </Icon>{" "}
                                      SUNAT
                                    </Text>
                                  </div>
                                </div>
                              ) : (
                                <div>
                                  <div>
                                    <Text component="p">
                                      <Icon isInline status="info">
                                        <InfrastructureIcon />
                                      </Icon>{" "}
                                      SUNAT
                                    </Text>
                                  </div>
                                  <span>
                                    <Link to="../sunat">Proyecto</Link>
                                  </span>
                                </div>
                              )}
                            </DataListCell>,
                            <DataListAction
                              key="quick actions content"
                              id="quick actions"
                              aria-label="Quick actions"
                              aria-labelledby="Quick actions"
                            >
                              <Stack>
                                <StackItem>
                                  <Button
                                    variant={ButtonVariant.secondary}
                                    onClick={() =>
                                      drawerModal.open("VIEW", item)
                                    }
                                  >
                                    {t("actions.edit")}
                                  </Button>
                                </StackItem>
                              </Stack>
                            </DataListAction>,
                          ]}
                        />
                        <DataListAction
                          id="actions"
                          aria-label="Actions"
                          aria-labelledby="Actions"
                          isPlainButtonAction
                        >
                          <Dropdown
                            isPlain
                            position={DropdownPosition.right}
                            isOpen={isActionsKebabExpanded(item)}
                            toggle={
                              <KebabToggle
                                onToggle={() => toggleActionsKebab(item)}
                              />
                            }
                            dropdownItems={[
                              <DropdownItem
                                key="edit"
                                onClick={() => {
                                  drawerModal.open("VIEW", item);
                                  toggleActionsKebab(item);
                                }}
                              >
                                {t("actions.edit")}
                              </DropdownItem>,
                              <DropdownItem
                                key="delete"
                                component="button"
                                onClick={() => deleteCompany(item)}
                              >
                                {t("actions.delete")}
                              </DropdownItem>,
                            ]}
                            menuAppendTo={() =>
                              document.getElementById("data-list") as any
                            }
                          />
                        </DataListAction>
                      </DataListItemRow>
                    </DataListItem>
                  ))}
                </DataList>
              </ConditionalRender>
            </DrawerContentBody>
          </DrawerContent>
        </Drawer>
      </div>

      <Modal
        variant="medium"
        title={t("actions.create-object", {
          what: t("terms.company").toLowerCase(),
        })}
        isOpen={companyModal.isOpen}
        onClose={companyModal.close}
      >
        {project && (
          <AddCompanyForm
            projectId={project.id!}
            onSaved={companyModal.close}
            onCancel={companyModal.close}
          />
        )}
      </Modal>
    </>
  );
};

export default Companies;

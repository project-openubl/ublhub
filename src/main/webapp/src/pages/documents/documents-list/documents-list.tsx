import React from "react";
import { Redirect, useHistory, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";

import {
  Button,
  ButtonVariant,
  Card,
  CardBody,
  PageSection,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import spacing from "@patternfly/react-styles/css/utilities/Spacing/spacing";

import { ResolvedQueries } from "@konveyor/lib-ui";
import { SimpleTableWithToolbar } from "@project-openubl/lib-ui";

import {
  NamespaceContextSelector,
  SearchInput,
  SimplePageSection,
  useNamespaceContext,
} from "shared/components";

import { LONG_LOADING_MESSAGE } from "queries/constants";

import { documentsPath, formatPath, INamespaceParams, Paths } from "Paths";

export const DocumentsList: React.FC = () => {
  const { t } = useTranslation();

  const history = useHistory();
  const documentsRouteMatch = useRouteMatch<INamespaceParams>({
    path: Paths.documents,
    exact: true,
  });

  const namespaceContext = useNamespaceContext();

  return (
    <>
      {documentsRouteMatch?.isExact && namespaceContext ? (
        <Redirect to={documentsPath(namespaceContext)} />
      ) : (
        <NamespaceContextSelector redirect={documentsPath}>
          <SimplePageSection title="Documents" />
          <PageSection>
            <Card>
              <CardBody>
                <ResolvedQueries
                  resultsWithErrorTitles={
                    [
                      // { result: keysQuery, errorTitle: "Cannot load keys" },
                      // {
                      //   result: componentsQuery,
                      //   errorTitle: "Cannot load components",
                      // },
                    ]
                  }
                  errorsInline={false}
                  className={spacing.mMd}
                  emptyStateBody={LONG_LOADING_MESSAGE}
                >
                  <SimpleTableWithToolbar
                    hasTopPagination
                    hasBottomPagination
                    totalCount={0}
                    // Sorting
                    // sortBy={currentSortBy}
                    onSort={() => {}}
                    // Pagination
                    currentPage={{ page: 1, perPage: 10 }}
                    onPageChange={() => {}}
                    // Table
                    rows={[]}
                    cells={[]}
                    actions={[]}
                    // Fech data
                    isLoading={false}
                    loadingVariant="skeleton"
                    fetchError={false}
                    // Toolbar filters
                    filtersApplied={false}
                    toolbarToggle={
                      <ToolbarGroup variant="filter-group">
                        <ToolbarItem>
                          <SearchInput onSearch={() => {}} />
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
                            onClick={() => {
                              history.push(
                                formatPath(Paths.documents_ns_create, {
                                  namespaceId: namespaceContext?.id,
                                })
                              );
                            }}
                          >
                            {t("actions.create-object", { what: "document" })}
                          </Button>
                        </ToolbarItem>
                      </ToolbarGroup>
                    }
                  />
                </ResolvedQueries>
              </CardBody>
            </Card>
          </PageSection>
        </NamespaceContextSelector>
      )}
    </>
  );
};

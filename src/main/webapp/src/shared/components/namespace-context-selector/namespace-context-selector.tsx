import React, { useEffect, useReducer, useState } from "react";
import { useHistory, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import {
  PageSection,
  Toolbar,
  ToolbarContent,
  Divider,
  ContextSelector,
  ContextSelectorItem,
  ToolbarItem,
  ContextSelectorFooter,
  Button,
} from "@patternfly/react-core";

import { ResolvedQuery } from "@konveyor/lib-ui";
import { useModal } from "@project-openubl/lib-ui";

import { useDispatch, useSelector } from "react-redux";
import { RootState } from "store/rootReducer";
import {
  namespaceContextSelectors,
  namespaceContextActions,
} from "store/namespace-context";

import { AddNamespaceWizard } from "shared/components";
import { useNamespacesQuery } from "queries/namespaces";

import { documentsPath, INamespaceParams } from "Paths";
import { Namespace } from "api/models";

import { NoNamespaceSelected } from "./no-namespace-selected";

export const useNamespaceContext = () => {
  const dispatch = useDispatch();

  const { namespaceId: namespaceIdFromURL } = useParams<INamespaceParams>();
  const namespaceIdFromLocalStore = useSelector((state: RootState) =>
    namespaceContextSelectors.selectedNamespace(state)
  );
  const namespaceId = namespaceIdFromURL || namespaceIdFromLocalStore;

  const namespacesQuery = useNamespacesQuery();

  useEffect(() => {
    if (namespaceIdFromURL) {
      dispatch(
        namespaceContextActions.setSelectedNamespace(namespaceIdFromURL)
      );
    }
  }, [namespaceIdFromURL, dispatch]);

  return namespacesQuery.data?.find((f) => f.id === namespaceId);
};

export interface INamespaceContextSelectorProps {
  redirect: (selectedNamespace: Namespace) => string;
}

export const NamespaceContextSelector: React.FC<INamespaceContextSelectorProps> =
  ({ redirect, children }) => {
    const { t } = useTranslation();

    const history = useHistory();
    const namespacesQuery = useNamespacesQuery();
    const namespaceContext = useNamespaceContext();

    const [filterText, setFilterText] = useState("");
    const [isContextSelectorOpen, toggleContextSelector] = useReducer(
      (isVisible) => !isVisible,
      false
    );

    enum ModalAction {
      ADD,
    }
    const modal = useModal<ModalAction, Namespace>();

    const onSelectNamespace = (namespace: Namespace) => {
      const ns = namespacesQuery.data?.find((f) => f.id === namespace.id);
      if (!ns) {
        return;
      }

      toggleContextSelector();

      // The url will save the selected value so no need to save it in a state
      // useNamespaceContext() is in charge of defining it
      history.push(redirect(ns));
    };

    return (
      <>
        <ResolvedQuery
          result={namespacesQuery}
          errorTitle="Can not load namespaces"
        >
          <PageSection isWidthLimited padding={{ default: "noPadding" }}>
            <Toolbar>
              <ToolbarContent>
                <ToolbarItem>Namespace:</ToolbarItem>
                <ToolbarItem>
                  <ContextSelector
                    toggleText={namespaceContext?.name}
                    isOpen={isContextSelectorOpen}
                    onToggle={toggleContextSelector}
                    searchInputValue={filterText}
                    onSearchInputChange={setFilterText}
                    onSearchButtonClick={() => {}}
                    screenReaderLabel="Selected namespace:"
                    footer={
                      <ContextSelectorFooter>
                        <Button
                          variant="secondary"
                          isInline
                          onClick={() => modal.open(ModalAction.ADD)}
                        >
                          {t("actions.create-object", { what: "namespace" })}
                        </Button>
                      </ContextSelectorFooter>
                    }
                  >
                    {(namespacesQuery.data || [])
                      .filter((f) => f.name.indexOf(filterText) !== -1)
                      .map((item, index) => {
                        return (
                          <ContextSelectorItem
                            key={index}
                            onClick={() => onSelectNamespace(item)}
                          >
                            {item.name}
                          </ContextSelectorItem>
                        );
                      })}
                  </ContextSelector>
                </ToolbarItem>
              </ToolbarContent>
            </Toolbar>
          </PageSection>
          <Divider />
        </ResolvedQuery>
        {namespaceContext ? children : <NoNamespaceSelected />}
        {modal.isOpen && modal.isAction(ModalAction.ADD) && (
          <AddNamespaceWizard
            onSave={(ns) => {
              modal.close();
              history.push(documentsPath(ns));
            }}
            onClose={modal.close}
          />
        )}
      </>
    );
  };

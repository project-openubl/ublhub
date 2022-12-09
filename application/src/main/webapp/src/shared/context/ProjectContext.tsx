import React, {
  createContext,
  useContext,
  useEffect,
  useReducer,
  useState,
} from "react";

import {
  ContextSelector,
  ContextSelectorItem,
  ContextSelectorProps,
} from "@patternfly/react-core";

export interface ContextOption {
  key: string;
  label: string;
}

interface IProjectContext {
  allContexts: ContextOption[];
  currentContext?: ContextOption;
  selectContext: (key: string) => void;
}

const ProjectContext = createContext<IProjectContext>({
  allContexts: [],
  currentContext: undefined,
  selectContext: () => undefined,
});

interface IProjectContextProviderProps {
  allContexts: ContextOption[];
  children: React.ReactNode;
}

export const ProjectContextProvider: React.FC<IProjectContextProviderProps> = ({
  allContexts,
  children,
}: IProjectContextProviderProps) => {
  const [selectedContextKey, setSelectedContextKey] = useState<string>();

  return (
    <ProjectContext.Provider
      value={{
        allContexts,
        currentContext: allContexts.find((f) => f.key === selectedContextKey),
        selectContext: (key: string) => setSelectedContextKey(key),
      }}
    >
      {children}
    </ProjectContext.Provider>
  );
};

export const useProjectContext = (): IProjectContext =>
  useContext(ProjectContext);

// Helpers components

export interface IProjectContextSelectorProps {
  contextKeyFromURL?: string;
  props?: Omit<
    ContextSelectorProps,
    | "isOpen"
    | "toggleText"
    | "onToggle"
    | "searchInputValue"
    | "onSearchInputChange"
  >;
  onChange: (context: ContextOption) => void;
}

export const ProjectContextSelector: React.FC<IProjectContextSelectorProps> = ({
  contextKeyFromURL,
  props,
  onChange,
}) => {
  const { allContexts, currentContext, selectContext } = useProjectContext();

  useEffect(() => {
    const currentContextKey = contextKeyFromURL ?? currentContext?.key;

    if (typeof currentContextKey === "string") {
      selectContext(currentContextKey);
    }
  }, [contextKeyFromURL, currentContext, selectContext]);

  const [filterText, setFilterText] = useState("");
  const [isSelectorOpen, toggleSelector] = useReducer(
    (isVisible) => !isVisible,
    false
  );

  const onSelect = (value: ContextOption) => {
    toggleSelector();
    selectContext(value.key);
    onChange(value);
  };

  return (
    <ContextSelector
      isOpen={isSelectorOpen}
      toggleText={currentContext?.label}
      onToggle={toggleSelector}
      searchInputValue={filterText}
      onSearchInputChange={setFilterText}
      {...props}
    >
      {allContexts
        .filter(
          (f) => f.label.toLowerCase().indexOf(filterText.toLowerCase()) !== -1
        )
        .map((item, index) => {
          return (
            <ContextSelectorItem key={index} onClick={() => onSelect(item)}>
              {item.label}
            </ContextSelectorItem>
          );
        })}
    </ContextSelector>
  );
};

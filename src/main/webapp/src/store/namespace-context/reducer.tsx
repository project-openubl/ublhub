import { ActionType, getType } from "typesafe-actions";

import { setSelectedNamespace } from "./actions";

export const stateKey = "namespaceContext";

export type NamespaceContextState = Readonly<{
  selected?: string;
}>;

export const defaultState: NamespaceContextState = {
  selected: undefined,
};

export type NamespaceContextAction = ActionType<typeof setSelectedNamespace>;

export function namespaceContextReducer(
  state = defaultState,
  action: NamespaceContextAction
): NamespaceContextState {
  switch (action.type) {
    case getType(setSelectedNamespace):
      return {
        ...state,
        selected: action.payload,
      };

    default:
      return state;
  }
}

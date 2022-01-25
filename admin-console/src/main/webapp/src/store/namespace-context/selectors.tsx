import { RootState } from "../rootReducer";
import { stateKey } from "./reducer";

export const namespaceContextState = (state: RootState) => state[stateKey];

export const selectedNamespace = (state: RootState) =>
  namespaceContextState(state).selected;

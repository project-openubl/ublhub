import { createAction } from "typesafe-actions";

export const setSelectedNamespace = createAction(
  "context/namespace/set-selected"
)<string>();

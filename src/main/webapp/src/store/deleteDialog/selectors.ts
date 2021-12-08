import { RootState } from "../rootReducer";
import { stateKey } from "./reducer";

export const deleteDialogState = (state: RootState) => state[stateKey];

export const isProcessing = (state: RootState) =>
  deleteDialogState(state).isProcessing;

export const isOpen = (state: RootState) => deleteDialogState(state).isOpen;

export const isError = (state: RootState) => deleteDialogState(state).isError;

export const name = (state: RootState) => deleteDialogState(state).name;

export const type = (state: RootState) => deleteDialogState(state).type;

export const onDelete = (state: RootState) => deleteDialogState(state).onDelete;

export const config = (state: RootState) => deleteDialogState(state).config;

import { createAction } from "typesafe-actions";

interface Item {
  name: string;
  type: string;
  onDelete: () => void;
  config?: {
    title: string;
    message: string;
    deleteBtnLabel?: string;
    cancelBtnLabel?: string;
  };
}

export const openModal = createAction("dialog/delete/open")<Item>();
export const closeModal = createAction("dialog/delete/close")<void>();
export const processing = createAction("dialog/delete/processing")<void>();
export const error = createAction("dialog/delete/error")<void>();

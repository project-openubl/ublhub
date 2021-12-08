import { connect } from "react-redux";
import DeleteDialogBase from "./delete-dialog";
import { deleteDialogSelectors, deleteDialogActions } from "store/deleteDialog";
import { createMapStateToProps } from "store/common";

export default connect(
  createMapStateToProps((state) => ({
    onDelete: deleteDialogSelectors.onDelete(state),
    isOpen: deleteDialogSelectors.isOpen(state),
    isProcessing: deleteDialogSelectors.isProcessing(state),
    isError: deleteDialogSelectors.isError(state),
    name: deleteDialogSelectors.name(state),
    type: deleteDialogSelectors.type(state),
    config: deleteDialogSelectors.config(state),
  })),
  {
    onCancel: deleteDialogActions.closeModal,
  }
)(DeleteDialogBase);

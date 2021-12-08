import React from "react";
import { Button, Modal, ButtonVariant } from "@patternfly/react-core";
import { deleteDialogActions } from "store/deleteDialog";

interface Props {
  onDelete: () => void;
  onCancel: typeof deleteDialogActions.closeModal;
  isOpen: boolean;
  isProcessing: boolean;
  isError: boolean;
  name: string;
  type: string;
  config: any;
}

interface State {}

class DeleteDialogBase extends React.Component<Props, State> {
  public render() {
    const {
      type,
      name,
      onDelete,
      onCancel,
      isOpen,
      isProcessing,
      isError,
      config,
    } = this.props;

    return (
      <Modal
        variant={"small"}
        title={config.title ? config.title : `Eliminar ${name}?`}
        onClose={() => {
          onCancel();
        }}
        isOpen={isOpen}
        actions={[
          <Button
            key="confirm"
            isDisabled={isProcessing}
            variant={ButtonVariant.danger}
            onClick={onDelete}
          >
            {config.deleteBtnLabel ? config.deleteBtnLabel : "Eliminar"}
          </Button>,
          <Button
            key="cancel"
            isDisabled={isProcessing}
            variant={ButtonVariant.link}
            onClick={() => {
              onCancel();
            }}
          >
            {config.cancelBtnLabel ? config.cancelBtnLabel : "Cancelar"}
          </Button>,
        ]}
      >
        {isError
          ? `Ops! There was a problem while executing your action.`
          : config.message
          ? config.message
          : `¿Estas seguro de querer eliminar este(a) ${type}? Esta acción eliminará ${name} permanentemente.`}
      </Modal>
    );
  }
}

export default DeleteDialogBase;

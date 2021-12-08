import React from "react";
import { Modal, ModalVariant, Button } from "@patternfly/react-core";

import { Contribuyente } from "api/models";
import { ContribuyenteDetails } from "shared/components";

export interface DetailsModalProps {
  value?: Contribuyente;
  onClose: () => void;
}

export const DetailsModal: React.FC<DetailsModalProps> = ({
  value,
  onClose,
}) => {
  return (
    <Modal
      variant={ModalVariant.medium}
      title="Detalle"
      isOpen={!!value}
      onClose={onClose}
      aria-label="details-modal"
      actions={[
        <Button key="close" variant="primary" onClick={onClose}>
          Cerrar
        </Button>,
      ]}
    >
      {value && <ContribuyenteDetails value={value} />}
    </Modal>
  );
};

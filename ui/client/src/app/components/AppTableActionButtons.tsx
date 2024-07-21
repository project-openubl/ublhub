import React from "react";
import { Button, Flex, FlexItem } from "@patternfly/react-core";
import { ConditionalTooltip } from "./ConditionalTooltip";

export interface AppTableActionButtonsProps {
  isDeleteEnabled?: boolean;
  tooltipMessage?: string;
  onEdit: () => void;
  onDelete: () => void;
}

export const AppTableActionButtons: React.FC<AppTableActionButtonsProps> = ({
  isDeleteEnabled = false,
  tooltipMessage = "",
  onEdit,
  onDelete,
}) => {
  return (
    <>
      <Flex>
        <FlexItem align={{ default: "alignRight" }}>
          <Button
            id="edit-button"
            aria-label="edit"
            variant="secondary"
            onClick={onEdit}
          >
            Edit
          </Button>
        </FlexItem>
        <FlexItem>
          <ConditionalTooltip
            isTooltipEnabled={isDeleteEnabled}
            content={tooltipMessage}
          >
            <Button
              id="delete-button"
              aria-label="delete"
              variant="link"
              onClick={onDelete}
              isAriaDisabled={isDeleteEnabled}
            >
              Eliminar
            </Button>
          </ConditionalTooltip>
        </FlexItem>
      </Flex>
    </>
  );
};

import * as React from "react";
import { Button } from "@patternfly/react-core";
import EyeSlashIcon from "@patternfly/react-icons/dist/js/icons/eye-slash-icon";
import EyeIcon from "@patternfly/react-icons/dist/js/icons/eye-icon";

interface IKeyDisplayToggleProps {
  keyName: string;
  isKeyHidden: boolean;
  onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
}

export const KeyDisplayToggle: React.FC<IKeyDisplayToggleProps> = ({
  keyName,
  isKeyHidden,
  onClick,
}: IKeyDisplayToggleProps) => (
  <Button variant="link" aria-label={`Show/hide ${keyName}`} onClick={onClick}>
    <span className="pf-v5-c-icon pf-m-info">
      {isKeyHidden ? <EyeSlashIcon /> : <EyeIcon />}
    </span>
  </Button>
);

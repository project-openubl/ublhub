import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Dropdown, DropdownItem, DropdownToggle } from "@patternfly/react-core";
import { CaretDownIcon } from "@patternfly/react-icons";

export interface MenuActionsProps {
  actions: { label: string; callback: () => void }[];
}

export const MenuActions: React.FC<MenuActionsProps> = ({ actions }) => {
  const { t } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <Dropdown
      isOpen={isOpen}
      onSelect={() => {
        setIsOpen(!isOpen);
      }}
      toggle={
        <DropdownToggle
          onToggle={(isOpen: boolean) => {
            setIsOpen(isOpen);
          }}
          toggleIndicator={CaretDownIcon}
        >
          {t("terms.actions")}
        </DropdownToggle>
      }
      dropdownItems={actions.map((element, index) => (
        <DropdownItem key={index} component="button" onClick={element.callback}>
          {element.label}
        </DropdownItem>
      ))}
    />
  );
};

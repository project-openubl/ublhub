import React, { useState } from "react";
import {
  Dropdown,
  DropdownItem,
  DropdownSeparator,
  KebabToggle,
} from "@patternfly/react-core";
import { HelpIcon } from "@patternfly/react-icons";
import { AppAboutModal } from "../AppAboutModal";

export const MobileDropdown: React.FC = () => {
  const [isKebabDropdownOpen, setIsKebabDropdownOpen] = useState(false);
  const [isAboutModalOpen, setAboutModalOpen] = useState(false);

  const onKebabDropdownToggle = (isOpen: boolean) => {
    setIsKebabDropdownOpen(isOpen);
  };
  const onKebabDropdownSelect = () => {
    setIsKebabDropdownOpen((current) => !current);
  };

  const toggleAboutModal = () => {
    setAboutModalOpen((current) => !current);
  };

  return (
    <>
      <Dropdown
        isPlain
        position="right"
        onSelect={onKebabDropdownSelect}
        toggle={<KebabToggle onToggle={onKebabDropdownToggle} />}
        isOpen={isKebabDropdownOpen}
        dropdownItems={[
          <DropdownItem key="logout">Logout</DropdownItem>,
          <DropdownSeparator key="separator" />,
          <DropdownItem key="about" onClick={toggleAboutModal}>
            <HelpIcon />
            &nbsp;About
          </DropdownItem>,
        ]}
      />
      <AppAboutModal isOpen={isAboutModalOpen} onClose={toggleAboutModal} />
    </>
  );
};

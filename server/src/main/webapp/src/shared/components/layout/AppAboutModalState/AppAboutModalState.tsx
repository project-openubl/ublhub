import React, { useState } from "react";
import { AppAboutModal } from "../AppAboutModal";

export interface ChildrenProps {
  isOpen: boolean;
  toggleModal: () => void;
}

export interface AppAboutModalStateProps {
  children: (args: ChildrenProps) => any;
}

export const AppAboutModalState: React.FC<AppAboutModalStateProps> = ({
  children,
}) => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleModal = () => {
    setIsOpen((current) => !current);
  };

  return (
    <>
      {children({
        isOpen,
        toggleModal,
      })}
      <AppAboutModal isOpen={isOpen} onClose={toggleModal} />
    </>
  );
};

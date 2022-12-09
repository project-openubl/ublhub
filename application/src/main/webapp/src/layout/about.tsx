import React from "react";
import { AboutModal, TextContent } from "@patternfly/react-core";

import { Theme } from "./theme-constants";

interface IButtonAboutAppProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AboutApp: React.FC<IButtonAboutAppProps> = ({
  isOpen,
  onClose,
}) => {
  return (
    <AboutModal
      isOpen={isOpen}
      onClose={onClose}
      brandImageAlt="Brand Image"
      brandImageSrc={Theme.logoSrc}
      productName={Theme.name}
      className="about-app__component"
    >
      <TextContent className="pf-u-py-xl">
        <h4>About</h4>
        <p>
          {Theme.name} te permite administrar tus comprobantes de pago
          electrónico. Create, almacena, y envía tus XMLs a la SUNAT.
        </p>
      </TextContent>
    </AboutModal>
  );
};

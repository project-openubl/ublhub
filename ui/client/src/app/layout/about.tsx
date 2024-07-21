import React from "react";

import {
  AboutModal,
  Text,
  TextContent,
  TextList,
  TextListItem,
  TextVariants,
} from "@patternfly/react-core";

import openublBrandImage from "@app/images/Openubl-white-logo.svg";
import lfBrandImage from "@app/images/Openubl-white-logo.svg";

import backgroundImage from "@app/images/pfbg-icon.svg";

import { APP_BRAND, BrandType } from "@app/Constants";
import ENV from "@app/env";

interface IButtonAboutAppProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AboutApp: React.FC<IButtonAboutAppProps> = ({
  isOpen,
  onClose,
}) => {
  const brandName = APP_BRAND === BrandType.Openubl ? "Openubl" : "LibreFact";

  return (
    <AboutModal
      isOpen={isOpen}
      onClose={onClose}
      productName={brandName}
      brandImageAlt="Logo"
      brandImageSrc={
        APP_BRAND === BrandType.Openubl ? openublBrandImage : lfBrandImage
      }
      backgroundImageSrc={backgroundImage}
      trademark="COPYRIGHT © 2022."
    >
      <TextContent>
        <Text component={TextVariants.h4}>Acerca de</Text>
        <Text component={TextVariants.p}>
          {brandName} es una colección de herramientas que ayudan en la
          administración de documentos electrónicos basados en UBL (Universal
          Busissess Language) y los estándares de la SUNAT (Perú).
        </Text>
        <Text component={TextVariants.p}>
          {brandName} Permite crear, firmar, enviar, y consultar comprobantes
          electrónicos en la SUNAT
        </Text>
        <Text component={TextVariants.p}>
          {brandName} es un proyecto de{" "}
          <Text
            component={TextVariants.a}
            href="https://www.konveyor.io/"
            target="_blank"
          >
            Project OpenUBL
          </Text>
          .
        </Text>
        <Text component={TextVariants.p}>
          Para mayor información por favor diríjase a{" "}
          <Text
            component={TextVariants.a}
            href={"https://project-openubl.github.io/"}
            target="_blank"
          >
            {brandName} documentación
          </Text>
          .
        </Text>
      </TextContent>
      <TextContent className="pf-v5-u-py-xl">
        <TextContent>
          <TextList component="dl">
            <TextListItem component="dt">Versión</TextListItem>
            <TextListItem component="dd">{ENV.VERSION}</TextListItem>
          </TextList>
        </TextContent>
      </TextContent>
    </AboutModal>
  );
};

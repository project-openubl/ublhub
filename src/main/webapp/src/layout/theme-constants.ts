import windupLogo from "images/themes/logo.svg";
import windupNavBrandImage from "images/themes/navbar.svg";

type ThemeType = "ublhub";
const defaultTheme: ThemeType = "ublhub";

type ThemeListType = {
  [key in ThemeType]: {
    name: string;
    logoSrc: string;
    logoNavbarSrc: string;
    faviconSrc?: string;
  };
};

const themeList: ThemeListType = {
  ublhub: {
    name: "Windup",
    logoSrc: windupLogo,
    logoNavbarSrc: windupNavBrandImage,
  },
};

export const Theme =
  themeList[(process.env.REACT_APP_THEME as ThemeType) || defaultTheme];

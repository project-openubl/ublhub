import ublHubLogo from "images/themes/logo.svg";
import ublHubNavBrandImage from "images/themes/navbar.svg";

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
    name: "Ublhub",
    logoSrc: ublHubLogo,
    logoNavbarSrc: ublHubNavBrandImage,
  },
};

export const Theme =
  themeList[(process.env.REACT_APP_THEME as ThemeType) || defaultTheme];

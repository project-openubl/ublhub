import { Tab, Tabs, TabTitleText } from "@patternfly/react-core";
import React from "react";
import { useLocation, useNavigate } from "react-router-dom";

export interface HorizontalNavProps {
  navItems: { title: string; path: string }[];
}

export const HorizontalNav: React.FC<HorizontalNavProps> = ({ navItems }) => {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Tabs
      activeKey={navItems.find((e) => e.path === location.pathname)?.path}
      onSelect={(_, tabKey) => navigate(`${tabKey}`)}
      isBox
      inset={{
        default: "insetNone",
        md: "insetSm",
        xl: "insetLg",
      }}
    >
      {navItems.map((e, index) => (
        <Tab
          key={index}
          eventKey={e.path}
          title={<TabTitleText>{e.title}</TabTitleText>}
        />
      ))}
    </Tabs>
  );
};

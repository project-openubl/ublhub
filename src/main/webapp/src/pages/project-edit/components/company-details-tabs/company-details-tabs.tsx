import React from "react";
import { useTranslation } from "react-i18next";

import {
  Sidebar,
  SidebarContent,
  SidebarPanel,
  Tab,
  TabContent,
  TabContentBody,
  Tabs,
  TabTitleText,
} from "@patternfly/react-core";

import { CompanyDto, ProjectDto } from "api/models";

import { General } from "./general";
import { Sunat } from "./sunat";

interface ICompanyDetailsTabsProps {
  project: ProjectDto;
  company: CompanyDto;
}

export const CompanyDetailsTabs: React.FC<ICompanyDetailsTabsProps> = ({
  project,
  company,
}) => {
  const { t } = useTranslation();

  const tabContent0 = React.createRef<HTMLElement>();
  const tabContent1 = React.createRef<HTMLElement>();
  const tabContent2 = React.createRef<HTMLElement>();

  return (
    <Sidebar hasGutter style={{ paddingLeft: 40 }}>
      <SidebarPanel>
        <Tabs
          aria-label="Company details"
          defaultActiveKey={0}
          isVertical
          isBox
        >
          <Tab
            eventKey={0}
            title={<TabTitleText>{t("terms.general")}</TabTitleText>}
            tabContentId="tab0"
            tabContentRef={tabContent0}
          />
          <Tab
            eventKey={1}
            title={<TabTitleText>SUNAT</TabTitleText>}
            tabContentId="tab1"
            tabContentRef={tabContent1}
          />
          <Tab
            eventKey={2}
            title={<TabTitleText>{t("terms.certificates")}</TabTitleText>}
            tabContentId="tab2"
            tabContentRef={tabContent2}
          />
        </Tabs>
      </SidebarPanel>
      <SidebarContent>
        <TabContent eventKey={0} id="tab0" ref={tabContent0}>
          <TabContentBody>
            <General project={project} company={company} />
          </TabContentBody>
        </TabContent>
        <TabContent eventKey={1} id="tab1" ref={tabContent1} hidden>
          <TabContentBody>
            <Sunat project={project} company={company} />
          </TabContentBody>
        </TabContent>
        <TabContent eventKey={2} id="tab2" ref={tabContent2} hidden>
          <TabContentBody hasPadding>
            Tab 3 section with body and padding
          </TabContentBody>
        </TabContent>
      </SidebarContent>
    </Sidebar>
  );
};

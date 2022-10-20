import React from "react";
import { useTranslation } from "react-i18next";

import {
  Tab,
  TabContent,
  TabContentBody,
  Tabs,
  TabTitleText,
} from "@patternfly/react-core";

import { CompanyDto, ProjectDto } from "api/models";

import { General } from "./general";
import { Sunat } from "./sunat";
import { Certificates } from "./certificates";

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
    <>
      <Tabs
        aria-label="Company details"
        defaultActiveKey={0}
        isBox
        inset={{
          default: "insetNone",
          md: "insetSm",
          xl: "insetLg",
        }}
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
      <div>
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
          <TabContentBody>
            <Certificates project={project} company={company} />
          </TabContentBody>
        </TabContent>
      </div>
    </>
  );
};
